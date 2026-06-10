package ir.dotin.loan.trade.core.application.service.shared.authz;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.BranchCoveragePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class BranchAccessValidatorTest {

    @Mock
    private BranchCoveragePort branchCoveragePort;

    @Mock
    private TradeLoanFacilityRepository facilityRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TradeLoanFacility facility;

    @InjectMocks
    private BranchAccessValidator validator;

    @Test
    void nullCallerBranchSkipsCheck() {
        Result<Unit> result =
                validator.verifyCallerCoversBranch(null, BranchCode.of("001").unwrap());

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(branchCoveragePort);
    }

    @Test
    void callerInCoveredSetIsAllowed() {
        BranchCode facilityBranch = BranchCode.of("001").unwrap();
        when(branchCoveragePort.coveredBranches(facilityBranch))
                .thenReturn(Result.success(List.of(BranchCode.of("123").unwrap())));

        Result<Unit> result = validator.verifyCallerCoversBranch("123", facilityBranch);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void callerNotInCoveredSetIsDenied() {
        BranchCode facilityBranch = BranchCode.of("001").unwrap();
        when(branchCoveragePort.coveredBranches(facilityBranch))
                .thenReturn(Result.success(List.of(BranchCode.of("999").unwrap())));

        Result<Unit> result = validator.verifyCallerCoversBranch("123", facilityBranch);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    void nullCallerBranchByIdSkipsRepositoryAndPort() {
        Result<Unit> result = validator.verifyCallerCoversFacility(null, LoanFacilityId.of(UUID.randomUUID()));

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(facilityRepository, branchCoveragePort);
    }

    @Test
    void missingFacilityByIdSkipsCheck() {
        LoanFacilityId facilityId = LoanFacilityId.of(UUID.randomUUID());
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        Result<Unit> result = validator.verifyCallerCoversFacility("123", facilityId);

        assertThat(result.isSuccess()).isTrue();
        verifyNoInteractions(branchCoveragePort);
    }

    @Test
    void facilityByIdDelegatesToBranchCheck() {
        LoanFacilityId facilityId = LoanFacilityId.of(UUID.randomUUID());
        BranchCode facilityBranch = BranchCode.of("001").unwrap();
        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(facility.getLoanApplication().getBranch().code()).thenReturn(facilityBranch);
        when(branchCoveragePort.coveredBranches(facilityBranch))
                .thenReturn(Result.success(List.of(BranchCode.of("999").unwrap())));

        Result<Unit> result = validator.verifyCallerCoversFacility("123", facilityId);

        assertThat(result.isFailure()).isTrue();
    }
}
