package ir.dotin.loan.trade.adapters.driving.contract.mapper;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.formula.service.cqrs.query.EvaluateFormulaQuery;
import ir.dotin.loan.trade.adapters.driving.contract.dto.EvaluateFormulaRequest;

@Component
public class EvaluateFormulaRequestToQueryMapper {

    public EvaluateFormulaQuery toQuery(String code, EvaluateFormulaRequest request) {
        Map<String, String> providerRefs = request.providerRefs() != null ? request.providerRefs() : Map.of();
        Map<String, BigDecimal> overrides = request.overrides() != null ? request.overrides() : Map.of();
        return new EvaluateFormulaQuery(UUID.randomUUID(), code, providerRefs, overrides);
    }
}
