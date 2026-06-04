package ir.dotin.loan.trade.core.application.service.shared.authz;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class BranchAccessValidatorTest {

    @Mock
    private LoanServicePort loanServicePort;

    @InjectMocks
    private BranchAccessValidator validator;

    @Test
    void nullCallerBranchSkipsCheck() {
        Result<Unit> result =
                validator.verifyCallerCoversBranch(null, BranchCode.of("001").unwrap());

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(loanServicePort);
    }

    @Test
    void callerInCoveredSetIsAllowed() {
        BranchCode facilityBranch = BranchCode.of("001").unwrap();
        when(loanServicePort.loadCoveredBranches(facilityBranch))
                .thenReturn(Result.success(List.of(BranchCode.of("123").unwrap())));

        Result<Unit> result = validator.verifyCallerCoversBranch("123", facilityBranch);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void callerNotInCoveredSetIsDenied() {
        BranchCode facilityBranch = BranchCode.of("001").unwrap();
        when(loanServicePort.loadCoveredBranches(facilityBranch))
                .thenReturn(Result.success(List.of(BranchCode.of("999").unwrap())));

        Result<Unit> result = validator.verifyCallerCoversBranch("123", facilityBranch);

        assertThat(result.isFailure()).isTrue();
    }
}
