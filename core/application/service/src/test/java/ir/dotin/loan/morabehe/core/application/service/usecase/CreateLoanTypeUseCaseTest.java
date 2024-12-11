package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.application.service.command.config.BaseCreateLoanTypeCommand;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanTypeAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheCreateLoanTypeCommand;
import ir.dotin.loan.morabehe.core.application.service.response.LoanTypeResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.impl.CreateLoanTypeUseCaseImpl;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.LoanType;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.exception.MorabeheLoanTypeValidationException;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class CreateLoanTypeUseCaseTest {

    @Mock
    private MorabeheLoanTypePersistencePort loanTypePersistencePort;

    @Mock
    private MorabeheLoanRulePersistencePort loanRulePersistencePort;

    @Mock
    private MorabeheLoanTypeAssembler assembler;

    @InjectMocks
    private CreateLoanTypeUseCaseImpl useCase;

    private MorabeheCreateLoanTypeCommand command;
    private MorabeheLoanType mockLoanTypeAggregate;
    private LoanType mockLoanType;
    private LoanTypeResponse mockResponse;

    private final LoanTypeCode loanTypeCode = LoanTypeCode.valueOf("TYPE-ABC");
    private MorabeheLoanRuleId existingRuleId;
    private MorabeheLoanRuleId missingRuleId;

    @BeforeEach
    void setUp() {
        existingRuleId = new MorabeheLoanRuleId(UUID.randomUUID());
        missingRuleId = new MorabeheLoanRuleId(UUID.randomUUID());


        BaseCreateLoanTypeCommand baseCommand = mock(BaseCreateLoanTypeCommand.class);

        command = new MorabeheCreateLoanTypeCommand(false, baseCommand);

        mockLoanTypeAggregate = mock(MorabeheLoanType.class);
        mockLoanType = mock(LoanType.class);
        mockResponse = new LoanTypeResponse(UUID.randomUUID());

        given(mockLoanTypeAggregate.getLoanType()).willReturn(mockLoanType);
        given(mockLoanType.getCode()).willReturn(loanTypeCode);

        given(assembler.mapToAggregateRoot(command)).willReturn(mockLoanTypeAggregate);

        // By default, no code duplicates and all rules exist
        given(loanTypePersistencePort.existsByCode(loanTypeCode)).willReturn(false);
    }

    @Nested
    @DisplayName("Positive Scenario")
    class PositiveScenario {
        @Test
        @DisplayName("Should create loan type when code is unique and all rules exist")
        void shouldCreateLoanTypeSuccessfully() {
            // Given
            given(assembler.mapToResponse(mockLoanTypeAggregate)).willReturn(mockResponse);
            given(mockLoanType.getLoanRuleIds()).willReturn(Set.of(existingRuleId));
            given(loanRulePersistencePort.existsByIdAndEnable(existingRuleId)).willReturn(true);
            // When
            LoanTypeResponse actualResponse = useCase.execute(command);

            // Then
            assertThat(actualResponse).isEqualTo(mockResponse);

            then(assembler).should().mapToAggregateRoot(command);
            then(loanTypePersistencePort).should().existsByCode(loanTypeCode);
            then(loanRulePersistencePort).should().existsByIdAndEnable(existingRuleId);
            then(mockLoanTypeAggregate).should().createLoanType();
            then(loanTypePersistencePort).should().save(mockLoanTypeAggregate);
            then(assembler).should().mapToResponse(mockLoanTypeAggregate);
        }
    }

    @Nested
    @DisplayName("Negative Scenarios")
    class NegativeScenarios {

        @Test
        @DisplayName("Should throw MorabeheLoanTypeValidationException if code is duplicate")
        void shouldThrowIfDuplicateCode() {
            // Given
            given(loanTypePersistencePort.existsByCode(loanTypeCode)).willReturn(true);

            // When / Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(MorabeheLoanTypeValidationException.class)
                    .hasMessageContaining("Duplicate loan type code");

            then(assembler).should().mapToAggregateRoot(command);
            then(loanTypePersistencePort).should().existsByCode(loanTypeCode);
            then(loanRulePersistencePort).shouldHaveNoInteractions();
            then(loanTypePersistencePort).shouldHaveNoMoreInteractions();
            then(assembler).should(never()).mapToResponse(any());
        }

        @Test
        @DisplayName("Should throw MorabeheLoanTypeValidationException if a loan rule doesn't exist or is disabled")
        void shouldThrowIfLoanRuleMissingOrDisabled() {
            // Given: Changing command loan rule IDs to include a missing rule
            given(mockLoanType.getLoanRuleIds()).willReturn(Set.of(existingRuleId, missingRuleId));
            given(loanRulePersistencePort.existsByIdAndEnable(existingRuleId)).willReturn(true);
            given(loanRulePersistencePort.existsByIdAndEnable(missingRuleId)).willReturn(false);

            // When / Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(MorabeheLoanTypeValidationException.class)
                    .hasMessageContaining("loan rule not exist with Id:");

            then(assembler).should().mapToAggregateRoot(command);
            then(loanTypePersistencePort).should().existsByCode(loanTypeCode);
            then(loanRulePersistencePort).should().existsByIdAndEnable(existingRuleId);
            then(loanRulePersistencePort).should().existsByIdAndEnable(missingRuleId);
            then(mockLoanTypeAggregate).should(never()).createLoanType();
            then(loanTypePersistencePort).shouldHaveNoMoreInteractions();
            then(assembler).should(never()).mapToResponse(any());
        }
    }

}
