package ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;

import java.util.List;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.TopicInfo;

public interface LoanServicePort {

    Result<EconomicSector> loadEconomicalSectorByCode(EconomicSector economicSector);

    Result<EconomicalSectorResponse> loadEconomicalSector(EconomicSector economicSector);

    Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            EconomicSector economicSector, LoanTypeCode loanTypeCode);

    Result<ReasonType> loadReasonTypeForCreate(String reasonTypeCode);

    Result<ReasonType> loadReasonTypeForRevoke(String reasonTypeCode);

    Result<SubSource> loadResourceByCode(String subSourceCode);

    Result<List<TopicInfo>> loadTopicByCode(List<String> topicCodes);

    Result<List<BranchCode>> loadCoveredBranches(BranchCode branchCode);

    Result<ApplicationNumber> getApplicationNumber(Branch branch, LoanTypeCode loanTypeCode, Party party);
}
