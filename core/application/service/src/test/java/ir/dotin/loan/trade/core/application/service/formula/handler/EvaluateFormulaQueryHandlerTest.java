package ir.dotin.loan.trade.core.application.service.formula.handler;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.platform.formula.api.exception.FormulaNotFoundException;
import ir.dotin.platform.formula.service.cqrs.query.EvaluateFormulaQuery;
import ir.dotin.platform.formula.service.cqrs.query.EvaluateFormulaResult;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.loan.trade.core.application.service.shared.formula.TradeLoanFormulaEvaluationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class EvaluateFormulaQueryHandlerTest {

    @Mock
    private TradeLoanFormulaEvaluationService evaluationService;

    private EvaluateFormulaQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EvaluateFormulaQueryHandler(evaluationService);
    }

    @Test
    void returnsResultWhenEvaluationSucceeds() {
        when(evaluationService.evaluate(eq("interest"), anyMap(), anyMap())).thenReturn(new BigDecimal("42"));

        EvaluateFormulaResult result =
                handler.handle(new EvaluateFormulaQuery(UUID.randomUUID(), "interest", Map.of(), Map.of()));

        assertThat(result.value()).isEqualByComparingTo("42");
        assertThat(result.code()).isEqualTo("interest");
        assertThat(result.fromCache()).isFalse();
    }

    @Test
    void translatesNotFoundToFailureCauseException() {
        when(evaluationService.evaluate(eq("missing"), anyMap(), anyMap()))
                .thenThrow(new FormulaNotFoundException(FormulaId.of("missing")));

        assertThatThrownBy(() ->
                        handler.handle(new EvaluateFormulaQuery(UUID.randomUUID(), "missing", Map.of(), Map.of())))
                .isInstanceOf(FailureCauseException.class)
                .satisfies(ex -> assertThat(((FailureCauseException) ex).failureCause())
                        .isInstanceOf(FailureCause.NotFound.class));
    }
}
