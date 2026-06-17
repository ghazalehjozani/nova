package ir.dotin.loan.trade.architecture.builder;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.structurizr.model.*;
import com.structurizr.view.*;

import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.WorkflowConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConfig.WorkflowStepConfig;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Containers;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Systems;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Views;

public class ViewBuilder {

    private static final int MAX_ELEMENTS_PER_VIEW = 30;

    private final ViewSet views;
    private final SoftwareSystem mainSystem;
    private final Container mainContainer;
    private final List<WorkflowConfig> workflows;

    public ViewBuilder(
            ViewSet views, SoftwareSystem mainSystem, Container mainContainer, List<WorkflowConfig> workflows) {
        this.views = views;
        this.mainSystem = mainSystem;
        this.mainContainer = mainContainer;
        this.workflows = workflows == null ? List.of() : workflows;
    }

    public void buildAllViews() {
        buildSystemContextView();
        buildContainerView();
        buildComponentViews();
        buildLayerViews();
        buildFlowViews();
        buildWorkflowViews();
    }

    private void buildSystemContextView() {
        var view = views.createSystemContextView(
                mainSystem,
                Views.SYSTEM_CONTEXT,
                "System context showing Trade Loan Service and external dependencies");
        view.addAllElements();
        view.enableAutomaticLayout(AutomaticLayout.RankDirection.TopBottom, 300, 300, 200, false);
        view.setPaperSize(PaperSize.A4_Landscape);
    }

    private void buildContainerView() {
        var view = views.createContainerView(
                mainSystem, Views.CONTAINERS, "Container diagram showing application and infrastructure");
        view.addAllElements();
        view.enableAutomaticLayout(AutomaticLayout.RankDirection.TopBottom, 300, 300, 200, false);
        view.setPaperSize(PaperSize.A4_Landscape);
    }

    private void buildComponentViews() {
        var allComponents = mainContainer.getComponents().stream().toList();
        int total = allComponents.size();

        if (total <= MAX_ELEMENTS_PER_VIEW) {
            var view = views.createComponentView(mainContainer, Views.COMPONENTS, "All components");
            allComponents.forEach(view::add);
            view.enableAutomaticLayout(AutomaticLayout.RankDirection.TopBottom, 300, 300, 200, false);
            view.setPaperSize(PaperSize.A3_Landscape);
        } else {
            int pages = (total + MAX_ELEMENTS_PER_VIEW - 1) / MAX_ELEMENTS_PER_VIEW;
            for (int p = 0; p < pages; p++) {
                String key = p == 0 ? Views.COMPONENTS : Views.COMPONENTS + "_" + (p + 1);
                var view = views.createComponentView(
                        mainContainer, key, "Components (page " + (p + 1) + "/" + pages + ")");
                int start = p * MAX_ELEMENTS_PER_VIEW;
                int end = Math.min(start + MAX_ELEMENTS_PER_VIEW, total);
                for (int i = start; i < end; i++) {
                    view.add(allComponents.get(i));
                }
                view.enableAutomaticLayout(AutomaticLayout.RankDirection.TopBottom, 300, 300, 200, false);
                view.setPaperSize(PaperSize.A3_Landscape);
            }
        }
    }

    private void buildLayerViews() {
        createFilteredView(Views.CONTROLLERS, "REST controllers", Set.of(ArchitectureConstants.Tags.CONTROLLER));
        createFilteredView(Views.HANDLERS, "Command and query handlers", Set.of(ArchitectureConstants.Tags.HANDLER));
        createFilteredView(Views.DOMAIN_MODEL, "Domain layer", Set.of(ArchitectureConstants.Tags.DOMAIN));
        createFilteredView(Views.MCP, "MCP driving adapter (read + ops tools)", Set.of(ArchitectureConstants.Tags.MCP));

        var repoView = createFilteredView(
                Views.REPOSITORIES, "Repository adapters", Set.of(ArchitectureConstants.Tags.REPOSITORY));
        addContainer(repoView, Containers.POSTGRESQL_DATABASE);

        var reconView = createFilteredView(
                Views.RECONCILIATION,
                "Nova ↔ FCB reconciliation engine",
                Set.of(ArchitectureConstants.Tags.RECONCILIATION));
        addContainer(reconView, Containers.POSTGRESQL_DATABASE);

        createFilteredView(Views.EXTERNAL_CLIENTS, "FCB corridor clients", Set.of(ArchitectureConstants.Tags.CLIENT));

        var msgView = createFilteredView(
                Views.MESSAGING,
                "Kafka consumers and outbox handlers",
                Set.of(ArchitectureConstants.Tags.CONSUMER, ArchitectureConstants.Tags.OUTBOX));
        addContainer(msgView, Containers.KAFKA);
        addContainer(msgView, Containers.ARTEMIS);
    }

