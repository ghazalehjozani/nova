package ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy;

import org.springframework.stereotype.Component;

import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstallmentScheduleStrategySelector {

    private final GradualScheduleStrategy gradualStrategy;
    private final StandardScheduleStrategy standardStrategy;

    public InstallmentScheduleStrategy selectStrategy(InstallmentPaymentType paymentType) {
        log.debug("Selecting strategy for payment type: {}", paymentType);

        return switch (paymentType) {
            case GRADUAL -> gradualStrategy;
            case ONE_TIME, SCHEDULED -> standardStrategy;
        };
    }
}
