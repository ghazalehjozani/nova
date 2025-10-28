package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query.mapper;

import java.util.HashMap;
import java.util.Map;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.entity.TradeLoanArrangementEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.mapper.BaseMapperConfig;
import ir.dotin.loan.trade.adapters.driven.persistence.service.FormulaFieldMappingService;
import ir.dotin.loan.trade.core.application.query.loanarrangement.dto.TradeLoanArrangementQueryDto;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Mapper(config = BaseMapperConfig.class)
public abstract class LoanArrangementQueryModelMapper {

    @Autowired
    private FormulaFieldMappingService formulaFieldMappingService;

    public abstract TradeLoanArrangementQueryDto toQueryModel(TradeLoanArrangementEntity entity);

    TradeLoanArrangementQueryDto.FormulaDto mapFormula(String formula) {
        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> deserialized =
                formulaFieldMappingService.deserializeParameterizedFormula(formula);
        Map<Character, TradeLoanFacilityFormulaField> formulaFieldMap =
                deserialized.parameterizedFormula().fieldMappings();
        HashMap<Character, String> map = new HashMap<>();
        formulaFieldMap.forEach(
                (character, tradeLoanFacilityFormulaField) -> map.put(character, tradeLoanFacilityFormulaField.name()));
        return TradeLoanArrangementQueryDto.FormulaDto.builder()
                .expression(deserialized.parameterizedFormula().formula().value())
                .fieldMappings(map)
                .build();
    }
}
