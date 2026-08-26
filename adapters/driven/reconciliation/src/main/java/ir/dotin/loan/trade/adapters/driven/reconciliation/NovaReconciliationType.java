package ir.dotin.loan.trade.adapters.driven.reconciliation;

import ir.dotin.platform.pangaea.commons.core.i18n.LocalizedEnum;

/**
 * The reconciliation types Nova registers with the platform engine, and the single place each type's wire code is
 * written.
 *
 * <p>The platform keeps the type an opaque, open string — it holds no list of types and attaches no meaning to this
 * enum. The constant travels the {@code ReconciliationSource} SPI purely as a label carrier, so the ops surface can
 * render {@code {code,label}} with Persian display text drawn from the ordinary {@code enum.NovaReconciliationType.*}
 * message keys the i18n extractor generates. Adding a type here plus its translation is the whole cost; the platform
 * needs no change.
 *
 * <p>{@link #code()} is the wire value — kebab-case and stable, persisted on every discrepancy row and accepted as
 * input by every reconciliation endpoint. It is deliberately not the constant's {@code name()}: renaming a constant
 * must never change the wire contract or orphan persisted rows.
 */
public enum NovaReconciliationType implements LocalizedEnum<NovaReconciliationType> {

    /** Nova facility status against FCB's legacy loan-file state. */
    FACILITY_STATE("facility-state");

    private final String code;

    NovaReconciliationType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
