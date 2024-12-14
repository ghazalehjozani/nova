package ir.dotin.loan.morabehe.core.application.service.usecase;

import ir.dotin.loan.baseloan.core.application.service.command.loanapplication.BaseApproveLoanApplicationCommand;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionType;
import ir.dotin.loan.morabehe.core.application.ports.outbound.client.MorabeheSanctionClientPort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.application.service.assembler.MorabeheLoanApplicationAssembler;
import ir.dotin.loan.morabehe.core.application.service.command.MorabeheApproveLoanApplicationCommand;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanApplicationNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.exception.LoanRuleNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.exception.SanctionNotFoundException;
import ir.dotin.loan.morabehe.core.application.service.response.LoanApplicationResponse;
import ir.dotin.loan.morabehe.core.application.service.usecase.impl.ApproveLoanApplicationUseCaseImpl;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.LoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.service.ApproveLoanApplicationService;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
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
class ApproveLoanApplicationUseCaseTest {

    @Mock
    private MorabeheLoanApplicationPersistencePort loanApplicationPersistencePort;

    @Mock
    private ApproveLoanApplicationService approveService;

    @Mock
    private MorabeheLoanRulePersistencePort loanRulePersistencePort;

    @Mock
    private MorabeheSanctionClientPort sanctionClientPort;

    @Mock
    private MorabeheLoanApplicationAssembler assembler;

    @InjectMocks
    private ApproveLoanApplicationUseCaseImpl useCase;

    private MorabeheApproveLoanApplicationCommand command;
    private MorabeheLoanApplication mockApplication;
    private LoanApplication mockLoanApplication;
    private MorabeheLoanRule mockLoanRule;
    private Sanction mockSanction;
    private MorabeheLoanApplicationId applicationId;
    private MorabeheLoanRuleId loanRuleId;
    private LoanApplicationResponse response;

    @BeforeEach
    void setUp() {
        UUID appUuid = UUID.randomUUID();
        applicationId = new MorabeheLoanApplicationId(appUuid);

        // Prepare the command with a sanction serial
        BaseApproveLoanApplicationCommand.SanctionSerialDto sanctionDto =
                new BaseApproveLoanApplicationCommand.SanctionSerialDto("SANCTION-123", SanctionType.GENERAL);
        BaseApproveLoanApplicationCommand baseCommand = new BaseApproveLoanApplicationCommand(appUuid, sanctionDto);
        command = new MorabeheApproveLoanApplicationCommand(baseCommand);

        // Mock domain objects
        mockApplication = mock(MorabeheLoanApplication.class);
        mockLoanApplication = mock(LoanApplication.class);
        mockLoanRule = mock(MorabeheLoanRule.class);
        mockSanction = mock(Sanction.class);

        UUID ruleUuid = UUID.randomUUID();
        loanRuleId = new MorabeheLoanRuleId(ruleUuid);

        response = new LoanApplicationResponse(UUID.randomUUID());
    }

    @Nested
    @DisplayName("Positive Scenario")
    class PositiveScenario {

        @Test
        @DisplayName("Should successfully approve a valid loan application")
        void shouldApproveLoanApplication() {
            // Given
            given(mockLoanApplication.loanRuleId()).willReturn(loanRuleId);
            given(mockApplication.loanApplication()).willReturn(mockLoanApplication);
            given(loanApplicationPersistencePort.findById(applicationId)).willReturn(Optional.of(mockApplication));
            given(assembler.mapToAggregateRoot(command, mockApplication)).willReturn(mockApplication);

            given(loanRulePersistencePort.findById(loanRuleId)).willReturn(Optional.of(mockLoanRule));

            SanctionSerial sanctionSerial = new SanctionSerial("SANCTION-123", SanctionType.GENERAL);
            given(mockLoanApplication.sanctionSerial()).willReturn(sanctionSerial);
            given(sanctionClientPort.getBySerial(sanctionSerial)).willReturn(Optional.of(mockSanction));

            given(assembler.mapToResponse(mockApplication)).willReturn(response);

            // When
            LoanApplicationResponse actualResponse = useCase.execute(command);

            // Then
            assertThat(actualResponse).isNotNull();
            assertThat(actualResponse.serial()).isEqualTo(response.serial());

            then(loanApplicationPersistencePort).should().findById(applicationId);
            then(assembler).should().mapToAggregateRoot(command, mockApplication);
            then(loanRulePersistencePort).should().findById(loanRuleId);
            then(sanctionClientPort).should().getBySerial(sanctionSerial);
            then(approveService).should().approve(mockApplication, mockLoanRule, mockSanction);
            then(loanApplicationPersistencePort).should().update(mockApplication);
            then(assembler).should().mapToResponse(mockApplication);
        }
    }

