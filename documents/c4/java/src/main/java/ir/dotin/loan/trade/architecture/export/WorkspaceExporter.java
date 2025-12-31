package ir.dotin.loan.trade.architecture.export;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;

import com.structurizr.Workspace;
import com.structurizr.export.Diagram;
import com.structurizr.export.dot.DOTExporter;
import com.structurizr.export.mermaid.MermaidDiagramExporter;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
import com.structurizr.util.WorkspaceUtils;

public class WorkspaceExporter {

    public void exportJson(Workspace workspace, Path outputPath) throws Exception {
        Files.createDirectories(outputPath.getParent());
        WorkspaceUtils.saveWorkspaceToJson(workspace, outputPath.toFile());
        System.out.println("   📄 JSON: " + outputPath);
    }

    public void exportMermaid(Workspace workspace, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        var exporter = new MermaidDiagramExporter();
        Collection<Diagram> diagrams = exporter.export(workspace);
        for (var diagram : diagrams) {
            Files.writeString(outputDir.resolve(diagram.getKey() + ".mmd"), diagram.getDefinition());
        }
        System.out.println("   📊 Mermaid: " + outputDir + " (" + diagrams.size() + " diagrams)");
    }

    public void exportPlantUML(Workspace workspace, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        var exporter = new StructurizrPlantUMLExporter();
        Collection<Diagram> diagrams = exporter.export(workspace);
        for (var diagram : diagrams) {
            Files.writeString(outputDir.resolve(diagram.getKey() + ".puml"), diagram.getDefinition());
        }
        System.out.println("   🌱 PlantUML: " + outputDir + " (" + diagrams.size() + " diagrams)");
    }

    public void exportDot(Workspace workspace, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        var exporter = new DOTExporter();
        Collection<Diagram> diagrams = exporter.export(workspace);
        for (var diagram : diagrams) {
            Files.writeString(outputDir.resolve(diagram.getKey() + ".dot"), diagram.getDefinition());
        }
        System.out.println("   📐 DOT: " + outputDir + " (" + diagrams.size() + " diagrams)");
    }

    public void exportDsl(Workspace workspace, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        var dsl = new DslFormatter().format(workspace);
        Files.writeString(outputPath, dsl);
        System.out.println("   📝 DSL: " + outputPath);
    }

    private static class DslFormatter {
        String format(Workspace ws) {
            var sb = new StringBuilder();
            sb.append("workspace \"").append(esc(ws.getName())).append("\" {\n\n");
            sb.append("    description \"").append(esc(ws.getDescription())).append("\"\n\n");
            sb.append("    model {\n");
            formatModel(ws, sb);
            sb.append("    }\n\n");
            sb.append("    views {\n");
            formatViews(ws, sb);
            sb.append("    }\n}\n");
            return sb.toString();
        }

        private void formatModel(Workspace ws, StringBuilder sb) {
            var model = ws.getModel();
            model.getPeople().forEach(p -> sb.append("        ")
                    .append(id(p.getName()))
                    .append(" = person \"")
                    .append(esc(p.getName()))
                    .append("\"")
                    .append(p.getDescription() != null ? " \"" + esc(p.getDescription()) + "\"" : "")
                    .append("\n"));
            sb.append("\n");

            model.getSoftwareSystems().forEach(sys -> {
                sb.append("        ")
                        .append(id(sys.getName()))
                        .append(" = softwareSystem \"")
                        .append(esc(sys.getName()))
                        .append("\"");
                if (sys.getDescription() != null)
                    sb.append(" \"").append(esc(sys.getDescription())).append("\"");
                if (!sys.getContainers().isEmpty()) {
                    sb.append(" {\n");
                    sys.getContainers().forEach(c -> sb.append("            ")
                            .append(id(c.getName()))
                            .append(" = container \"")
                            .append(esc(c.getName()))
                            .append("\"")
                            .append(c.getDescription() != null ? " \"" + esc(c.getDescription()) + "\"" : "")
                            .append(c.getTechnology() != null ? " \"" + esc(c.getTechnology()) + "\"" : "")
                            .append("\n"));
                    sb.append("        }\n");
                } else {
                    sb.append("\n");
                }
            });
        }

        private void formatViews(Workspace ws, StringBuilder sb) {
            ws.getViews().getSystemContextViews().forEach(v -> sb.append("        systemContext ")
                    .append(id(v.getSoftwareSystem().getName()))
                    .append(" \"")
                    .append(v.getKey())
                    .append("\" { include * autoLayout }\n"));
            ws.getViews().getContainerViews().forEach(v -> sb.append("        container ")
                    .append(id(v.getSoftwareSystem().getName()))
                    .append(" \"")
                    .append(v.getKey())
                    .append("\" { include * autoLayout }\n"));
            ws.getViews().getComponentViews().forEach(v -> sb.append("        component ")
                    .append(id(v.getContainer().getName()))
                    .append(" \"")
                    .append(v.getKey())
                    .append("\" { include * autoLayout }\n"));
            sb.append("        styles { }\n");
        }

        private String id(String name) {
            return name == null
                    ? "unknown"
                    : name.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        }

        private String esc(String s) {
            return s == null ? "" : s.replace("\"", "\\\"").replace("\n", " ");
        }
    }
}
