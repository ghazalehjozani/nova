package ir.dotin.loan.trade.core.application.service.defineloantype.mapper;

import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.EconomicSectorCurrency;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.IncomeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TopicRelationType;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineLoanTypeCommand;
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

    default EconomicSectorCurrency map(DefineLoanTypeCommand.EconomicSectorCurrencyDto dto) {
        Set<CurrencyType> currencyTypes = new HashSet<>();
        for (var cu : dto.currencyTypes()) {
            CurrencyType currencyType = new CurrencyType(Currency.getInstance(cu.value()));
            currencyTypes.add(currencyType);
        }
        return new EconomicSectorCurrency(map(dto.economicSector()), currencyTypes);
    }

    Set<EconomicSector> map(Set<DefineLoanTypeCommand.EconomicSectorDto> dto);

    EconomicSector map(DefineLoanTypeCommand.EconomicSectorDto dto);

    LoanArrangementId map(DefineLoanTypeCommand.LoanArrangementIdDto dto);

    IncomeId map(DefineLoanTypeCommand.IncomeIdDto dto);

    LoanTypeGroupId map(DefineLoanTypeCommand.LoanTypeGroupIdDto dto);

    default Multimap<RelationType<TradeRelationType>, LoanTopic> map(
            List<DefineLoanTypeCommand.RelationTypeLoanTopicDto> dtos) {

        Multimap<RelationType<TradeRelationType>, LoanTopic> multimap = ArrayListMultimap.create();

        if (dtos == null || dtos.isEmpty()) {
            return multimap;
        }

        for (var dto : dtos) {
            LoanTopic loanTopic = LoanTopic.of(
                            dto.topicName(),
                            dto.topicCode(),
                            TopicRelationType.of(dto.relationType().name()).getValue(),
                            map(dto.economicSectors()))
                    .orElseThrow();

            multimap.put(dto.relationType(), loanTopic);
        }

        return multimap;
    }
}
