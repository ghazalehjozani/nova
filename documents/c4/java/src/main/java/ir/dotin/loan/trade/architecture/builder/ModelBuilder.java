package ir.dotin.loan.trade.architecture.builder;

import java.util.Map;

import com.structurizr.model.*;

import ir.dotin.loan.trade.architecture.ScannedComponent;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.UsesConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Containers;
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

    public void buildComponents(Container container, Map<String, ScannedComponent> scannedComponents) {
        scannedComponents.values().forEach(scanned -> {
            if (container.getComponentWithName(scanned.name()) != null) return;

            var component = container.addComponent(scanned.name(), scanned.description(), scanned.technology());
            if (component == null) return;

            scanned.tags().forEach(tag -> {
                if (tag != null && !tag.isBlank()) component.addTags(tag);
            });
        });
    }

    public void buildComponentRelationships(Container container, Map<String, ScannedComponent> scannedComponents) {
        var system = container.getSoftwareSystem();
        var postgres = system.getContainerWithName(Containers.POSTGRESQL_DATABASE);
        var kafka = system.getContainerWithName(Containers.KAFKA);

        scannedComponents.values().forEach(scanned -> {
            var component = container.getComponentWithName(scanned.name());
            if (component == null) return;

            buildInternalDependencies(container, component, scanned);
            buildInfrastructureRelationships(component, scanned, postgres, kafka);
            buildExternalClientRelationships(component, scanned);
        });
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
                cfg.uses().forEach(uses -> addContainerRelationship(mainSystem, container, uses));
            }
        });
    }

    private void addContainerRelationship(SoftwareSystem mainSystem, Container source, UsesConfig uses) {
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

    private void buildInternalDependencies(Container container, Component component, ScannedComponent scanned) {
        if (scanned.dependencies() == null) return;
        scanned.dependencies().forEach(dep -> {
            if (dep == null || dep.isBlank()) return;
            var target = container.getComponentWithName(dep);
            if (target != null && !component.equals(target) && !hasRelationship(component, target)) {
                component.uses(target, "Uses");
            }
        });
    }

    private void buildInfrastructureRelationships(
            Component component, ScannedComponent scanned, Container postgres, Container kafka) {
        if (scanned.isRepository() && postgres != null && !hasRelationship(component, postgres)) {
            component.uses(postgres, "Reads/Writes", Technologies.JDBC);
        }
        if (scanned.isOutbox() && kafka != null && !hasRelationship(component, kafka)) {
            component.uses(kafka, "Publishes events", Technologies.KAFKA_PROTOCOL);
        }
        if (scanned.isConsumer() && kafka != null && !hasRelationship(component, kafka)) {
            component.uses(kafka, "Consumes from", Technologies.KAFKA_PROTOCOL);
        }
    }

    private void buildExternalClientRelationships(Component component, ScannedComponent scanned) {
        if (!scanned.isClient()) return;
        var targetSystemName = inferExternalSystemFromComponentName(scanned.name());
        if (targetSystemName != null) {
            var system = model.getSoftwareSystemWithName(targetSystemName);
            if (system != null && !hasRelationship(component, system)) {
                component.uses(system, "Calls", Technologies.HTTP_REST);
            }
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
