package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.core.application.service.command.config.BaseCreateLoanRuleCommand;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanRuleAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanRuleCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanRuleResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.impl.CreateLoanRuleUseCaseImpl;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.LoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanRuleValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class CreateLoanRuleUseCaseTest {

    @Mock
    private MorabeheLoanRulePersistencePort persistencePort;

    @Mock
    private MorabeheLoanRuleAssembler assembler;

    @InjectMocks
    private CreateLoanRuleUseCaseImpl useCase;

    private MorabeheCreateLoanRuleCommand command;
    private MorabeheLoanRule mockLoanRule;
    private LoanRule mockInnerLoanRule;
    private LoanRuleResponse mockResponse;

    @BeforeEach
    void setUp() {
        UUID ruleId = UUID.randomUUID();
        BaseCreateLoanRuleCommand baseCommand = mock(BaseCreateLoanRuleCommand.class);
        command = new MorabeheCreateLoanRuleCommand(baseCommand);

        // Mock domain objects
        mockLoanRule = mock(MorabeheLoanRule.class);
        mockInnerLoanRule = mock(LoanRule.class);
        mockResponse = new LoanRuleResponse(ruleId);

        // Common stubs
        given(mockLoanRule.getLoanRule()).willReturn(mockInnerLoanRule);
        given(mockInnerLoanRule.code()).willReturn(LoanRuleCode.valueOf("RULE-ABC"));

        // Mock assembler behavior
        given(assembler.mapToAggregateRoot(command)).willReturn(mockLoanRule);
    }

    @Nested
    @DisplayName("Positive Scenario")
    class PositiveScenario {

        @Test
        @DisplayName("Should successfully create a new loan rule when code does not exist")
        void shouldCreateLoanRuleWhenCodeNotExists() {
            // Given
            given(assembler.mapToResponse(mockLoanRule)).willReturn(mockResponse);
            given(persistencePort.existsByCode(LoanRuleCode.valueOf("RULE-ABC"))).willReturn(false);

            // When: executing the use case
            LoanRuleResponse actualResponse = useCase.execute(command);

            // Then:
            // The response is returned successfully
            assertThat(actualResponse).isSameAs(mockResponse);

            // Verify the interactions
            then(persistencePort).should().existsByCode(LoanRuleCode.valueOf("RULE-ABC"));
            then(mockLoanRule).should().createLoanRule();
            then(persistencePort).should().save(mockLoanRule);
            then(assembler).should().mapToResponse(mockLoanRule);
        }
    }

    @Nested
    @DisplayName("Negative Scenario")
    class NegativeScenario {

        @Test
        @DisplayName("Should throw MorabeheLoanRuleValidationException if code already exists")
        void shouldThrowExceptionIfCodeExists() {
            // Given: The code already exists
            given(persistencePort.existsByCode(LoanRuleCode.valueOf("RULE-ABC"))).willReturn(true);

            // When / Then: Expect an exception
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(MorabeheLoanRuleValidationException.class)
                    .hasMessageContaining("Duplicate loan rule code");

            // Verify that we do not proceed with creation and saving
            then(persistencePort).should().existsByCode(LoanRuleCode.valueOf("RULE-ABC"));
            then(mockLoanRule).should(never()).createLoanRule();
            then(persistencePort).should(never()).save(mockLoanRule);
            then(assembler).should(never()).mapToResponse(any());
        }
    }

}
