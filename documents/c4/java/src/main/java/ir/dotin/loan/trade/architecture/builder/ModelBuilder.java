package ir.dotin.loan.trade.architecture.builder;

import com.structurizr.model.*;

import ir.dotin.loan.trade.architecture.config.ArchitectureConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.StepKind;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.UsesConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.WorkflowConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.WorkflowStepConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Containers;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Systems;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Tags;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Technologies;

import static ir.dotin.loan.trade.architecture.config.ArchitectureConstants.inferExternalSystemFromComponentName;

public class ModelBuilder {

    private final Model model;
    private final ArchitectureConfig config;

    public ModelBuilder(Model model, ArchitectureConfig config) {
        this.model = model;
        this.config = config;
        model.setImpliedRelationshipsStrategy(new CreateImpliedRelationshipsUnlessSameRelationshipExistsStrategy());
    }

    public void buildStaticModel() {
        buildExternalSystems();
        buildMainSystem();
        buildContainers();
        buildPersons();
    }

    private void buildExternalSystems() {
        config.externalSystems().forEach(sys -> {
            if (model.getSoftwareSystemWithName(sys.name()) != null) return;
            var system = model.addSoftwareSystem(sys.name(), sys.description());
            if (system != null && sys.tags() != null) {
                sys.tags().forEach(tag -> {
                    if (tag != null && !tag.isBlank()) system.addTags(tag);
                });
            }
        });
    }

    private void buildMainSystem() {
        var sys = config.mainSystem();
        if (model.getSoftwareSystemWithName(sys.name()) != null) return;
        var system = model.addSoftwareSystem(sys.name(), sys.description());
        if (system != null && sys.tags() != null) {
            sys.tags().forEach(tag -> {
                if (tag != null && !tag.isBlank()) system.addTags(tag);
            });
        }
    }

    private void buildContainers() {
        var mainSystem = model.getSoftwareSystemWithName(config.mainSystem().name());
        if (mainSystem == null) return;

        config.containers().forEach(cfg -> {
            if (mainSystem.getContainerWithName(cfg.name()) != null) return;
            var container = mainSystem.addContainer(cfg.name(), cfg.description(), cfg.technology());
            if (container != null && cfg.tags() != null) {
                cfg.tags().forEach(tag -> {
                    if (tag != null && !tag.isBlank()) container.addTags(tag);
                });
            }
        });

        config.containers().forEach(cfg -> {
            var container = mainSystem.getContainerWithName(cfg.name());
            if (container != null && cfg.uses() != null) {
                cfg.uses().forEach(uses -> addOutboundRelationship(mainSystem, container, uses));
            }
        });
    }

    private void buildPersons() {
        var mainSystem = model.getSoftwareSystemWithName(config.mainSystem().name());
        if (mainSystem == null) return;

        config.persons().forEach(cfg -> {
            if (model.getPersonWithName(cfg.name()) != null) return;
            var person = model.addPerson(cfg.name(), cfg.description());
            if (person == null) return;

            if (cfg.tags() != null) {
                cfg.tags().forEach(tag -> {
                    if (tag != null && !tag.isBlank()) person.addTags(tag);
                });
            }

            if (cfg.uses() != null) {
                cfg.uses().forEach(uses -> {
                    var container = mainSystem.getContainerWithName(uses.target());
                    if (container != null && !hasRelationship(person, container)) {
                        person.uses(container, uses.description(), uses.technology());
                    }
                });
            }
        });
    }

    /**
     * Wires inbound external-system relationships (e.g. ESB -> Trade Loan Application). Must run after
     * {@link #buildStaticModel()} so both source systems and target containers exist.
     */
    public void wireExternalSystemUsage() {
        var mainSystem = model.getSoftwareSystemWithName(config.mainSystem().name());
        if (mainSystem == null) return;

        config.externalSystems().forEach(cfg -> {
            if (cfg.uses() == null) return;
            var system = model.getSoftwareSystemWithName(cfg.name());
            if (system == null) return;
            cfg.uses().forEach(uses -> {
                var container = mainSystem.getContainerWithName(uses.target());
                if (container != null && !hasRelationship(system, container)) {
                    system.uses(container, uses.description(), uses.technology());
                }
            });
        });
    }

