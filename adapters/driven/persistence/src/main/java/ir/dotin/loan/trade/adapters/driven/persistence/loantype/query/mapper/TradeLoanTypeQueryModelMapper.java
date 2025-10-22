package ir.dotin.loan.trade.adapters.driven.persistence.loantype.query.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorCurrencyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RelationTypeLoanTopicEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanTypeQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface TradeLoanTypeQueryModelMapper {
    TradeLoanTypeQueryDto toQueryModel(TradeLoanTypeEntity entity);

    default TradeLoanTypeQueryDto.EconomicSectorCurrencyEmbDto map(EconomicSectorCurrencyEmb e) {
        return new TradeLoanTypeQueryDto.EconomicSectorCurrencyEmbDto(
                e.getEconomicSectorCode(), currencyTypeEmbDtoSetMap(e.getCurrencyTypes()));
    }

    default TradeLoanTypeQueryDto.CurrencyTypeEmbDto map(String value) {
        return new TradeLoanTypeQueryDto.CurrencyTypeEmbDto(value);
    }

    default TradeLoanTypeQueryDto.RelationTypeLoanTopicEmbDto map(RelationTypeLoanTopicEmb e) {
        return new TradeLoanTypeQueryDto.RelationTypeLoanTopicEmbDto(
                e.getTradeRelationType(),
                e.getTopicName(),
                e.getTopicCode(),
                economicSectorEmbDtoSetMap(e.getEconomicSectors()));
    }

    default java.util.Set<TradeLoanTypeQueryDto.CurrencyTypeEmbDto> currencyTypeEmbDtoSetMap(
            java.util.Set<String> values) {
        return values.stream().map(this::map).collect(java.util.stream.Collectors.toSet());
    }

    default java.util.Set<TradeLoanTypeQueryDto.RelationTypeLoanTopicEmbDto.EconomicSectorEmbDto>
            economicSectorEmbDtoSetMap(java.util.Set<String> codes) {
        return codes.stream()
                .map(TradeLoanTypeQueryDto.RelationTypeLoanTopicEmbDto.EconomicSectorEmbDto::new)
                .collect(java.util.stream.Collectors.toSet());
    }
}
