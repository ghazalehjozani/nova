package ir.dotin.loan.trade.adapters.driven.persistance.exception;

public class UnsupportedTypeConversionException extends PersistenceConversionException {
    private final Class<?> sourceType;
    private final Class<?> targetType;

    public UnsupportedTypeConversionException(Class<?> sourceType, Class<?> targetType) {
        super(String.format("Cannot convert from %s to %s", sourceType.getSimpleName(), targetType.getSimpleName()));
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public Class<?> getSourceType() {
        return sourceType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }
}
