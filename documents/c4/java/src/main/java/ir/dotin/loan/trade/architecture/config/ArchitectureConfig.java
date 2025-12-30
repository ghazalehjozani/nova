package ir.dotin.loan.trade.architecture.config;

import java.util.List;
import java.util.Map;

public record ArchitectureConfig(
        WorkspaceConfig workspace,
        List<PersonConfig> persons,
        List<ExternalSystemConfig> externalSystems,
        SystemConfig mainSystem,
        List<ContainerConfig> containers,
        StyleConfig styles) {

    public record WorkspaceConfig(String name, String description, String basePackage) {}

    public record PersonConfig(String name, String description, List<String> tags, List<UsesConfig> uses) {}

    public record ExternalSystemConfig(String name, String description, List<String> tags) {}

    public record SystemConfig(String name, String description, List<String> tags) {}

    public record ContainerConfig(
            String name, String description, String technology, List<String> tags, List<UsesConfig> uses) {}

    public record UsesConfig(String target, String description, String technology) {}

    public record StyleConfig(
            Map<String, ElementStyleConfig> elements, Map<String, RelationshipStyleConfig> relationships) {}

    public record ElementStyleConfig(String background, String color, String shape, String border) {}

    public record RelationshipStyleConfig(String color, int thickness, String style) {}
}
