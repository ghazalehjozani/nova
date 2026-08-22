package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.OriginateLoanFacilityApplicationMapper;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.ApplicationNumberStrategySelector;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Guards the regression left behind when {@code StandardScheduleStrategy} was deleted: it used to reject a
 * {@code SCHEDULED} product carrying no instalment count, and base-loan's specification runs too late to catch it.
 */
@DisplayName("FacilityBuilder — a missing instalment count is a business error, not an exception")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"NullAway", "TimeZoneUsage"})
class FacilityBuilderInstallmentCountTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OriginateFacilityCommand command;

    @Mock
    private OriginateLoanFacilityApplicationMapper applicationMapper;

    @Mock
    private ApplicationNumberStrategySelector applicationNumberStrategySelector;

    private FacilityBuilder builder() {
        return new FacilityBuilder(
                applicationMapper,
                applicationNumberStrategySelector,
                Clock.fixed(Instant.EPOCH, java.time.ZoneOffset.UTC));
    }

    @Test
    @DisplayName("a SCHEDULED product with no instalment count fails with INSTALLMENT_COUNT_CANNOT_BE_EMPTY")
    void scheduledProductWithoutCountFailsAsBusinessError() {
        when(command.loanApplication().branch().code()).thenReturn("001");
        when(command.loanApplication().applicationNumber()).thenReturn(null);
        when(command.loanApplication().installmentCount()).thenReturn(null);
        when(applicationMapper.map(any(OriginateFacilityCommand.LoanApplicationDto.class)))
                .thenReturn(mock(TradeLoanApplication.Builder.class, Answers.RETURNS_SELF));

        Result<TradeLoanApplication> result =
                builder().buildApplication(command, context(), scheduledProfile(), mock(ApplicationNumber.class));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.err().orElseThrow().notification().toString()).contains("INSTALLMENT_COUNT_CANNOT_BE_EMPTY");
    }

    @Test
    @DisplayName("a SCHEDULED product with a null-valued instalment count DTO fails the same way, not with an NPE")
    void scheduledProductWithNullValuedCountFailsAsBusinessError() {
        when(command.loanApplication().branch().code()).thenReturn("001");
        when(command.loanApplication().applicationNumber()).thenReturn(null);
        when(command.loanApplication().installmentCount())
                .thenReturn(new OriginateFacilityCommand.InstallmentCountDto(null));
        when(applicationMapper.map(any(OriginateFacilityCommand.LoanApplicationDto.class)))
                .thenReturn(mock(TradeLoanApplication.Builder.class, Answers.RETURNS_SELF));

        assertThatCode(() -> {
                    Result<TradeLoanApplication> result = builder()
                            .buildApplication(command, context(), scheduledProfile(), mock(ApplicationNumber.class));
                    assertThat(result.isFailure()).isTrue();
                    assertThat(result.err().orElseThrow().notification().toString())
                            .contains("INSTALLMENT_COUNT_CANNOT_BE_EMPTY");
                })
                .doesNotThrowAnyException();
    }

    private ProductProfile scheduledProfile() {
        return ProductProfile.of(
                        DisbursementType.LUMP_SUM,
                        InstallmentScheduleType.EQUAL_INSTALLMENTS,
                        InstallmentPaymentType.SCHEDULED)
                .unwrap();
    }

    private FacilityOriginationContext context() {
        return new FacilityOriginationContext(null, null, List.of());
    }
}
