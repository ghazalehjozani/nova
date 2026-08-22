package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.AccountNumberValidationRule;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.DepositValidationRules;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.EconomicSectorValidationRules;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.RequestReasonValidationRule;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.SamatValidationRule;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.validation.SubSourceValidationRule;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityValidator {

    private final DepositValidationRules depositValidationRules;
    private final EconomicSectorValidationRules economicSectorValidationRules;
    private final SamatValidationRule samatValidationRule;
    private final AccountNumberValidationRule accountNumberValidationRule;
    private final RequestReasonValidationRule requestReasonValidationRule;
    private final SubSourceValidationRule subSourceValidationRule;

    @WithSpan("facility.validate.fanout")
    public Result<Unit> callAndValidateServices(OriginateFacilityCommand command) {
        log.debug("Call and validate services for facility origination");

        List<Supplier<Result<Unit>>> tasks = List.of(
                () -> depositValidationRules.validateDeposit(command),
                () -> accountNumberValidationRule.validateAccountNumber(command),
                () -> depositValidationRules.isDepositClosed(command),
                () -> economicSectorValidationRules.validateEconomicalSector(command),
                () -> economicSectorValidationRules.validateEconomicalSectionForLoanType(command),
                () -> depositValidationRules.validateDebtorDeposit(command),
                () -> depositValidationRules.validateCreditorDeposit(command),
                () -> subSourceValidationRule.validateSubSource(command),
                () -> depositValidationRules.validateDepositCurrency(command),
                () -> samatValidationRule.validateSamat(command),
                () -> requestReasonValidationRule.validateRequestReason(command));

        return ParallelFanout.allVoid(tasks);
    }
}
