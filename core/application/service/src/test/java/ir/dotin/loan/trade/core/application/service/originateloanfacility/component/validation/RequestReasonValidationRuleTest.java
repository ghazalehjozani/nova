package ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class RequestReasonValidationRuleTest {

    private static final String REASON = "R1";

    @Mock
    private LoanServicePort loanServicePort;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OriginateFacilityCommand command;

    @InjectMocks
    private RequestReasonValidationRule rule;

    @Test
    void validateRequestReasonSucceedsWhenLoaded() {
        when(command.loanApplication().requestReason().code()).thenReturn(REASON);
        when(loanServicePort.loadReasonTypeForCreate(REASON))
                .thenReturn(Result.success(new ReasonType(REASON, "C1", "desc", "type", false, false)));

        Result<Unit> result = rule.validateRequestReason(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateRequestReasonFailsWhenPortFails() {
        when(command.loanApplication().requestReason().code()).thenReturn(REASON);
        when(loanServicePort.loadReasonTypeForCreate(REASON))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_ACCOUNT_NUMBER, REASON)));

        Result<Unit> result = rule.validateRequestReason(command);

        assertThat(result.isFailure()).isTrue();
    }
}
