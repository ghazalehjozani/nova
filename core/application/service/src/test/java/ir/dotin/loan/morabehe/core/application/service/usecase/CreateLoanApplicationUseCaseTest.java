package ir.dotin.loan.morabehe.core.application.service.usecase;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.loan.baseloan.core.application.service.command.loanapplication.BaseCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanApplicationAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanRuleNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanTypeNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.impl.CreateLoanApplicationUseCaseImpl;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.CreateLoanApplicationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class CreateLoanApplicationUseCaseTest {

    @Mock
    private MorabeheLoanApplicationPersistencePort loanApplicationPersistencePort;

    @Mock
    private CreateLoanApplicationService createService;

    @Mock
    private MorabeheLoanRulePersistencePort loanRulePersistencePort;

    @Mock
    private MorabeheLoanTypePersistencePort loanTypePersistencePort;

    @Mock
    private MorabeheLoanApplicationAssembler assembler;

    @InjectMocks
    private CreateLoanApplicationUseCaseImpl useCase;

    private MorabeheCreateLoanApplicationCommand command;
    private MorabeheLoanApplication mockApplication;
    private LoanApplication mockLoanApplication;
    private MorabeheLoanRule mockLoanRule;
    private MorabeheLoanType mockLoanType;
    private LoanApplicationResponse response;

    private MorabeheLoanRuleId loanRuleId;
    private MorabeheLoanTypeId loanTypeId;

    @BeforeEach
    void setUp() {
        // Setup UUID-based IDs
        UUID ruleUuid = UUID.randomUUID();
        UUID typeUuid = UUID.randomUUID();
        loanRuleId = new MorabeheLoanRuleId(ruleUuid);
        loanTypeId = new MorabeheLoanTypeId(typeUuid);

        // Prepare command input
        BaseCreateLoanApplicationCommand baseCommand = mock(BaseCreateLoanApplicationCommand.class);
        command = new MorabeheCreateLoanApplicationCommand(baseCommand);

        // Mock domain entities
        mockApplication = mock(MorabeheLoanApplication.class);
        mockLoanApplication = mock(LoanApplication.class);
        mockLoanRule = mock(MorabeheLoanRule.class);
        mockLoanType = mock(MorabeheLoanType.class);

        response = new LoanApplicationResponse(UUID.randomUUID());

        // Common stubs
        given(mockApplication.loanApplication()).willReturn(mockLoanApplication);
        given(mockLoanApplication.loanRuleId()).willReturn(loanRuleId);
    }

    @Nested
    @DisplayName("Positive Scenario")
    class PositiveScenario {

        @Test
        @DisplayName("Should successfully create a loan application")
        void shouldCreateLoanApplication() {
            // Given: All required entities are found
            given(mockLoanApplication.loanTypeId()).willReturn(loanTypeId);
            given(assembler.mapToAggregateRoot(command)).willReturn(mockApplication);
            given(loanRulePersistencePort.findById(loanRuleId)).willReturn(Optional.of(mockLoanRule));
            given(loanTypePersistencePort.findById(loanTypeId)).willReturn(Optional.of(mockLoanType));
            given(assembler.mapToResponse(mockApplication)).willReturn(response);

            // When
            LoanApplicationResponse actualResponse = useCase.execute(command);

            // Then
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.serial()).isEqualTo(response.serial());

            then(assembler).should().mapToAggregateRoot(command);
            then(loanRulePersistencePort).should().findById(loanRuleId);
            then(loanTypePersistencePort).should().findById(loanTypeId);
            then(createService).should().create(mockApplication, mockLoanRule, mockLoanType);
            then(loanApplicationPersistencePort).should().save(mockApplication);
            then(assembler).should().mapToResponse(mockApplication);
        }
    }

    @Nested
    @DisplayName("Negative Scenarios")
    class NegativeScenarios {

        @Test
        @DisplayName("Should throw LoanRuleNotFoundException if loan rule is missing")
        void shouldThrowIfLoanRuleMissing() {
            // Given: Loan rule not found
            given(assembler.mapToAggregateRoot(command)).willReturn(mockApplication);
            given(loanRulePersistencePort.findById(loanRuleId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(LoanRuleNotFoundException.class)
                    .hasMessageContaining(loanRuleId.value().toString());

            then(assembler).should().mapToAggregateRoot(command);
            then(loanRulePersistencePort).should().findById(loanRuleId);
            then(loanTypePersistencePort).shouldHaveNoInteractions();
            then(createService).shouldHaveNoInteractions();
            then(loanApplicationPersistencePort).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw LoanTypeNotFoundException if loan type is missing")
        void shouldThrowIfLoanTypeMissing() {
            // Given: Loan rule found but loan type missing
            given(mockLoanApplication.loanTypeId()).willReturn(loanTypeId);
            given(assembler.mapToAggregateRoot(command)).willReturn(mockApplication);
            given(loanRulePersistencePort.findById(loanRuleId)).willReturn(Optional.of(mockLoanRule));
            given(loanTypePersistencePort.findById(loanTypeId)).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(LoanTypeNotFoundException.class)
                    .hasMessageContaining(loanTypeId.value().toString());

            then(assembler).should().mapToAggregateRoot(command);
            then(loanRulePersistencePort).should().findById(loanRuleId);
            then(loanTypePersistencePort).should().findById(loanTypeId);
            then(createService).shouldHaveNoInteractions();
            then(loanApplicationPersistencePort).shouldHaveNoInteractions();
        }
    }
}
