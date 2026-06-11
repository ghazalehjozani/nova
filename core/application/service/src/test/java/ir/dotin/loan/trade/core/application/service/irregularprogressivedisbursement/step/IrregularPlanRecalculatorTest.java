package ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.step;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.TransactionConfig;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.Installment;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.entity.InstallmentSchedule;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.service.InstallmentRecalculationService;
import ir.dotin.loan.trade.core.application.service.irregularprogressivedisbursement.workflow.IrregularDisbursementData;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class IrregularPlanRecalculatorTest {

    @Mock
    private InstallmentRecalculationService recalculationService;

    @Mock
    private InstallmentSchedule schedule;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanFacility facility;

    @Mock
    private Installment installment;

    @InjectMocks
    private IrregularPlanRecalculator recalculator;

    @Test
    void trancheMoneyParsesCurrencyAndAmount() {
        IrregularDisbursementData data = data();

        Money tranche = recalculator.trancheMoney(data);

        assertThat(tranche.value()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(tranche.currency()).isEqualTo(CurrencyType.IRR);
    }

    @Test
    void recalculateAndVerifySucceedsWhenPlanMatchesApproved() {
        IrregularDisbursementData data = data();

        when(recalculationService.recalculateForIrregularDisbursement(eq(schedule), eq(facility), any(), eq(null)))
                .thenReturn(Result.success(List.of()));

        Result<List<Installment>> result = recalculator.recalculateAndVerify(facility, schedule, data);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void recalculateAndVerifyFailsOnPlanDrift() {
        IrregularDisbursementData data = data();

        when(recalculationService.recalculateForIrregularDisbursement(eq(schedule), eq(facility), any(), eq(null)))
                .thenReturn(Result.success(List.of(installment)));

        Result<List<Installment>> result = recalculator.recalculateAndVerify(facility, schedule, data);

        assertThat(result.isFailure()).isTrue();
    }

    private static IrregularDisbursementData data() {
        return IrregularDisbursementData.initial(
                UUID.randomUUID(),
                "001",
                TransactionConfig.builder().build(),
                LocalDate.of(2026, 1, 1),
                0L,
                BigDecimal.valueOf(1000),
                "IRR",
                1,
                null,
                List.of());
    }
}
