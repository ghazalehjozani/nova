package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand.LoanApplicationDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountValidationPort;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class AccountNumberValidationRuleTest {

    private static final String ACCOUNT = "001-100-200-1";

    @Mock
    private AccountValidationPort accountValidationPort;

    @Mock
    private OriginateFacilityCommand command;

    @Mock
    private LoanApplicationDto loanApplication;

    @InjectMocks
    private AccountNumberValidationRule rule;

    @BeforeEach
    void wireApplication() {
        lenient().when(command.loanApplication()).thenReturn(loanApplication);
    }

    @Test
    void validateAccountNumberSucceedsWhenPortAccepts() {
        when(loanApplication.disburseDestination())
                .thenReturn(new DisburseDestinationDto.AccountDestinationDto(ACCOUNT));
        when(accountValidationPort.validateAccountNumber(ACCOUNT))
                .thenReturn(Result.success(new AccountNumber(ACCOUNT)));

        Result<Unit> result = rule.validateAccountNumber(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateAccountNumberFailsWhenPortRejects() {
        when(loanApplication.disburseDestination())
                .thenReturn(new DisburseDestinationDto.AccountDestinationDto(ACCOUNT));
        when(accountValidationPort.validateAccountNumber(ACCOUNT))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_ACCOUNT_NUMBER, ACCOUNT)));

        Result<Unit> result = rule.validateAccountNumber(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateAccountNumberSkipsForDepositDestination() {
        when(loanApplication.disburseDestination())
                .thenReturn(new DisburseDestinationDto.DepositDestinationDto("1234567890"));

        Result<Unit> result = rule.validateAccountNumber(command);

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(accountValidationPort);
    }
}
