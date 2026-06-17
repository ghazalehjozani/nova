package ir.dotin.loan.trade.architecture;

import java.nio.file.Files;
import java.nio.file.Path;

import com.structurizr.Workspace;

import ir.dotin.loan.trade.architecture.builder.AdrImporter;
import ir.dotin.loan.trade.architecture.builder.ModelBuilder;
import ir.dotin.loan.trade.architecture.builder.StyleBuilder;
import ir.dotin.loan.trade.architecture.builder.ViewBuilder;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig;
import ir.dotin.loan.trade.architecture.config.DefaultConfig;
import ir.dotin.loan.trade.architecture.export.WorkspaceExporter;

public class ArchitectureGenerator {

    private final ArchitectureConfig config;
    private final WorkspaceExporter exporter;

    public ArchitectureGenerator() {
        this(DefaultConfig.load());
    }

    public ArchitectureGenerator(ArchitectureConfig config) {
        this.config = config;
        this.exporter = new WorkspaceExporter();
    }

    public static void main(String[] args) {
        var outputDir = args.length > 0 ? args[0] : "documents/c4";
        // outputDir is <reactor>/documents/c4 -> the reactor root is two levels up.
        var reactorRoot = Path.of(outputDir).toAbsolutePath().normalize().getParent();
        if (reactorRoot != null) reactorRoot = reactorRoot.getParent();

        System.out.println("🏗️  C4 Architecture Generation");
        System.out.println("   Current Working Directory: " + System.getProperty("user.dir"));
        System.out.println("   Output: " + outputDir);
        System.out.println("   Reactor root: " + reactorRoot);

        try {
            var generator = new ArchitectureGenerator();
            var workspace = generator.generate(reactorRoot);
            generator.export(workspace, Path.of(outputDir));
            System.out.println("✅ Done!");
        } catch (Exception e) {
            System.err.println("❌ Generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Workspace generate(Path reactorRoot) {
        var workspace =
                new Workspace(config.workspace().name(), config.workspace().description());

        var modelBuilder = new ModelBuilder(workspace.getModel(), config);
        modelBuilder.buildStaticModel();

        var mainContainer = modelBuilder.getMainContainer();
        if (mainContainer == null) {
            throw new IllegalStateException("Main container not found");
        }

        var discovery = new ComponentDiscovery(config.workspace().basePackage());
        discovery.discover(mainContainer, reactorRoot);

        modelBuilder.wireExternalSystemUsage();
        modelBuilder.wireComponentInfrastructure(mainContainer);
        modelBuilder.wireWorkflows(mainContainer);

        var mainSystem = modelBuilder.getMainSystem();
        if (mainSystem == null) {
            throw new IllegalStateException("Main system not found");
        }

        var viewBuilder = new ViewBuilder(workspace.getViews(), mainSystem, mainContainer, config.workflows());
        viewBuilder.buildAllViews();

        var styleBuilder = new StyleBuilder(workspace.getViews(), config.styles());
        styleBuilder.applyAllStyles();

        Path adrPath = findAdrDirectory();

        if (adrPath != null) {
            System.out.println("   📄 Importing ADRs from: " + adrPath.toAbsolutePath());
            new AdrImporter().importAdrs(workspace, adrPath);
        } else {
            System.err.println("   ⚠️  ADR folder NOT found. Decisions will be missing from workspace.json.");
        }

        return workspace;
    }

    private Path findAdrDirectory() {
        Path current = Path.of(".").toAbsolutePath().normalize();

        for (int i = 0; i < 5; i++) {
            Path docsAdr = current.resolve("documents").resolve("adr");
            if (Files.isDirectory(docsAdr)) {
                return docsAdr;
            }
            Path standardAdr = current.resolve("adr");
            if (Files.isDirectory(standardAdr)) {
                return standardAdr;
            }
            current = current.getParent();
            if (current == null) break;
        }
        return null;
    }

    public void export(Workspace workspace, Path outputDir) throws Exception {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        exporter.exportJson(workspace, outputDir.resolve("workspace.json"));
        exporter.exportDsl(workspace, outputDir.resolve("generated-workspace.dsl"));

        try {
            exporter.exportMermaid(workspace, outputDir.resolve("mermaid"));
        } catch (Exception e) {
            System.err.println("   ⚠️  Mermaid export failed: " + e.getMessage());
        }

        try {
            exporter.exportPlantUML(workspace, outputDir.resolve("plantuml"));
        } catch (Exception e) {
            System.err.println("   ⚠️  PlantUML export failed: " + e.getMessage());
        }

        try {
            exporter.exportDot(workspace, outputDir.resolve("dot"));
        } catch (Exception e) {
            System.err.println("   ⚠️  DOT export failed: " + e.getMessage());
        }
    }
}
