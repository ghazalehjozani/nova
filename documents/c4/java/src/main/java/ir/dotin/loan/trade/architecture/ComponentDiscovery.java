package ir.dotin.loan.trade.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.structurizr.component.ComponentFinder;
import com.structurizr.component.ComponentFinderBuilder;
import com.structurizr.component.ComponentFinderStrategyBuilder;
import com.structurizr.component.matcher.NameSuffixTypeMatcher;
import com.structurizr.component.matcher.RegexTypeMatcher;
import com.structurizr.model.Component;
import com.structurizr.model.Container;

import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Tags;
import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Technologies;

/**
 * Discovers the trade-loan components straight from compiled bytecode using the official {@code structurizr-component}
 * finder (BCEL-based, no Spring context, no classloading).
 *
 * <p>Replaces the previous hand-rolled ClassGraph scanner. Each strategy matches a concern by name-suffix or package
 * regex and stamps a distinct {@code technology}; a second pass derives the C4 tags from that technology (tagging logic
 * stays fully under our control, decoupled from any matcher quirk). Component-to-component relationships are inferred
 * automatically by the finder.
 */
public class ComponentDiscovery {

    private final String basePackage;

    public ComponentDiscovery(String basePackage) {
        this.basePackage = basePackage;
    }

    /** Populate the given container with discovered components, then tag them. */
    public void discover(Container container, Path reactorRoot) {
        List<Path> classDirs = findClassDirectories(reactorRoot);
        System.out.println("   📦 Class directories scanned: " + classDirs.size());
        classDirs.forEach(d -> System.out.println("      - " + d));

        if (classDirs.isEmpty()) {
            System.err.println("   ⚠️  No target/classes dirs with " + basePackage
                    + " found. Did the reactor compile (use -am)? Components will be empty.");
            return;
        }

        var builder = new ComponentFinderBuilder().forContainer(container);
        for (Path dir : classDirs) {
            builder.fromClasses(dir.toString());
        }

        // --- one strategy per concern; distinct technology drives the later tagging pass ---
        addStrategy(builder, new NameSuffixTypeMatcher("Controller"), Technologies.SPRING_REST);
        addStrategy(builder, new NameSuffixTypeMatcher("CommandHandler"), Technologies.COMMAND_HANDLER);
        addStrategy(builder, new NameSuffixTypeMatcher("QueryHandler"), Technologies.QUERY_HANDLER);
        addStrategy(builder, new NameSuffixTypeMatcher("RepositoryAdapter"), Technologies.SPRING_DATA_JPA);
        addStrategy(builder, new NameSuffixTypeMatcher("QueryAdapter"), Technologies.SPRING_DATA_JPA);
        addStrategy(builder, new NameSuffixTypeMatcher("OutboxHandler"), Technologies.OUTBOX_PATTERN);
        addStrategy(builder, new NameSuffixTypeMatcher("Consumer"), Technologies.SPRING_KAFKA);
        addStrategy(builder, new NameSuffixTypeMatcher("McpTools"), Technologies.MCP_TOOL);
        // FCB corridor clients (Kafka + Artemis transports)
        addStrategy(builder, new NameSuffixTypeMatcher("KafkaAdapter"), Technologies.FCB_CLIENT);
        addStrategy(builder, new NameSuffixTypeMatcher("Client"), Technologies.FCB_CLIENT);
        // Reconciliation engine — DRIVEN package only (the driving ops REST controller is already a Controller)
        addStrategy(
                builder,
                new RegexTypeMatcher(".*\\.adapters\\.driven\\.reconciliation\\..*"),
                Technologies.RECON_COMPONENT);
        // Domain aggregate roots (explicit allowlist) + domain services — anchored so e.g.
        // TradeLoanFacilityRepositoryAdapter (claimed by the repository strategy) cannot also match.
        addStrategy(
                builder,
                new RegexTypeMatcher(".*\\.(TradeLoanFacility|TradeLoanArrangement|TradeLoanType"
                        + "|InstallmentSchedule|TradeLoanApplication|TradeSanctionedLoan)$"),
                Technologies.DDD_AGGREGATE);
        addStrategy(builder, new RegexTypeMatcher(".*\\.core\\.domain\\..*Service$"), Technologies.DDD_SERVICE);

        try {
            ComponentFinder finder = builder.build();
            finder.run();
        } catch (Exception e) {
            System.err.println("   ⚠️  Component discovery error: " + e.getMessage());
            e.printStackTrace();
        }

        tagComponents(container);
        System.out.println(
                "   🔍 Total discovered: " + container.getComponents().size() + " components");
    }

