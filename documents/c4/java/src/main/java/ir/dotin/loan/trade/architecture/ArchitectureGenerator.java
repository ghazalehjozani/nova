package ir.dotin.loan.trade.architecture;

import java.nio.file.Path;

import com.structurizr.Workspace;

import ir.dotin.loan.trade.architecture.builder.*;
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

        System.out.println("🏗️  C4 Architecture Generation");
        System.out.println("   Output: " + outputDir);

        try {
            var generator = new ArchitectureGenerator();
            var workspace = generator.generate();
            generator.export(workspace, Path.of(outputDir));
            System.out.println("✅ Done!");
        } catch (Exception e) {
            System.err.println("❌ Generation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Workspace generate() {
        var workspace =
                new Workspace(config.workspace().name(), config.workspace().description());

        var modelBuilder = new ModelBuilder(workspace.getModel(), config);
        modelBuilder.buildStaticModel();

        var mainContainer = modelBuilder.getMainContainer();
        if (mainContainer == null) {
            throw new IllegalStateException("Main container not found");
        }

        var scanner = new ComponentScanner(config.workspace().basePackage());
        var scannedComponents = scanner.scanComponents();

        modelBuilder.buildComponents(mainContainer, scannedComponents);
        modelBuilder.buildComponentRelationships(mainContainer, scannedComponents);

        var mainSystem = modelBuilder.getMainSystem();
        if (mainSystem == null) {
            throw new IllegalStateException("Main system not found");
        }

        var viewBuilder = new ViewBuilder(workspace.getViews(), mainSystem, mainContainer);
        viewBuilder.buildAllViews();

        var styleBuilder = new StyleBuilder(workspace.getViews(), config.styles());
        styleBuilder.applyAllStyles();

        return workspace;
    }

    public void export(Workspace workspace, Path outputDir) throws Exception {
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
