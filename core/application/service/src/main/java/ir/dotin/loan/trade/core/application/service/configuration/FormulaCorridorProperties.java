package ir.dotin.loan.trade.core.application.service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import ir.dotin.loan.trade.core.application.service.shared.formula.FormulaEvaluationEngine;

import lombok.Data;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "trade.features.formula")
public class FormulaCorridorProperties {

    private boolean validateInFcbEnabled = false;

    private boolean crossCheckEnabled = false;

    private FormulaEvaluationEngine engine = FormulaEvaluationEngine.EXPRESSION_KIT;
}
