package ir.dotin.loan.trade.core.application.query.formula.handler;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.formula.service.cqrs.query.FormulaView;
import ir.dotin.platform.formula.service.cqrs.query.GetFormulaQuery;
import ir.dotin.platform.formula.service.dto.FormulaDto;
import ir.dotin.platform.formula.service.query.FormulaQueryService;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class GetFormulaQueryHandlerTest {

    @Mock
    private FormulaQueryService formulaQueryService;

    private GetFormulaQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetFormulaQueryHandler(formulaQueryService);
    }

    @Test
    void returnsFormulaViewWhenFound() {
        FormulaDto dto = new FormulaDto(
                "interest",
                "approvedAmount * rate",
                Set.of("approvedAmount"),
                List.of(),
                Set.of(),
                "LOAN_FACILITY",
                "interest formula",
                Instant.EPOCH,
                Instant.EPOCH,
                1L);
        when(formulaQueryService.findByCode("interest")).thenReturn(Optional.of(dto));

        FormulaView view = handler.handle(new GetFormulaQuery(UUID.randomUUID(), "interest"));

        assertThat(view.formula()).isEqualTo(dto);
    }

    @Test
    void throwsNotFoundWhenAbsent() {
        when(formulaQueryService.findByCode("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetFormulaQuery(UUID.randomUUID(), "missing")))
                .isInstanceOf(FailureCauseException.class);
    }
}
