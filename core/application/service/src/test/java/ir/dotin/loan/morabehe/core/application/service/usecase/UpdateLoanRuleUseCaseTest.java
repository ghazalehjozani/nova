package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.core.application.service.command.config.BaseCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanRuleAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheUpdateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanRuleNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.impl.UpdateLoanRuleUseCaseImpl;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.service.MorabeheLoanRuleUpdateService;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UpdateLoanRuleUseCaseTest {

    @Mock
    private MorabeheLoanRulePersistencePort persistencePort;

    @Mock
    private MorabeheLoanRuleUpdateService updateService;

    @Mock
    private MorabeheLoanRuleAssembler assembler;

    @InjectMocks
    private UpdateLoanRuleUseCaseImpl useCase;

    private MorabeheUpdateLoanRuleCommand command;
    private MorabeheLoanRuleId ruleId;
    private MorabeheLoanRule oldRule;
    private MorabeheLoanRule updatedRule;
    private LoanRuleResponse response;

    @BeforeEach
    void setUp() {
        // Prepare a unique rule ID
        UUID ruleUUID = UUID.randomUUID();
        ruleId = new MorabeheLoanRuleId(ruleUUID);

        // Construct a command to update a loan rule
        // Adjust as needed for your actual command constructor
        BaseCreateLoanRuleCommand baseCommand = mock(BaseCreateLoanRuleCommand.class);
        given(baseCommand.loanRuleId()).willReturn(ruleUUID);
        command = new MorabeheUpdateLoanRuleCommand(baseCommand);

        // Mock domain aggregates and DTOs
        oldRule = mock(MorabeheLoanRule.class);
        updatedRule = mock(MorabeheLoanRule.class);
        response = new LoanRuleResponse(UUID.randomUUID());

        // Default stubbing
        given(assembler.mapToAggregateRoot(command)).willReturn(updatedRule);
        given(persistencePort.findById(ruleId)).willReturn(Optional.of(oldRule));
    }

    @Nested
    @DisplayName("Positive Scenario")
    class PositiveScenario {

        @Test
        @DisplayName("Should update existing loan rule successfully")
        void shouldUpdateLoanRuleSuccessfully() {
            given(assembler.mapToResponse(updatedRule)).willReturn(response);

            // When
            LoanRuleResponse actualResponse = useCase.execute(command);

            // Then
            assertThat(actualResponse).isEqualTo(response);

            then(assembler).should().mapToAggregateRoot(command);
            then(persistencePort).should().findById(ruleId);
            then(updateService).should().update(oldRule, updatedRule);
            then(persistencePort).should().update(oldRule);
            then(persistencePort).should().save(updatedRule);
            then(assembler).should().mapToResponse(updatedRule);
        }
    }

    @Nested
    @DisplayName("Negative Scenario")
    class NegativeScenario {

        @Test
        @DisplayName("Should throw exception if loan rule does not exist")
        void shouldThrowIfLoanRuleNotFound() {
            // Given: No rule found for the given ID
            given(persistencePort.findById(ruleId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(LoanRuleNotFoundException.class)
                    .hasMessageContaining(ruleId.toString());

            then(assembler).should().mapToAggregateRoot(command);
            then(persistencePort).should().findById(ruleId);
            then(updateService).shouldHaveNoInteractions();
            then(persistencePort).should(never()).update(any(MorabeheLoanRule.class));
            then(persistencePort).should(never()).save(any(MorabeheLoanRule.class));
            then(assembler).should(never()).mapToResponse(any());
        }
    }
}
