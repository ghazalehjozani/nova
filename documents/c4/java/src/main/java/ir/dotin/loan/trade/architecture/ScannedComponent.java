package ir.dotin.loan.trade.architecture;

import java.util.List;
import java.util.Objects;

import ir.dotin.loan.trade.architecture.config.ArchitectureConstants.Tags;

public record ScannedComponent(
        String name, String description, String technology, List<String> tags, List<String> dependencies) {

    public ScannedComponent {
        Objects.requireNonNull(name);
        tags = tags != null ? List.copyOf(tags) : List.of();
        dependencies = dependencies != null ? List.copyOf(dependencies) : List.of();
    }

    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag);
    }

    public boolean isController() {
        return hasTag(Tags.CONTROLLER);
    }

    public boolean isHandler() {
        return hasTag(Tags.HANDLER);
    }

    public boolean isRepository() {
        return hasTag(Tags.REPOSITORY);
    }

    public boolean isClient() {
        return hasTag(Tags.CLIENT);
    }

    public boolean isConsumer() {
        return hasTag(Tags.CONSUMER);
    }

    public boolean isOutbox() {
        return hasTag(Tags.OUTBOX);
    }

    public boolean isSaga() {
        return hasTag(Tags.SAGA);
    }

    public boolean isDomain() {
        return hasTag(Tags.DOMAIN);
    }
}
