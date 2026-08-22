package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand.LoanApplicationDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.AmountDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.CurrencyTypeDto;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.DisburseDestinationDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CreditorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CurrencyValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DebtorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DepositClosedStatus;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class DepositValidationRulesTest {

    private static final String DEPOSIT = "1234567890";
    private static final String CURRENCY = "IRR";

    @Mock
    private DepositServicePort depositServicePort;

    @Mock
    private OriginateFacilityCommand command;

    @Mock
    private LoanApplicationDto loanApplication;

    @InjectMocks
    private DepositValidationRules rules;

    @BeforeEach
    void wireApplication() {
        lenient().when(command.loanApplication()).thenReturn(loanApplication);
        lenient().when(loanApplication.currency()).thenReturn(new CurrencyTypeDto(CURRENCY));
    }

    private void depositDestination() {
        when(loanApplication.disburseDestination())
                .thenReturn(new DisburseDestinationDto.DepositDestinationDto(DEPOSIT));
    }

    private void accountDestination() {
        when(loanApplication.disburseDestination())
                .thenReturn(new DisburseDestinationDto.AccountDestinationDto("001-1"));
    }

    @Test
    void validateDepositSucceedsWhenPortReturnsInfo() {
        depositDestination();
        when(depositServicePort.getDepositInfo(any(DepositNumber.class)))
                .thenReturn(Result.success(mock(DepositInfo.class)));

        Result<Unit> result = rules.validateDeposit(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateDepositFailsWhenPortFails() {
        depositDestination();
        when(depositServicePort.getDepositInfo(any(DepositNumber.class)))
                .thenReturn(Result.failure(Notification.ofError(
                        OriginateLoanFacilityErrorCodes.DISBURSE_DESTINATION_DEPOSIT_IS_CLOSED, DEPOSIT)));

        Result<Unit> result = rules.validateDeposit(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateDepositSkipsForAccountDestination() {
        accountDestination();

        Result<Unit> result = rules.validateDeposit(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void isDepositClosedSucceedsWhenOpen() {
        depositDestination();
        when(depositServicePort.isDepositClosed(
                        any(DepositNumber.class),
                        eq(CurrencyType.valueOf(CURRENCY).unwrap())))
                .thenReturn(Result.success(new DepositClosedStatus(false, CURRENCY)));

        Result<Unit> result = rules.isDepositClosed(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void isDepositClosedFailsWhenClosed() {
        depositDestination();
        when(depositServicePort.isDepositClosed(
                        any(DepositNumber.class),
                        eq(CurrencyType.valueOf(CURRENCY).unwrap())))
                .thenReturn(Result.success(new DepositClosedStatus(true, CURRENCY)));

        Result<Unit> result = rules.isDepositClosed(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateDebtorDepositFailsWhenInvalid() {
        depositDestination();
        when(depositServicePort.validateDebtorDeposit(
                        any(DepositNumber.class),
                        eq(CurrencyType.valueOf(CURRENCY).unwrap())))
                .thenReturn(Result.success(new DebtorDepositValidation(false)));

        Result<Unit> result = rules.validateDebtorDeposit(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateDebtorDepositSucceedsWhenValid() {
        depositDestination();
        when(depositServicePort.validateDebtorDeposit(
                        any(DepositNumber.class),
                        eq(CurrencyType.valueOf(CURRENCY).unwrap())))
                .thenReturn(Result.success(new DebtorDepositValidation(true)));

        Result<Unit> result = rules.validateDebtorDeposit(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateCreditorDepositFailsWhenInvalid() {
        depositDestination();
        when(loanApplication.requestedAmount()).thenReturn(new AmountDto(BigDecimal.TEN));
        when(depositServicePort.validateCreditorDeposit(
                        any(DepositNumber.class),
                        eq(CurrencyType.valueOf(CURRENCY).unwrap()),
                        any(BigDecimal.class)))
                .thenReturn(Result.success(new CreditorDepositValidation(false)));

        Result<Unit> result = rules.validateCreditorDeposit(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateDepositCurrencyFailsWhenInvalid() {
        depositDestination();
        when(depositServicePort.hasDepositAllowedCurrencies(any(DepositNumber.class), any(List.class)))
                .thenReturn(Result.success(new CurrencyValidation(false, "bad")));

        Result<Unit> result = rules.validateDepositCurrency(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateDepositCurrencySucceedsWhenValid() {
        depositDestination();
        when(depositServicePort.hasDepositAllowedCurrencies(any(DepositNumber.class), any(List.class)))
                .thenReturn(Result.success(new CurrencyValidation(true, null)));

        Result<Unit> result = rules.validateDepositCurrency(command);

        assertThat(result.isSuccess()).isTrue();
    }
}
