package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

/**
 * One guarantor of a facility as seen by reconciliation, normalised to the common comparison shape used on both sides
 * of the Nova↔FCB corridor: the guarantor's customer number and guarantee percentage. The percentage is carried as a
 * {@link BigDecimal} so the drift comparison can match FCB ("100.0000") against Nova ("100") by value
 * ({@link BigDecimal#compareTo}), ignoring scale.
 *
 * <p>Pure DTO at the outbound-port boundary: FCB's wire shape carries the percentage as a string; the FCB recon adapter
 * parses it into this percentage, and the Nova recon read adapter projects the persisted guarantor parties into the
 * same shape. A {@code null} percentage models a guarantor whose percentage is unknown/unparseable on either side.
 *
 * @param customerNumber the guarantor's customer number (the join key across the two sides).
 * @param guaranteePercentage the guarantee percentage, or {@code null} when absent/unparseable.
 */
public record ReconGuarantor(
        String customerNumber, @Nullable BigDecimal guaranteePercentage) {}
