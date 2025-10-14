package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper;

import org.mapstruct.Mapper;

import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeLoanArrangementQueryDto;

@Mapper(config = BaseMapperConfig.class)
public interface LoanArrangementQueryModelMapper {
    TradeLoanArrangementQueryDto toQueryModel(TradeLoanArrangementEntity entity);
}
