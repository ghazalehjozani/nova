package ir.dotin.loan.trade.architecture.config;

import java.util.List;
import java.util.Map;

public record ArchitectureConfig(
        WorkspaceConfig workspace,
        List<PersonConfig> persons,
        List<ExternalSystemConfig> externalSystems,
        SystemConfig mainSystem,
        List<ContainerConfig> containers,
        StyleConfig styles,
        List<WorkflowConfig> workflows) {

    public record WorkspaceConfig(String name, String description, String basePackage) {}

    public record PersonConfig(String name, String description, List<String> tags, List<UsesConfig> uses) {}

    public record ExternalSystemConfig(String name, String description, List<String> tags, List<UsesConfig> uses) {}

    public record SystemConfig(String name, String description, List<String> tags) {}

    public record ContainerConfig(
            String name, String description, String technology, List<String> tags, List<UsesConfig> uses) {}

    public record UsesConfig(String target, String description, String technology) {}

    public record StyleConfig(
            Map<String, ElementStyleConfig> elements, Map<String, RelationshipStyleConfig> relationships) {}

    public record ElementStyleConfig(String background, String color, String shape, String border) {}

    public record RelationshipStyleConfig(String color, int thickness, String style) {}

    /**
     * A curated description of a durable workflow (saga) orchestration. The orchestrator handler itself is discovered
     * automatically (a {@code *CommandHandler}); this config adds its ordered steps + compensation + durability, which
     * auto-discovery cannot represent because step classes collide on simple name across use cases (e.g. three
     * different {@code ValidateFacilityStep}).
     */
    public record WorkflowConfig(
            String name,
            String shortName,
            String viewKey,
            String orchestratorType,
            String compensationType,
            boolean durable,
            List<WorkflowStepConfig> steps) {}

    /** A single workflow step. {@code kind} drives the technology label + which infrastructure it touches. */
    public record WorkflowStepConfig(String label, String description, StepKind kind, boolean compensable) {}

    /** Workflow step kinds (mirror the pangaea workflow-api activity types). */
    public enum StepKind {
        READ,
        REMOTE,
        WRITE,
        PUBLISH,
        COMPENSATION
    }
}
