package ir.dotin.loan.trade.e2e.fixture.builder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.trade.adapters.driving.contract.dto.DisburseDestinationRequestDto;
import ir.dotin.loan.trade.adapters.driving.contract.dto.FullLoanFacilityLifecycleMessage;
import ir.dotin.loan.trade.adapters.driving.contract.dto.PartyRequestDto;

public final class FullLifecycleMessageBuilder {

    @SuppressWarnings("NullAway.Init") // builder field set via withLoanTypeCode before build()
    private String loanTypeCode;

    @SuppressWarnings("NullAway.Init") // builder field set via withLoanArrangementCode before build()
    private String loanArrangementCode;

    private BigDecimal amount = new BigDecimal("50000000");
    private String currency = "IRR";
    private int durationMonths = 12;
    private int installmentCount = 3;
    private int gracePeriodDays = 10;
    private String customerNumber = "12345678";
    private DisbursementMethod disbursementMethod = DisbursementMethod.IRREGULAR_PROGRESSIVE;

    private FullLifecycleMessageBuilder() {}

    public static FullLifecycleMessageBuilder defaults() {
        return new FullLifecycleMessageBuilder();
    }

    public FullLifecycleMessageBuilder withLoanTypeCode(String code) {
        this.loanTypeCode = code;
        return this;
    }

    public FullLifecycleMessageBuilder withLoanArrangementCode(String code) {
        this.loanArrangementCode = code;
        return this;
    }

    public FullLifecycleMessageBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public FullLifecycleMessageBuilder withInstallmentCount(int count) {
        this.installmentCount = count;
        return this;
    }

    public FullLifecycleMessageBuilder withCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
        return this;
    }

    public FullLoanFacilityLifecycleMessage build() {
        BigDecimal principalPerInstallment =
                amount.divide(BigDecimal.valueOf(installmentCount), 0, java.math.RoundingMode.DOWN);
        BigDecimal lastPrincipal =
                amount.subtract(principalPerInstallment.multiply(BigDecimal.valueOf(installmentCount - 1)));
        BigDecimal interestPerInstallment = new BigDecimal("750000");

        List<FullLoanFacilityLifecycleMessage.InstallmentSpecDto> installments = new java.util.ArrayList<>();
        for (int i = 1; i <= installmentCount; i++) {
            BigDecimal principal = (i == installmentCount) ? lastPrincipal : principalPerInstallment;
            installments.add(new FullLoanFacilityLifecycleMessage.InstallmentSpecDto(
                    i, Instant.now().plus(Duration.ofDays(30L * i)), principal, interestPerInstallment));
        }

        return FullLoanFacilityLifecycleMessage.builder()
                .version(1L)
                .loanTypeCode(loanTypeCode)
                .loanArrangementCode(loanArrangementCode)
                .loanApplication(new FullLoanFacilityLifecycleMessage.LoanApplicationDto(
                        Instant.now(),
                        Set.of(new PartyRequestDto.ApplicantDto(customerNumber)),
                        amount,
                        currency,
                        durationMonths,
                        ApplicantChannel.DIGITAL_BANK,
                        gracePeriodDays,
                        installmentCount,
                        new DisburseDestinationRequestDto.DepositDestinationDto("1.10.1357.60"),
                        "2-1",
                        "1",
                        new FullLoanFacilityLifecycleMessage.RequestReasonDto("0"),
                        "03",
                        "E2E test facility",
                        null,
                        disbursementMethod,
                        new FullLoanFacilityLifecycleMessage.SamatDto("1234567899876543", null, null, null, null, null),
                        "A"))
                .disbursement(new FullLoanFacilityLifecycleMessage.DisbursementDto(amount, LocalDate.now()))
                .installmentSchedulePlan(new FullLoanFacilityLifecycleMessage.InstallmentSchedulePlanDto(installments))
                .collaterals(List.of())
                .confirmType("1")
                .metadata(Map.of())
                .build();
    }
}
