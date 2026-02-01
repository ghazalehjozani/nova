package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ir.dotin.platform.formula.infrastructure.persistence.embeddable.FormulaIdRefEmb;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.CollateralType;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;

@Mapper(config = BaseMapperConfig.class)
public abstract class LoanArrangementQueryModelMapper {

    public abstract TradeLoanArrangementQueryDto toQueryModel(TradeLoanArrangementEntity entity);

    @Mapping(target = "code", expression = "java(type.name())")
    protected abstract TradeLoanArrangementQueryDto.CollateralPolicyEmbDto.CollateralTypeEmbDto mapCollateralType(
            CollateralType type);

    @Mapping(target = "formulaId", source = "value")
    protected abstract TradeLoanArrangementQueryDto.FormulaDto mapFormula(FormulaIdRefEmb formulaIdRefEmb);
}