    /**
     * Adds infrastructure and external-system relationships for discovered components, keyed off the C4 tags assigned
     * during discovery. Component-to-component relationships are already inferred by the structurizr-component finder.
     */
    public void wireComponentInfrastructure(Container container) {
        var system = container.getSoftwareSystem();
        var postgres = system.getContainerWithName(Containers.POSTGRESQL_DATABASE);
        var kafka = system.getContainerWithName(Containers.KAFKA);
        var artemis = system.getContainerWithName(Containers.ARTEMIS);
        var fcb = model.getSoftwareSystemWithName(
                ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Systems.FCB_CORE_BANKING);

        container.getComponents().forEach(component -> {
            var tags = component.getTagsAsSet();

            if (tags.contains(Tags.REPOSITORY) && postgres != null && !hasRelationship(component, postgres)) {
                component.uses(postgres, "Reads/Writes", Technologies.JDBC);
            }
            if (tags.contains(Tags.OUTBOX) && kafka != null && !hasRelationship(component, kafka)) {
                component.uses(kafka, "Publishes events", Technologies.KAFKA_PROTOCOL);
            }
            if (tags.contains(Tags.CONSUMER) && kafka != null && !hasRelationship(component, kafka)) {
                component.uses(kafka, "Consumes from", Technologies.KAFKA_PROTOCOL);
            }
            if (tags.contains(Tags.RECONCILIATION)) {
                if (postgres != null && !hasRelationship(component, postgres)) {
                    component.uses(postgres, "Reads reconciliation state", Technologies.JDBC);
                }
                if (fcb != null && !hasRelationship(component, fcb)) {
                    component.uses(fcb, "Probes / converges", Technologies.CORRIDOR);
                }
            }
            if (tags.contains(Tags.CLIENT)) {
                wireCorridorClient(component, fcb, artemis, kafka);
            }
        });
    }

    private void wireCorridorClient(Component component, SoftwareSystem fcb, Container artemis, Container kafka) {
        var targetName = inferExternalSystemFromComponentName(component.getName());
        if (targetName != null) {
            var target = model.getSoftwareSystemWithName(targetName);
            if (target != null && !hasRelationship(component, target)) {
                component.uses(target, "Calls (corridor)", Technologies.CORRIDOR);
            }
        }
        String lower = component.getName().toLowerCase();
        if (lower.contains("artemis") && artemis != null && !hasRelationship(component, artemis)) {
            component.uses(artemis, "Request/reply (primary)", Technologies.ARTEMIS_JMS);
        }
        if (lower.contains("kafka") && kafka != null && !hasRelationship(component, kafka)) {
            component.uses(kafka, "Request/reply (fallback)", Technologies.KAFKA_PROTOCOL);
        }
    }

