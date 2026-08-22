package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.AccountNumberValidationRule;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.DepositValidationRules;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.EconomicSectorValidationRules;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.RequestReasonValidationRule;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.SamatValidationRule;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.SubSourceValidationRule;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class FacilityValidatorTest {

    @Mock
    private DepositValidationRules depositValidationRules;

    @Mock
    private EconomicSectorValidationRules economicSectorValidationRules;

    @Mock
    private SamatValidationRule samatValidationRule;

    @Mock
    private AccountNumberValidationRule accountNumberValidationRule;

    @Mock
    private RequestReasonValidationRule requestReasonValidationRule;

    @Mock
    private SubSourceValidationRule subSourceValidationRule;

    @InjectMocks
    private FacilityValidator validator;

    private final OriginateFacilityCommand command = mock(OriginateFacilityCommand.class);

    private void allRulesPass() {
        lenient().when(depositValidationRules.validateDeposit(command)).thenReturn(Result.success());
        lenient().when(depositValidationRules.isDepositClosed(command)).thenReturn(Result.success());
        lenient().when(depositValidationRules.validateDebtorDeposit(command)).thenReturn(Result.success());
        lenient().when(depositValidationRules.validateCreditorDeposit(command)).thenReturn(Result.success());
        lenient().when(depositValidationRules.validateDepositCurrency(command)).thenReturn(Result.success());
        lenient()
                .when(economicSectorValidationRules.validateEconomicalSector(command))
                .thenReturn(Result.success());
        lenient()
                .when(economicSectorValidationRules.validateEconomicalSectionForLoanType(command))
                .thenReturn(Result.success());
        lenient().when(samatValidationRule.validateSamat(command)).thenReturn(Result.success());
        lenient()
                .when(accountNumberValidationRule.validateAccountNumber(command))
                .thenReturn(Result.success());
        lenient()
                .when(requestReasonValidationRule.validateRequestReason(command))
                .thenReturn(Result.success());
        lenient().when(subSourceValidationRule.validateSubSource(command)).thenReturn(Result.success());
    }

    @Test
    void allValidationsPassYieldsSuccess() {
        allRulesPass();

        Result<Unit> result = validator.callAndValidateServices(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void anyRuleFailureYieldsAggregatedFailure() {
        allRulesPass();
        when(accountNumberValidationRule.validateAccountNumber(command))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_ACCOUNT_NUMBER, "001")));

        Result<Unit> result = validator.callAndValidateServices(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void multipleRuleFailuresAreAggregated() {
        allRulesPass();
        when(depositValidationRules.validateDebtorDeposit(command))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_DEBTOR_DEPOSIT, "d")));
        when(depositValidationRules.validateCreditorDeposit(command))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_CREDITOR_DEPOSIT, "c")));

        Result<Unit> result = validator.callAndValidateServices(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.err().orElseThrow().notification().errors()).hasSizeGreaterThanOrEqualTo(2);
    }
}
