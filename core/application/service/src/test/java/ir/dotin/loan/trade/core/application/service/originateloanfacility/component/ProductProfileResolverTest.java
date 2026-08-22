package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.time.Period;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.ScheduleSource;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPeriod;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ProductProfileResolver — the wrong-endpoint guard")
@SuppressWarnings("NullAway")
class ProductProfileResolverTest {

    private final ProductProfileResolver resolver = new ProductProfileResolver();

    @Test
    @DisplayName("an equal-instalment product resolves on the system-sourced use case")
    void equalInstallmentProductResolvesForSystemSource() {
        Result<ProductProfile> result = resolver.resolveFor(
                contextFor(DisbursementType.LUMP_SUM, InstallmentPaymentType.SCHEDULED), ScheduleSource.SYSTEM);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap().installmentScheduleType()).isEqualTo(InstallmentScheduleType.EQUAL_INSTALLMENTS);
    }

    @Test
    @DisplayName("a single-instalment product also resolves on the system-sourced use case")
    void singleInstallmentProductResolvesForSystemSource() {
        Result<ProductProfile> result = resolver.resolveFor(
                contextFor(DisbursementType.LUMP_SUM, InstallmentPaymentType.ONE_TIME), ScheduleSource.SYSTEM);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap().installmentScheduleType()).isEqualTo(InstallmentScheduleType.SINGLE_INSTALLMENT);
    }

    @Test
    @DisplayName("a gradual product resolves on the user-sourced use case")
    void gradualProductResolvesForUserSource() {
        Result<ProductProfile> result = resolver.resolveFor(
                contextFor(DisbursementType.LUMP_SUM, InstallmentPaymentType.GRADUAL),
                ScheduleSource.USER_ON_ORIGINATION);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap().installmentScheduleType()).isEqualTo(InstallmentScheduleType.GRADUAL_INSTALLMENTS);
    }

    @Test
    @DisplayName("a gradual product submitted to the system-sourced use case is rejected as a use-case mismatch")
    void gradualProductRejectedOnSystemSourcedUseCase() {
        Result<ProductProfile> result = resolver.resolveFor(
                contextFor(DisbursementType.LUMP_SUM, InstallmentPaymentType.GRADUAL), ScheduleSource.SYSTEM);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.err().orElseThrow().notification().toString())
                .contains("PRODUCT_NOT_SERVED_BY_THIS_USE_CASE");
    }

    @Test
    @DisplayName("a single-instalment product submitted to the user-sourced use case is rejected")
    void singleInstallmentProductRejectedOnUserSourcedUseCase() {
        Result<ProductProfile> result = resolver.resolveFor(
                contextFor(DisbursementType.LUMP_SUM, InstallmentPaymentType.ONE_TIME),
                ScheduleSource.USER_ON_ORIGINATION);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.err().orElseThrow().notification().toString())
                .contains("PRODUCT_NOT_SERVED_BY_THIS_USE_CASE");
    }

    @Test
    @DisplayName("an illegal combination fails as an illegal product, not as a use-case mismatch")
    void illegalCombinationFailsAsIllegalProduct() {
        Result<ProductProfile> result = resolver.resolveFor(
                contextFor(DisbursementType.PROGRESSIVE, InstallmentPaymentType.ONE_TIME), ScheduleSource.SYSTEM);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.err().orElseThrow().notification().toString())
                .contains("PRODUCT_PROFILE_COMBINATION_ILLEGAL");
    }

    private FacilityOriginationContext contextFor(
            DisbursementType disbursementType, InstallmentPaymentType paymentType) {

        TradeLoanArrangement arrangement = mock(TradeLoanArrangement.class);
        when(arrangement.getDisbursementType()).thenReturn(disbursementType);
        when(arrangement.getInstallmentPolicy()).thenReturn(installmentPolicy(paymentType));

        return new FacilityOriginationContext(arrangement, null, List.of());
    }

    private InstallmentPolicy installmentPolicy(InstallmentPaymentType paymentType) {
        return InstallmentPolicy.of(
                        InstallmentPeriod.of(Period.ofMonths(1)).unwrap(),
                        FormulaId.of("installment"),
                        FormulaId.of("interest-component"),
                        paymentType)
                .unwrap();
    }
}
