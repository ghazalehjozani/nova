package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.mapper;

import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.entity.TradeSanctionedLoanEntity;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistance.mapper.ValueObjectMapper;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {ValueObjectMapper.class, tradeDisbursementSchedulePersistenceMapper.class})
public interface TradeSanctionedLoanPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    TradeSanctionedLoanEntity map(TradeSanctionedLoan domain);

    default TradeSanctionedLoanEntity map(Optional<TradeSanctionedLoan> domainOpt) {
        return domainOpt.map(this::map).orElse(null);
    }

    TradeSanctionedLoan map(TradeSanctionedLoanEntity entity);
}
