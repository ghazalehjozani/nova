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
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class SubSourceValidationRuleTest {

    private static final String SUB_SOURCE = "SS1";

    @Mock
    private LoanServicePort loanServicePort;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OriginateFacilityCommand command;

    @InjectMocks
    private SubSourceValidationRule rule;

    @Test
    void validateSubSourceSkipsWhenNull() {
        when(command.loanApplication().subSource()).thenReturn(null);

        Result<Unit> result = rule.validateSubSource(command);

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(loanServicePort);
    }

    @Test
    void validateSubSourceSucceedsWhenLoaded() {
        when(command.loanApplication().subSource()).thenReturn(new OriginateFacilityCommand.SubSourceDto(SUB_SOURCE));
        when(loanServicePort.loadResourceByCode(SUB_SOURCE)).thenReturn(Result.success(new SubSource(SUB_SOURCE)));

        Result<Unit> result = rule.validateSubSource(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateSubSourceFailsWhenPortFails() {
        when(command.loanApplication().subSource()).thenReturn(new OriginateFacilityCommand.SubSourceDto(SUB_SOURCE));
        when(loanServicePort.loadResourceByCode(SUB_SOURCE))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_ACCOUNT_NUMBER, SUB_SOURCE)));

        Result<Unit> result = rule.validateSubSource(command);

        assertThat(result.isFailure()).isTrue();
    }
}
