package ir.dotin.loan.trade.core.application.service.openfacilitycase.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.core.application.ports.driven.command.OpenFacilityCaseCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

@Mapper(config = BaseMapperConfig.class, uses = OpenFacilityCaseLoanApplicationMapper.class)
public interface OpenFacilityCaseCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loanTypeId", ignore = true)
    @Mapping(target = "loanArrangementId", ignore = true)
    @Mapping(target = "currentState", ignore = true)
    @Mapping(target = "disbursementDestinationAccount", ignore = true)
    @Mapping(target = "totalDisbursedAmount", ignore = true)
    @Mapping(target = "sanctionedLoan", ignore = true)
    @Mapping(target = "issueContractTransactionNumbers", ignore = true)
    @Mapping(target = "disbursementTransactionNumbers", ignore = true)
    TradeLoanFacility.Builder map(OpenFacilityCaseCommand command);
}
