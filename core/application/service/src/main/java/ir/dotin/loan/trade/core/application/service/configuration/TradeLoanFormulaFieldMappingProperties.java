package ir.dotin.loan.trade.core.application.service.configuration;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.context.properties.ConfigurationProperties;

import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;

@ConfigurationProperties(prefix = "trade-loan.formula.field-mappings")
public record TradeLoanFormulaFieldMappingProperties(Map<Character, TradeLoanFacilityFormulaField> fieldMap) {

    public TradeLoanFormulaFieldMappingProperties {
        fieldMap = fieldMap == null ? createDefaultMappings() : ImmutableMap.copyOf(fieldMap);
    }

    private static Map<Character, TradeLoanFacilityFormulaField> createDefaultMappings() {
        return Map.of(
                'p', TradeLoanFacilityFormulaField.APPROVED_AMOUNT,
                'a', TradeLoanFacilityFormulaField.APPROVED_AMOUNT,
                'r', TradeLoanFacilityFormulaField.REQUESTED_AMOUNT,
                't', TradeLoanFacilityFormulaField.REQUESTED_AMOUNT,
                'c', TradeLoanFacilityFormulaField.COMMISSION_AMOUNT,
                's', TradeLoanFacilityFormulaField.SHIPMENT_VALUE,
                'i', TradeLoanFacilityFormulaField.INSURANCE_RATE);
    }
}
