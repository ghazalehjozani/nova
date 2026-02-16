package ir.dotin.loan.trade.e2e.fixture.builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.CollateralCalculationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LifeInsurancePaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.LoanSecondaryType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.SectionType;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.AmountRangeDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.CollateralPolicyDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.GracePeriodPolicyDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.InstallmentPolicyDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.InterestPolicyDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.LoanDurationRangeDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.PenaltyPolicyDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.RegulatoryCompliancePolicyDto;
import ir.dotin.loan.trade.adapters.driving.rest.command.dto.DefineTradeLoanArrangementRequest.RepaymentPriorityPolicyDto;

public final class ArrangementRequestBuilder {

    private String code = "E2E-ARR-" + UUID.randomUUID().toString().substring(0, 8);
    private String title = "E2E Test Arrangement";
    private String currencyType = "IRR";
    private BigDecimal minAmount = new BigDecimal("1000");
    private BigDecimal maxAmount = new BigDecimal("100000000");
    private Integer minDurationMonths = 1;
    private Integer maxDurationMonths = 24;
    private PartyType partyType = PartyType.REAL;
    private BigDecimal interestRate = new BigDecimal("20.000000");
    private BigDecimal penaltyRate = new BigDecimal("2.000000");
    private DisbursementType disbursementType = DisbursementType.PROGRESSIVE;

    private ArrangementRequestBuilder() {}

    public static ArrangementRequestBuilder defaults() {
        return new ArrangementRequestBuilder();
    }

    public ArrangementRequestBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public ArrangementRequestBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public ArrangementRequestBuilder withAmountRange(BigDecimal min, BigDecimal max) {
        this.minAmount = min;
        this.maxAmount = max;
        return this;
    }

    public ArrangementRequestBuilder withInterestRate(BigDecimal rate) {
        this.interestRate = rate;
        return this;
    }

    public ArrangementRequestBuilder withDisbursementType(DisbursementType type) {
        this.disbursementType = type;
        return this;
    }

    public DefineTradeLoanArrangementRequest build() {
        return new DefineTradeLoanArrangementRequest(
                code,
                title,
                currencyType,
                new AmountRangeDto(minAmount, maxAmount),
                new LoanDurationRangeDto(minDurationMonths, maxDurationMonths),
                partyType,
                List.of("1", "2", "3"),
                0,
                false,
                LifeInsurancePaymentType.NONE,
                LoanSecondaryType.NONE,
                SectionType.NONE,
                "2-1",
                new InterestPolicyDto(
                        interestRate,
                        new BigDecimal("-100.000000"),
                        new BigDecimal("100.000000"),
                        "10000",
                        "2000",
                        true),
                new PenaltyPolicyDto(
                        penaltyRate,
                        new BigDecimal("6.000000"),
                        "50000",
                        PenaltyPaymentType.INSTALLMENT_PENALTY_PAYMENT),
                new InstallmentPolicyDto(1, "2000", "1000", InstallmentPaymentType.GRADUAL),
                new GracePeriodPolicyDto(10, 30, "10000"),
                new RepaymentPriorityPolicyDto(1, 2, 3, 4, 5, 6, false),
                new RegulatoryCompliancePolicyDto(365, 30, 90),
                new CollateralPolicyDto(0, Set.of(), CollateralCalculationType.BASED_ON_PRINCIPAL),
                disbursementType,
                Map.of());
    }
}