    private void buildFlowViews() {
        createFlowView(
                Views.COMMAND_FLOW,
                "CQRS Command flow",
                Set.of(
                        ArchitectureConstants.Tags.CONTROLLER,
                        ArchitectureConstants.Tags.HANDLER,
                        ArchitectureConstants.Tags.REPOSITORY,
                        ArchitectureConstants.Tags.DOMAIN),
                c -> !c.getTagsAsSet().contains(ArchitectureConstants.Tags.QUERY));

        createFlowView(
                Views.QUERY_FLOW,
                "CQRS Query flow",
                Set.of(
                        ArchitectureConstants.Tags.CONTROLLER,
                        ArchitectureConstants.Tags.HANDLER,
                        ArchitectureConstants.Tags.REPOSITORY),
                c -> c.getTagsAsSet().contains(ArchitectureConstants.Tags.QUERY));

        createFlowView(
                Views.WORKFLOW_FLOW,
                "Workflow orchestration flow",
                Set.of(
                        ArchitectureConstants.Tags.CONTROLLER,
                        ArchitectureConstants.Tags.HANDLER,
                        ArchitectureConstants.Tags.CLIENT,
                        ArchitectureConstants.Tags.CONSUMER,
                        ArchitectureConstants.Tags.OUTBOX),
                _ -> true);
    }

    private void buildWorkflowViews() {
        // Combined: every workflow step (WORKFLOW-tagged) + the orchestrators + compensation handlers + durable store.
        var combined = views.createComponentView(
                mainContainer,
                Views.WORKFLOW,
                "Durable workflow orchestration — orchestrators, steps, compensation and persistent state");
        mainContainer.getComponents().stream()
                .filter(c -> c.getTagsAsSet().contains(ArchitectureConstants.Tags.WORKFLOW))
                .limit(MAX_ELEMENTS_PER_VIEW)
                .forEach(combined::add);
        for (WorkflowConfig wf : workflows) {
            addComponentByType(combined, wf.orchestratorType());
            addComponentByType(combined, wf.compensationType());
        }
        addContainer(combined, Containers.POSTGRESQL_DATABASE);
        addExternalSystem(combined, Systems.FCB_CORE_BANKING);
        combined.enableAutomaticLayout(AutomaticLayout.RankDirection.TopBottom, 300, 300, 200, false);
        combined.setPaperSize(PaperSize.A3_Landscape);

        // Per-workflow detail views (issue-contract + the disbursements), each showing the ordered steps.
        for (WorkflowConfig wf : workflows) {
            var view = views.createComponentView(
                    mainContainer,
                    wf.viewKey(),
                    wf.name() + (wf.durable() ? " — durable workflow" : " — single-write workflow"));
            addComponentByType(view, wf.orchestratorType());
            for (WorkflowStepConfig step : wf.steps()) {
                var stepComponent = mainContainer.getComponentWithName(ModelBuilder.stepComponentName(wf, step));
                if (stepComponent != null) view.add(stepComponent);
            }
            addComponentByType(view, wf.compensationType());
            addContainer(view, Containers.POSTGRESQL_DATABASE);
            addExternalSystem(view, Systems.FCB_CORE_BANKING);
            view.enableAutomaticLayout(AutomaticLayout.RankDirection.LeftRight, 400, 300, 200, false);
            view.setPaperSize(PaperSize.A4_Landscape);
        }
    }

    private void addComponentByType(ComponentView view, String type) {
        var component = ModelBuilder.findComponentByType(mainContainer, type);
        if (component != null) view.add(component);
    }

    private void addExternalSystem(ComponentView view, String systemName) {
        var system = mainSystem.getModel().getSoftwareSystemWithName(systemName);
        if (system != null) view.add(system);
    }

    private ComponentView createFilteredView(String key, String description, Set<String> requiredTags) {
        var view = views.createComponentView(mainContainer, key, description);
        mainContainer.getComponents().stream()
                .filter(c ->
                        requiredTags.stream().anyMatch(tag -> c.getTagsAsSet().contains(tag)))
                .limit(MAX_ELEMENTS_PER_VIEW)
                .forEach(view::add);
        view.enableAutomaticLayout(AutomaticLayout.RankDirection.TopBottom, 300, 300, 200, false);
        view.setPaperSize(PaperSize.A4_Landscape);
        return view;
    }

    private void createFlowView(String key, String description, Set<String> includeTags, Predicate<Component> filter) {
        var view = views.createComponentView(mainContainer, key, description);
        mainContainer.getComponents().stream()
                .filter(c ->
                        includeTags.stream().anyMatch(tag -> c.getTagsAsSet().contains(tag)))
                .filter(filter)
                .limit(MAX_ELEMENTS_PER_VIEW)
                .forEach(view::add);
        view.enableAutomaticLayout(AutomaticLayout.RankDirection.LeftRight, 400, 300, 200, false);
        view.setPaperSize(PaperSize.A4_Landscape);
    }

    private void addContainer(ComponentView view, String containerName) {
        var container = mainSystem.getContainerWithName(containerName);
        if (container != null) {
            view.add(container);
        }
    }
}
