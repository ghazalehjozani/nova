package ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;

public interface LoanServicePort {

    Result<EconomicSector> loadEconomicalSectionByCode(EconomicSector economicSector);

    Result<EconomicalSectorValidation> validateEconomicalSectionForLoanType(
            EconomicSector economicSector, LoanTypeCode loanTypeCode);
}
