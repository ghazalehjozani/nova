package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeSanctionedLoanEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.ValueObjectMapper;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeSanctionedLoan;

@Mapper(config = BaseMapperConfig.class, uses = ValueObjectMapper.class)
public interface TradeSanctionedLoanPersistenceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "disbursementSchedule", ignore = true)
    TradeSanctionedLoanEntity map(TradeSanctionedLoan domain);

    default @Nullable TradeSanctionedLoanEntity map(Optional<TradeSanctionedLoan> domainOpt) {
        return domainOpt.map(this::map).orElse(null);
    }

    TradeSanctionedLoan map(TradeSanctionedLoanEntity entity);
}