    /**
     * Adds the curated workflow internals: ordered step components, their sequence, infra touch-points, durable
     * workflow state, and the compensation handler link. The orchestrator + compensation handlers are reused from
     * auto-discovery (reused by type, not re-added, to avoid duplicate-name collisions).
     */
    public void wireWorkflows(Container container) {
        if (config.workflows() == null) return;
        var system = container.getSoftwareSystem();
        var postgres = system.getContainerWithName(Containers.POSTGRESQL_DATABASE);
        var kafka = system.getContainerWithName(Containers.KAFKA);
        var fcb = model.getSoftwareSystemWithName(Systems.FCB_CORE_BANKING);

        config.workflows().forEach(wf -> {
            var orchestrator = findComponentByType(container, wf.orchestratorType());
            Component previous = orchestrator;

            for (WorkflowStepConfig step : wf.steps()) {
                var name = stepComponentName(wf, step);
                var stepComponent = container.getComponentWithName(name);
                if (stepComponent == null) {
                    stepComponent = container.addComponent(name, step.description(), technologyFor(step.kind()));
                }
                if (stepComponent == null) continue;

                stepComponent.addTags(Tags.WORKFLOW);
                if (step.compensable() || step.kind() == StepKind.COMPENSATION) {
                    stepComponent.addTags(Tags.COMPENSATION);
                }

                if (previous != null && !hasRelationship(previous, stepComponent)) {
                    previous.uses(stepComponent, previous == orchestrator ? "Starts" : "Then");
                }

                if (step.kind() == StepKind.REMOTE && fcb != null && !hasRelationship(stepComponent, fcb)) {
                    stepComponent.uses(fcb, "Calls", Technologies.CORRIDOR);
                }
                if ((step.kind() == StepKind.WRITE || step.kind() == StepKind.PUBLISH)
                        && postgres != null
                        && !hasRelationship(stepComponent, postgres)) {
                    stepComponent.uses(postgres, "Persists", Technologies.JDBC);
                }
                if (step.kind() == StepKind.PUBLISH && kafka != null && !hasRelationship(stepComponent, kafka)) {
                    stepComponent.uses(kafka, "Emits events (outbox)", Technologies.KAFKA_PROTOCOL);
                }
                previous = stepComponent;
            }

            if (wf.durable() && orchestrator != null && postgres != null && !hasRelationship(orchestrator, postgres)) {
                orchestrator.uses(postgres, Technologies.WF_DURABLE_STATE, Technologies.JDBC);
            }
            if (wf.compensationType() != null && orchestrator != null) {
                var compensation = findComponentByType(container, wf.compensationType());
                if (compensation != null && !hasRelationship(orchestrator, compensation)) {
                    orchestrator.uses(compensation, "Compensates on failure");
                }
            }
        });
    }

    /** Stable display name for a curated workflow step — must match what ViewBuilder looks up. */
    public static String stepComponentName(WorkflowConfig wf, WorkflowStepConfig step) {
        return wf.shortName() + " — " + step.label();
    }

    /** Find an auto-discovered component by its source simple type name (component names are humanized). */
    public static Component findComponentByType(Container container, String simpleType) {
        if (simpleType == null) return null;
        return container.getComponents().stream()
                .filter(c -> c.getName() != null
                        && c.getName().replace(" ", "").equalsIgnoreCase(simpleType))
                .findFirst()
                .orElse(null);
    }

    private static String technologyFor(StepKind kind) {
        return switch (kind) {
            case READ -> Technologies.WF_READ_STEP;
            case REMOTE -> Technologies.WF_REMOTE_STEP;
            case WRITE -> Technologies.WF_WRITE_STEP;
            case PUBLISH -> Technologies.WF_PUBLISH_STEP;
            case COMPENSATION -> Technologies.WF_COMPENSATION_STEP;
        };
    }

    private void addOutboundRelationship(SoftwareSystem mainSystem, StaticStructureElement source, UsesConfig uses) {
        if (uses == null || uses.target() == null) return;

        var targetContainer = mainSystem.getContainerWithName(uses.target());
        if (targetContainer != null) {
            if (!hasRelationship(source, targetContainer))
                source.uses(targetContainer, uses.description(), uses.technology());
            return;
        }

        var targetSystem = model.getSoftwareSystemWithName(uses.target());
        if (targetSystem != null && !hasRelationship(source, targetSystem)) {
            source.uses(targetSystem, uses.description(), uses.technology());
        }
    }

    private boolean hasRelationship(StaticStructureElement source, StaticStructureElement target) {
        return source.getRelationships().stream()
                .anyMatch(r -> r.getDestination().equals(target));
    }

    public Container getMainContainer() {
        var mainSystem = model.getSoftwareSystemWithName(config.mainSystem().name());
        return mainSystem != null ? mainSystem.getContainerWithName(Containers.TRADE_LOAN_APPLICATION) : null;
    }

    public SoftwareSystem getMainSystem() {
        return model.getSoftwareSystemWithName(config.mainSystem().name());
    }
}
