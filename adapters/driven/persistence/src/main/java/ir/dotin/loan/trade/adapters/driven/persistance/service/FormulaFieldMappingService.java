package ir.dotin.loan.trade.adapters.driven.persistance.service;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.platform.commons.domain.vo.ParameterizedFormula;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.InvalidDomainStateException;
import ir.dotin.loan.trade.adapters.driven.persistance.exception.PersistenceConversionException;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@Service
public class FormulaFieldMappingService {

    private final ObjectMapper objectMapper;
    private final Map<Character, TradeLoanFacilityFormulaField> defaultFieldMappings;

    public FormulaFieldMappingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.defaultFieldMappings = initializeDefaultMappings();
    }

    private Map<Character, TradeLoanFacilityFormulaField>
            initializeDefaultMappings() { // TODO: Use dynamic service for load
        return Map.of(
                'A', TradeLoanFacilityFormulaField.APPROVED_AMOUNT, 'I', TradeLoanFacilityFormulaField.INSURANCE_RATE);
    }

    public String serializeFormula(
            LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> formula) {
        if (formula == null) return null;

        return serializeParameterizedFormula(formula.parameterizedFormula());
    }

    public String serializeParameterizedFormula(
            ParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> formula) {
        if (formula == null) return null;

        FormulaData data = new FormulaData(
                formula.formula().value(),
                serializeMappings(formula.fieldMappings()),
                extractVariables(formula.formula().value()));

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new PersistenceConversionException("Cannot serialize formula", e);
        }
    }

    public LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
            deserializeInterestFormula(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            return null;
        }

        if (!serialized.trim().startsWith("{")) {
            return deserializeLegacyFormula(serialized);
        }

        try {
            FormulaData data = objectMapper.readValue(serialized, FormulaData.class);

            Formula formula = Formula.valueOf(data.expression)
                    .orElseThrow(
                            () -> new InvalidDomainStateException("Invalid formula expression: " + data.expression));

            Map<Character, TradeLoanFacilityFormulaField> mappings = deserializeMappings(data.mappings, data.variables);

            ParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> paramFormula =
                    ParameterizedFormula.valueOf(formula, mappings)
                            .orElseThrow(() -> new InvalidDomainStateException("Cannot create parameterized formula"));

            return LoanFacilityParameterizedFormula.valueOf(paramFormula)
                    .orElseThrow(() -> new InvalidDomainStateException("Cannot create loan facility formula"));

        } catch (JsonProcessingException e) {
            return deserializeLegacyFormula(serialized);
        }
    }

    public ParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
            deserializeParameterizedFormula(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            return null;
        }

        LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> loanFormula =
                deserializeInterestFormula(serialized);

        return loanFormula != null ? loanFormula.parameterizedFormula() : null;
    }

    private LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
            deserializeLegacyFormula(String expression) {
        Formula formula = Formula.valueOf(expression)
                .orElseThrow(() -> new InvalidDomainStateException("Invalid legacy formula expression: " + expression));

        Set<Character> variables = extractVariables(expression);
        Map<Character, TradeLoanFacilityFormulaField> mappings = new HashMap<>();

        for (Character var : variables) {
            TradeLoanFacilityFormulaField field = defaultFieldMappings.get(var);
            if (field != null) {
                mappings.put(var, field);
            }
        }

        if (mappings.isEmpty() && !variables.isEmpty()) {
            throw new InvalidDomainStateException("Cannot map variables in legacy formula: " + variables);
        }

        ParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> paramFormula =
                ParameterizedFormula.valueOf(formula, mappings)
                        .orElseThrow(() -> new InvalidDomainStateException(
                                "Cannot create parameterized formula from legacy data"));

        return LoanFacilityParameterizedFormula.valueOf(paramFormula)
                .orElseThrow(
                        () -> new InvalidDomainStateException("Cannot create loan facility formula from legacy data"));
    }

    private Set<Character> extractVariables(String expression) {
        if (expression == null || expression.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Character> variables = new HashSet<>();
        for (char c : expression.toCharArray()) {
            if (Character.isLetter(c)) {
                variables.add(c);
            }
        }
        return variables;
    }

    private String serializeMappings(Map<Character, TradeLoanFacilityFormulaField> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }

        Map<String, String> simpleMappings = mappings.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(), e -> e.getValue().name()));

        try {
            return objectMapper.writeValueAsString(simpleMappings);
        } catch (JsonProcessingException e) {
            throw new PersistenceConversionException("Cannot serialize field mappings", e);
        }
    }

    private Map<Character, TradeLoanFacilityFormulaField> deserializeMappings(
            String serialized, Set<Character> expectedVariables) {

        if (serialized == null || serialized.trim().isEmpty()) {
            Map<Character, TradeLoanFacilityFormulaField> mappings = new HashMap<>();
            if (expectedVariables != null) {
                for (Character var : expectedVariables) {
                    TradeLoanFacilityFormulaField field = defaultFieldMappings.get(var);
                    if (field != null) {
                        mappings.put(var, field);
                    }
                }
            }
            return mappings;
        }

        try {
            Map<String, String> simpleMappings = objectMapper.readValue(
                    serialized, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));

            Map<Character, TradeLoanFacilityFormulaField> mappings = new HashMap<>();
            for (Map.Entry<String, String> entry : simpleMappings.entrySet()) {
                if (entry.getKey().length() == 1) {
                    Character key = entry.getKey().charAt(0);
                    TradeLoanFacilityFormulaField field = TradeLoanFacilityFormulaField.valueOf(entry.getValue());
                    mappings.put(key, field);
                }
            }
            return mappings;

        } catch (Exception e) {
            return new HashMap<>(defaultFieldMappings);
        }
    }

    private static class FormulaData {
        public String expression;
        public String mappings;
        public Set<Character> variables;

        public FormulaData() {}

        public FormulaData(String expression, String mappings, Set<Character> variables) {
            this.expression = expression;
            this.mappings = mappings;
            this.variables = variables;
        }
    }
}
