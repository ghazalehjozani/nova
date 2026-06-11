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
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SamatDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.samat.ValidateSamatPort;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class SamatValidationRuleTest {

    @Mock
    private ValidateSamatPort validateSamatPort;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OriginateLoanFacilityCommand command;

    @InjectMocks
    private SamatValidationRule rule;

    @Test
    void validateSamatSkipsWhenSamatDtoIsNull() {
        when(command.loanApplication().samat()).thenReturn(null);

        Result<Unit> result = rule.validateSamat(command);

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(validateSamatPort);
    }

    @Test
    void validateSamatDelegatesToPortWhenPresent() {
        SamatDto samatDto = new SamatDto("1234567890123456", null, null, null, null, null);
        when(command.loanApplication().samat()).thenReturn(samatDto);
        when(command.loanApplication().economicSector().code()).thenReturn("12");
        when(command.loanTypeCode()).thenReturn("100");
        when(validateSamatPort.validateSamat(any(Samat.class), anyString(), anyString()))
                .thenReturn(Result.success());

        Result<Unit> result = rule.validateSamat(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateSamatFailsWhenPortFails() {
        SamatDto samatDto = new SamatDto("1234567890123456", null, null, null, null, null);
        when(command.loanApplication().samat()).thenReturn(samatDto);
        when(command.loanApplication().economicSector().code()).thenReturn("12");
        when(command.loanTypeCode()).thenReturn("100");
        when(validateSamatPort.validateSamat(any(Samat.class), anyString(), anyString()))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_ECONOMIC_SECTOR_FOR_LOAN_TYPE)));

        Result<Unit> result = rule.validateSamat(command);

        assertThat(result.isFailure()).isTrue();
    }
}