    @Nested
    @DisplayName("Negative Scenarios")
    class NegativeScenarios {

        @Test
        @DisplayName("Should throw LoanApplicationNotFoundException if application is missing")
        void shouldThrowIfApplicationMissing() {
            // Given no application found
            given(loanApplicationPersistencePort.findById(applicationId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(LoanApplicationNotFoundException.class)
                    .hasMessageContaining(applicationId.toString());

            then(loanApplicationPersistencePort).should().findById(applicationId);
            then(assembler).shouldHaveNoInteractions();
            then(loanRulePersistencePort).shouldHaveNoInteractions();
            then(sanctionClientPort).shouldHaveNoInteractions();
            then(approveService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw LoanRuleNotFoundException if loan rule is missing")
        void shouldThrowIfLoanRuleMissing() {
            // Given application found, but loan rule missing
            given(mockLoanApplication.loanRuleId()).willReturn(loanRuleId);
            given(mockApplication.loanApplication()).willReturn(mockLoanApplication);
            given(loanApplicationPersistencePort.findById(applicationId)).willReturn(Optional.of(mockApplication));
            given(assembler.mapToAggregateRoot(command, mockApplication)).willReturn(mockApplication);

            given(loanRulePersistencePort.findById(loanRuleId)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(LoanRuleNotFoundException.class)
                    .hasMessageContaining(loanRuleId.toString());

            then(loanApplicationPersistencePort).should().findById(applicationId);
            then(assembler).should().mapToAggregateRoot(command, mockApplication);
            then(loanRulePersistencePort).should().findById(loanRuleId);
            then(sanctionClientPort).shouldHaveNoInteractions();
            then(approveService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw SanctionNotFoundException if sanction is missing")
        void shouldThrowIfSanctionMissing() {
            // Given everything found except sanction
            given(mockLoanApplication.loanRuleId()).willReturn(loanRuleId);
            given(mockApplication.loanApplication()).willReturn(mockLoanApplication);
            given(loanApplicationPersistencePort.findById(applicationId)).willReturn(Optional.of(mockApplication));
            given(assembler.mapToAggregateRoot(command, mockApplication)).willReturn(mockApplication);

            given(loanRulePersistencePort.findById(loanRuleId)).willReturn(Optional.of(mockLoanRule));

            SanctionSerial missingSanction = new SanctionSerial("NO-SANCTION", SanctionType.SPECIAL);
            given(mockLoanApplication.sanctionSerial()).willReturn(missingSanction);

            given(sanctionClientPort.getBySerial(missingSanction)).willReturn(Optional.empty());

            // When / Then
            assertThatThrownBy(() -> useCase.execute(command))
                    .isInstanceOf(SanctionNotFoundException.class);

            then(loanApplicationPersistencePort).should().findById(applicationId);
            then(assembler).should().mapToAggregateRoot(command, mockApplication);
            then(loanRulePersistencePort).should().findById(loanRuleId);
            then(sanctionClientPort).should().getBySerial(missingSanction);
            then(approveService).shouldHaveNoInteractions();
        }
    }

}
