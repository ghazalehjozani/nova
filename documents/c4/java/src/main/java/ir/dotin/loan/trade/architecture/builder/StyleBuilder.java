package ir.dotin.loan.trade.architecture.builder;

import com.structurizr.view.*;

import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.ElementStyleConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.RelationshipStyleConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.StyleConfig;

public class StyleBuilder {

    private final Styles styles;
    private final StyleConfig config;

    public StyleBuilder(ViewSet views, StyleConfig config) {
        this.styles = views.getConfiguration().getStyles();
        this.config = config;
    }

    public void applyAllStyles() {
        if (config == null) return;
        if (config.elements() != null) config.elements().forEach(this::applyElementStyle);
        if (config.relationships() != null) config.relationships().forEach(this::applyRelationshipStyle);
    }

    private void applyElementStyle(String tag, ElementStyleConfig cfg) {
        if (tag == null || cfg == null) return;
        var style = styles.addElementStyle(tag);
        if (cfg.background() != null) style.background(cfg.background());
        if (cfg.color() != null) style.color(cfg.color());
        if (cfg.shape() != null) style.shape(parseShape(cfg.shape()));
        if (cfg.border() != null) style.border(parseBorder(cfg.border()));
    }

    private void applyRelationshipStyle(String tag, RelationshipStyleConfig cfg) {
        if (tag == null || cfg == null) return;
        var style = styles.addRelationshipStyle(tag);
        if (cfg.color() != null) style.color(cfg.color());
        if (cfg.thickness() > 0) style.thickness(cfg.thickness());
        if (cfg.style() != null) style.style(parseLineStyle(cfg.style()));
    }

    private Shape parseShape(String shape) {
        return switch (shape.toLowerCase()) {
            case "person" -> Shape.Person;
            case "cylinder" -> Shape.Cylinder;
            case "pipe" -> Shape.Pipe;
            case "hexagon" -> Shape.Hexagon;
            case "diamond" -> Shape.Diamond;
            case "webbrowser" -> Shape.WebBrowser;
            case "folder" -> Shape.Folder;
            case "robot" -> Shape.Robot;
            case "ellipse" -> Shape.Ellipse;
            case "circle" -> Shape.Circle;
            case "roundedbox" -> Shape.RoundedBox;
            case "component" -> Shape.Component;
            default -> Shape.Box;
        };
    }

    private Border parseBorder(String border) {
        return switch (border.toLowerCase()) {
            case "dashed" -> Border.Dashed;
            case "dotted" -> Border.Dotted;
            default -> Border.Solid;
        };
    }

    private LineStyle parseLineStyle(String style) {
        return switch (style.toLowerCase()) {
            case "dashed" -> LineStyle.Dashed;
            case "dotted" -> LineStyle.Dotted;
            default -> LineStyle.Solid;
        };
    }
}