    private void addStrategy(ComponentFinderBuilder builder, Object matcher, String technology) {
        var strategy = new ComponentFinderStrategyBuilder()
                .matchedBy((com.structurizr.component.matcher.TypeMatcher) matcher)
                .withTechnology(technology)
                .build();
        builder.withStrategy(strategy);
    }

    /** Derive C4 tags from the technology each strategy stamped (+ a couple of name heuristics). */
    private void tagComponents(Container container) {
        for (Component c : container.getComponents()) {
            String tech = c.getTechnology() == null ? "" : c.getTechnology();
            String name = c.getName() == null ? "" : c.getName();

            if (Technologies.SPRING_REST.equals(tech)) {
                c.addTags(Tags.CONTROLLER, name.toLowerCase().contains("query") ? Tags.QUERY : Tags.COMMAND);
            } else if (Technologies.COMMAND_HANDLER.equals(tech)) {
                c.addTags(Tags.HANDLER, Tags.COMMAND);
                if (name.startsWith("Compensate")) c.addTags(Tags.COMPENSATION);
            } else if (Technologies.QUERY_HANDLER.equals(tech)) {
                c.addTags(Tags.HANDLER, Tags.QUERY);
            } else if (Technologies.SPRING_DATA_JPA.equals(tech)) {
                c.addTags(Tags.REPOSITORY, name.endsWith("QueryAdapter") ? Tags.QUERY : Tags.PERSISTENCE);
            } else if (Technologies.OUTBOX_PATTERN.equals(tech)) {
                c.addTags(Tags.OUTBOX, Tags.MESSAGING);
            } else if (Technologies.SPRING_KAFKA.equals(tech)) {
                c.addTags(Tags.CONSUMER, Tags.MESSAGING);
            } else if (Technologies.MCP_TOOL.equals(tech)) {
                c.addTags(Tags.MCP);
            } else if (Technologies.FCB_CLIENT.equals(tech)) {
                c.addTags(Tags.CLIENT, Tags.EXTERNAL);
            } else if (Technologies.RECON_COMPONENT.equals(tech)) {
                c.addTags(Tags.RECONCILIATION);
            } else if (Technologies.DDD_AGGREGATE.equals(tech)) {
                c.addTags(Tags.DOMAIN, Tags.AGGREGATE);
            } else if (Technologies.DDD_SERVICE.equals(tech)) {
                c.addTags(Tags.DOMAIN, Tags.SERVICE);
            }
        }
    }

    /** All {@code <module>/target/classes} dirs under the reactor that contain the base package. */
    private List<Path> findClassDirectories(Path reactorRoot) {
        var dirs = new ArrayList<Path>();
        if (reactorRoot == null || !Files.isDirectory(reactorRoot)) return dirs;

        String pkgPath = basePackage.replace('.', '/');
        try (Stream<Path> walk = Files.walk(reactorRoot)) {
            walk.filter(Files::isDirectory)
                    .filter(p -> p.endsWith(Path.of("target", "classes")))
                    .filter(p -> Files.isDirectory(p.resolve(pkgPath)))
                    .sorted()
                    .forEach(dirs::add);
        } catch (IOException e) {
            System.err.println("   ⚠️  Failed to walk reactor for class dirs: " + e.getMessage());
        }
        return dirs;
    }
}
