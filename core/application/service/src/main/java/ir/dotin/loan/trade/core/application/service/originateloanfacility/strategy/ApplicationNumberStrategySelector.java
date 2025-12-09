package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ir.dotin.loan.trade.core.application.service.configuration.TradeApplicationNumberConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApplicationNumberStrategySelector {

    private final Map<ApplicationNumberGenerationType, ApplicationNumberGenerationStrategy> strategies;
    private final TradeApplicationNumberConfiguration configuration;

    public ApplicationNumberStrategySelector(
            List<ApplicationNumberGenerationStrategy> strategyList, TradeApplicationNumberConfiguration configuration) {

        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ApplicationNumberGenerationStrategy::getType, Function.identity()));

        this.configuration = configuration;
    }

    public ApplicationNumberGenerationStrategy selectStrategy() {
        ApplicationNumberGenerationType type = configuration.generationType();

        ApplicationNumberGenerationStrategy strategy = strategies.get(type);

        if (strategy == null) {
            strategy = strategies.get(ApplicationNumberGenerationType.FCB_VALIDATION);
        }

        return strategy;
    }

    public ApplicationNumberGenerationStrategy selectStrategy(ApplicationNumberGenerationType type) {
        ApplicationNumberGenerationStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for type: " + type);
        }

        return strategy;
    }
}
