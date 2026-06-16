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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkspaceExporter {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceExporter.class);

    public void exportJson(Workspace workspace, Path outputPath) throws Exception {
        Files.createDirectories(outputPath.getParent());
        WorkspaceUtils.saveWorkspaceToJson(workspace, outputPath.toFile());
        log.info("\n 📄 JSON: {}", outputPath);
    }

    public void exportMermaid(Workspace workspace, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        var exporter = new MermaidDiagramExporter();
        Collection<Diagram> diagrams = exporter.export(workspace);
        for (var diagram : diagrams) {
            Files.writeString(outputDir.resolve(diagram.getKey() + ".mmd"), diagram.getDefinition());
        }
        log.info("\n 📊 Mermaid: {} ({} diagrams)", outputDir, diagrams.size());
    }

    public void exportPlantUML(Workspace workspace, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        var exporter = new StructurizrPlantUMLExporter();
        Collection<Diagram> diagrams = exporter.export(workspace);
        for (var diagram : diagrams) {
            Files.writeString(outputDir.resolve(diagram.getKey() + ".puml"), diagram.getDefinition());
        }
        log.info("\n 🌱 PlantUML: {} ({} diagrams)", outputDir, diagrams.size());
    }

    public void exportDot(Workspace workspace, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        var exporter = new DOTExporter();
        Collection<Diagram> diagrams = exporter.export(workspace);
        for (var diagram : diagrams) {
            Files.writeString(outputDir.resolve(diagram.getKey() + ".dot"), diagram.getDefinition());
        }
        log.info("\n 📐 DOT: {} ({} diagrams)", outputDir, diagrams.size());
    }

    public void exportDsl(Workspace workspace, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        var dsl = new DslFormatter().format(workspace);
        Files.writeString(outputPath, dsl);
        log.info("\n 📝 DSL: {}", outputPath);
    }

    private static class DslFormatter {
        String format(Workspace ws) {
            var sb = new StringBuilder();
            sb.append("workspace \"").append(esc(ws.getName())).append("\" {\n\n");
            sb.append("    !adrs adr\n\n");
            sb.append("    model {\n");
            formatModel(ws, sb);
            sb.append("    }\n\n");
            sb.append("    views {\n");
            formatViews(ws, sb);
            sb.append("        styles {\n");
            formatStyles(ws, sb);
            sb.append("        }\n");
            sb.append("    }\n}\n");
            return sb.toString();
        }

        private void formatModel(Workspace ws, StringBuilder sb) {
            var model = ws.getModel();

            // 1. People
            model.getPeople()
                    .forEach(p -> sb.append("        ")
                            .append(id(p.getName()))
                            .append(" = person \"")
                            .append(esc(p.getName()))
                            .append("\"")
                            .append(p.getDescription() != null ? " \"" + esc(p.getDescription()) + "\"" : "")
                            .append("\n"));

            if (!model.getPeople().isEmpty()) {
                sb.append("\n");
            }

            // 2. Software Systems -> Containers -> Components
            model.getSoftwareSystems().forEach(sys -> {
                sb.append("        ")
                        .append(id(sys.getName()))
                        .append(" = softwareSystem \"")
                        .append(esc(sys.getName()))
                        .append("\"");
                if (sys.getDescription() != null) {
                    sb.append(" \"").append(esc(sys.getDescription())).append("\"");
                }

                if (!sys.getContainers().isEmpty()) {
                    sb.append(" {\n");
                    sys.getContainers().forEach(c -> {
                        sb.append("            ")
                                .append(id(c.getName()))
                                .append(" = container \"")
                                .append(esc(c.getName()))
                                .append("\"")
                                .append(c.getDescription() != null ? " \"" + esc(c.getDescription()) + "\"" : "")
                                .append(c.getTechnology() != null ? " \"" + esc(c.getTechnology()) + "\"" : "");

                        if (!c.getComponents().isEmpty()) {
                            sb.append(" {\n");
                            c.getComponents()
                                    .forEach(comp -> sb.append("                ")
                                            .append(id(comp.getName()))
                                            .append(" = component \"")
                                            .append(esc(comp.getName()))
                                            .append("\"")
                                            .append(
                                                    comp.getDescription() != null
                                                            ? " \"" + esc(comp.getDescription()) + "\""
                                                            : "")
                                            .append(
                                                    comp.getTechnology() != null
                                                            ? " \"" + esc(comp.getTechnology()) + "\""
                                                            : "")
                                            .append("\n"));
                            sb.append("            }\n");
                        } else {
                            sb.append("\n");
                        }
                    });
                    sb.append("        }\n");
                } else {
                    sb.append("\n");
                }
            });

            // 3. Relationships
            // Note: If component names are not unique across the workspace, simple ID generation might conflict.
            // Ideally, relationships should be nested or use hierarchical references, but this flat list works if names
            // are unique.
            model.getRelationships().forEach(rel -> {
                sb.append("        ")
                        .append(id(rel.getSource().getName()))
                        .append(" -> ")
                        .append(id(rel.getDestination().getName()));
                if (rel.getDescription() != null) {
                    sb.append(" \"").append(esc(rel.getDescription())).append("\"");
                }
                sb.append("\n");
            });
        }

        private void formatViews(Workspace ws, StringBuilder sb) {
            ws.getViews().getSystemContextViews().forEach(v -> {
                sb.append("        systemContext ")
                        .append(id(v.getSoftwareSystem().getName()))
                        .append(" \"")
                        .append(v.getKey())
                        .append("\" {\n");
                sb.append("            include *\n");
                sb.append("            autoLayout tb 300 300\n");
                sb.append("        }\n\n");
            });

            ws.getViews().getContainerViews().forEach(v -> {
                sb.append("        container ")
                        .append(id(v.getSoftwareSystem().getName()))
                        .append(" \"")
                        .append(v.getKey())
                        .append("\" {\n");
                sb.append("            include *\n");
                sb.append("            autoLayout tb 300 300\n");
                sb.append("        }\n\n");
            });

            ws.getViews().getComponentViews().forEach(v -> {
                sb.append("        component ")
                        .append(id(v.getContainer().getName()))
                        .append(" \"")
                        .append(v.getKey())
                        .append("\" {\n");
                sb.append("            include *\n");
                sb.append("            autoLayout ");
                if (v.getKey().contains("Flow")) {
                    sb.append("lr 400 300\n");
                } else {
                    sb.append("tb 300 300\n");
                }
                sb.append("        }\n\n");
            });
        }

        private void formatStyles(Workspace ws, StringBuilder sb) {
            ws.getViews().getConfiguration().getStyles().getElements().forEach(style -> {
                sb.append("            element \"").append(esc(style.getTag())).append("\" {\n");
                if (style.getBackground() != null) {
                    sb.append("                background ")
                            .append(style.getBackground())
                            .append("\n");
                }
                if (style.getColor() != null) {
                    sb.append("                color ").append(style.getColor()).append("\n");
                }
                if (style.getShape() != null) {
                    sb.append("                shape ")
                            .append(style.getShape().toString())
                            .append("\n");
                }
                sb.append("            }\n");
            });
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
