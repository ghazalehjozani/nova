package ir.dotin.loan.trade.core.domain.shared.error;

import ir.dotin.platform.commons.core.error.ErrorCategory;

/*
 * ════════════════════════════════════════════════════════════════════════════════
 *  TRADE LOAN ERROR CATEGORIES — codes 51–60
 * ════════════════════════════════════════════════════════════════════════════════
 *
 *  Issuer: LOAN
 *  Module: trade-loan-api (product-specific, NOT shared with other loan products)
 *
 *  These categories cover domain concerns unique to the Trade Loan product
 *  that are NOT shared with other loan products (Mortgage, Consumer, etc.).
 *
 *  Trade Loan also uses:
 *    - PlatformErrorCategory (01–09) for cross-cutting concerns
 *    - LoanErrorCategory     (31–50) for shared loan domain concerns
 *
 *  Category code ranges reserved for other loan products:
 *    51–60  Trade Loan (this file)
 *    61–65  Reserved for Mortgage Loan
 *    66–70  Reserved for Consumer Loan
 *    71–80  Reserved for future loan products
 *    81–99  Reserved
 *
 *  ┌──────┬───────────────────────────┬────────────────────────────────────────┐
 *  │ Code │ Category                  │ Sequence allocation                   │
 *  ├──────┼───────────────────────────┼────────────────────────────────────────┤
 *  │  51  │ CORE_BANKING_INTEGRATION  │ CoreBankingErrors:  001–050           │
 *  │      │                           │ CoreBankingKafka:   051–080           │
 *  │      │                           │ Reserved:           081–999           │
 *  ├──────┼───────────────────────────┼────────────────────────────────────────┤
 *  │  52  │ TRADE_FACILITY            │ TradeFacilityErrors:  001–100         │
 *  │      │                           │ Reserved:             101–999         │
 *  ├──────┼───────────────────────────┼────────────────────────────────────────┤
 *  │  53  │ Reserved                  │                                       │
 *  │  …   │                           │                                       │
 *  │  60  │ Reserved                  │                                       │
 *  └──────┴───────────────────────────┴────────────────────────────────────────┘
 *
 * ════════════════════════════════════════════════════════════════════════════════
 */

/**
 * Trade Loan product-specific error categories.
 *
 * <p>These categories classify errors unique to the Trade Loan product within the Dotin loan ecosystem. They occupy
 * codes {@code 51–60} and complement the shared
 * {@link ir.dotin.loan.baseloan.core.domain.shared.error.LoanErrorCategory} (codes {@code 31–50}).
 *
 * <p>The Trade Loan product is characterized by its integration with the core banking system (FCB — Facility Core
 * Banking) and trade-specific facility processing rules (Morabehe contracts, trade-specific disbursement, etc.).
 *
 * <h2>When to use Trade vs Shared categories</h2>
 *
 * <ul>
 *   <li><strong>Use {@code LoanErrorCategory}</strong> for errors in shared domain concepts: facility lifecycle,
 *       installment schedule, collateral, sanction, etc.
 *   <li><strong>Use {@code TradeLoanErrorCategory}</strong> for errors specific to trade-loan business logic: FCB
 *       integration, Morabehe processing, trade-specific transaction rules.
 *   <li><strong>Use {@code PlatformErrorCategory}</strong> for cross-cutting concerns: validation, state conflicts,
 *       data access, etc.
 * </ul>
 *
 * <h2>Category Design Rationale</h2>
 *
 * <p>{@link #CORE_BANKING_INTEGRATION} is separate from {@code PlatformErrorCategory.INTEGRATION} because FCB errors
 * are <em>business-level</em> (customer not found, unsupported sector, invalid account) rather than
 * <em>transport-level</em> (timeout, connection refused). Transport errors when calling FCB use
 * {@code PlatformErrorCategory.INTEGRATION}.
 *
 * @since 2.0
 * @see ir.dotin.loan.baseloan.core.domain.shared.error.LoanErrorCategory
 * @see ir.dotin.platform.commons.core.error.PlatformErrorCategory
 */
public enum TradeLoanErrorCategory implements ErrorCategory {

    /**
     * Core banking system (FCB) business-level integration errors.
     *
     * <p>Covers errors returned by the core banking system that carry business meaning, as opposed to transport-level
     * failures. These include customer lookup failures, account validation errors, transaction code issues, and
     * FCB-specific response mapping errors.
     *
     * <p><strong>Distinction from {@code PlatformErrorCategory.INTEGRATION}:</strong>
     *
     * <ul>
     *   <li>{@code INTEGRATION (03)}: FCB is unreachable, timed out, returned HTTP 5xx → transport error
     *   <li>{@code CORE_BANKING_INTEGRATION (51)}: FCB returned a business error (customer not found, unsupported
     *       sector, invalid account number) → business error from FCB
     * </ul>
     *
     * <p>Sequence ranges:
     *
     * <ul>
     *   <li>{@code 001–050}: REST/client-based FCB integration errors
     *   <li>{@code 051–080}: Kafka-based FCB messaging errors (business-level)
     * </ul>
     */
    CORE_BANKING_INTEGRATION(51, "Core banking system (FCB) business-level errors"),

    /**
     * Trade-specific facility processing errors.
     *
     * <p>Covers errors unique to the Trade Loan product's facility processing that do not belong in the shared
     * {@code LoanErrorCategory.FACILITY_LIFECYCLE}. These include Morabehe-specific validation, trade contract rules,
     * trade-specific calculation context errors, and purpose code resolution.
     *
     * <p>If an error concept applies to ALL loan products, it belongs in
     * {@link ir.dotin.loan.baseloan.core.domain.shared.error.LoanErrorCategory#FACILITY_LIFECYCLE} instead.
     */
    TRADE_FACILITY(52, "Trade-specific facility processing errors");

    // Codes 53–60 are reserved for future trade-loan-specific categories.

    private final int code;
    private final String description;

    TradeLoanErrorCategory(int code, String description) {
        this.code = code;
        this.description = description;
        validate();
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String description() {
        return description;
    }
}
