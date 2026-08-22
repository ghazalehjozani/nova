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
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class EconomicSectorValidationRulesTest {

    private static final String SECTOR = "12";
    private static final String LOAN_TYPE = "100";

    @Mock
    private LoanServicePort loanServicePort;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OriginateFacilityCommand command;

    @InjectMocks
    private EconomicSectorValidationRules rules;

    @Test
    void validateEconomicalSectorSucceedsForLeafSector() {
        when(command.loanApplication().economicSector().code()).thenReturn(SECTOR);
        when(loanServicePort.loadEconomicalSector(any(EconomicSector.class)))
                .thenReturn(EconomicalSectorResponse.of(SECTOR, "name", false, null));

        Result<Unit> result = rules.validateEconomicalSector(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateEconomicalSectorFailsForParentSector() {
        when(command.loanApplication().economicSector().code()).thenReturn(SECTOR);
        when(loanServicePort.loadEconomicalSector(any(EconomicSector.class)))
                .thenReturn(EconomicalSectorResponse.of(SECTOR, "name", true, null));

        Result<Unit> result = rules.validateEconomicalSector(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateEconomicalSectorFailsWhenPortFails() {
        when(command.loanApplication().economicSector().code()).thenReturn(SECTOR);
        when(loanServicePort.loadEconomicalSector(any(EconomicSector.class)))
                .thenReturn(Result.failure(
                        Notification.ofError(OriginateLoanFacilityErrorCodes.ECONOMIC_SECTOR_IS_PARENT, SECTOR)));

        Result<Unit> result = rules.validateEconomicalSector(command);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void validateEconomicalSectionForLoanTypeSucceedsWhenValid() {
        when(command.loanApplication().economicSector().code()).thenReturn(SECTOR);
        when(command.loanTypeCode()).thenReturn(LOAN_TYPE);
        when(loanServicePort.validateEconomicalSectorForLoanType(any(EconomicSector.class), any(LoanTypeCode.class)))
                .thenReturn(Result.success(new EconomicalSectorValidation(true, null)));

        Result<Unit> result = rules.validateEconomicalSectionForLoanType(command);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void validateEconomicalSectionForLoanTypeFailsWhenInvalid() {
        when(command.loanApplication().economicSector().code()).thenReturn(SECTOR);
        when(command.loanTypeCode()).thenReturn(LOAN_TYPE);
        when(loanServicePort.validateEconomicalSectorForLoanType(any(EconomicSector.class), any(LoanTypeCode.class)))
                .thenReturn(Result.success(new EconomicalSectorValidation(false, "bad")));

        Result<Unit> result = rules.validateEconomicalSectionForLoanType(command);

        assertThat(result.isFailure()).isTrue();
    }
}
