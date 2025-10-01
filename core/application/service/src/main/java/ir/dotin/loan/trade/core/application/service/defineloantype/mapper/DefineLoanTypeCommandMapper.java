package ir.dotin.loan.trade.core.application.service.defineloantype.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.EconomicSectorCurrency;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanTopicConfiguration;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Attribute;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.IncomeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.trade.core.application.ports.driven.command.DefineLoanTypeCommand;
import ir.dotin.loan.trade.core.application.service.BaseMapperConfig;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@Mapper(config = BaseMapperConfig.class)
public interface DefineLoanTypeCommandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "editReason", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "disable", ignore = true)
    @Mapping(target = "previousVersion", ignore = true)
    TradeLoanType.Builder toBuilder(DefineLoanTypeCommand command);

    LoanTypeCode map(DefineLoanTypeCommand.LoanTypeCodeDto dto);

    Title map(DefineLoanTypeCommand.TitleDto dto);

    LoanApplicationStatus map(DefineLoanTypeCommand.LoanApplicationStatusDto dto);

    EconomicSectorCurrency map(DefineLoanTypeCommand.EconomicSectorCurrencyDto dto);

    EconomicSector map(DefineLoanTypeCommand.EconomicSectorDto dto);

    Attribute map(DefineLoanTypeCommand.AttributeDto dto);

    LoanArrangementId map(DefineLoanTypeCommand.LoanArrangementIdDto dto);

    IncomeId map(DefineLoanTypeCommand.IncomeIdDto dto);

    LoanTypeGroupId map(DefineLoanTypeCommand.LoanTypeGroupIdDto dto);

    LoanTopicConfiguration<TradeRelationType> map(DefineLoanTypeCommand.LoanTopicConfigurationDto dto);
}
