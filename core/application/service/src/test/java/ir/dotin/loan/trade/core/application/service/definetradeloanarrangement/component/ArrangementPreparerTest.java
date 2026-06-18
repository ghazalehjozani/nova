package ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.formula.ValidateFormulaInFcbPort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.service.configuration.FormulaCorridorProperties;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ArrangementPreparerTest {

    @Mock
    private TradeLoanArrangementRepository repository;

    @Mock
    private FormulaQueryService formulaQueryService;

    @Mock
    private EconomicalSectorLoader economicalSectorLoader;

    @Mock
    private ValidateFormulaInFcbPort validateFormulaInFcbPort;

    @Mock
    private EconomicSector economicSector;

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final FormulaCorridorProperties properties = new FormulaCorridorProperties();

    private ArrangementPreparer preparer;
    private DefineTradeLoanArrangementCommand command;

    @BeforeEach
    void setUp() {
        preparer = new ArrangementPreparer(
                repository,
                formulaQueryService,
                economicalSectorLoader,
                validateFormulaInFcbPort,
                properties,
                meterRegistry);

        command = mock(DefineTradeLoanArrangementCommand.class, RETURNS_DEEP_STUBS);
        when(command.interestPolicy().interestFormula()).thenReturn("interest");
        when(command.interestPolicy().refundFormula()).thenReturn("refund");
        when(command.penaltyPolicy().formula()).thenReturn("penalty");
        when(command.installmentPolicy().installmentFormula()).thenReturn("installment");
        when(command.installmentPolicy().interestComponentFormula()).thenReturn("interestComponent");
        when(command.gracePeriodPolicy().formula()).thenReturn("grace");
        when(command.code().value()).thenReturn("100");

        when(formulaQueryService.exists(anyString())).thenReturn(true);
        when(repository.existsByCode(any())).thenReturn(false);
        when(economicalSectorLoader.loadForArrangement(command)).thenReturn(Result.success(economicSector));
    }

    @Test
    void flagOffDoesNotCallFcbAndSucceeds() {
        properties.setValidateInFcbEnabled(false);

        Result<ArrangementPreparation> result = preparer.prepare(command);

        assertThat(result.isSuccess()).isTrue();
        verify(validateFormulaInFcbPort, never()).validateFormulaInFcb(anyString());
    }

    @Test
    void flagOnCallsFcbPerDistinctFormulaAndSucceeds() {
        properties.setValidateInFcbEnabled(true);
        when(validateFormulaInFcbPort.validateFormulaInFcb(anyString())).thenReturn(Result.success());

        Result<ArrangementPreparation> result = preparer.prepare(command);

        assertThat(result.isSuccess()).isTrue();
        verify(validateFormulaInFcbPort, times(6)).validateFormulaInFcb(anyString());
    }

    @Test
    void fcbValidationFailureDoesNotFailArrangement() {
        properties.setValidateInFcbEnabled(true);
        when(validateFormulaInFcbPort.validateFormulaInFcb(anyString()))
                .thenReturn(Result.failure(CoreBankingErrors.FORMULA_INVALID_IN_FCB, "x", "bad"));

        Result<ArrangementPreparation> result = preparer.prepare(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(meterRegistry.find("formula.fcb.validate.divergence").counters())
                .isNotEmpty();
    }

    @Test
    void fcbTransportErrorDoesNotFailArrangement() {
        properties.setValidateInFcbEnabled(true);
        when(validateFormulaInFcbPort.validateFormulaInFcb(anyString()))
                .thenThrow(new RuntimeException("transport down"));

        Result<ArrangementPreparation> result = preparer.prepare(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(meterRegistry.find("formula.fcb.validate.error").counters()).isNotEmpty();
    }
}
