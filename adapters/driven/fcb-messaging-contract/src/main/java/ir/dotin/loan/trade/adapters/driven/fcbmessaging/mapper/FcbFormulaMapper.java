package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.NotificationError;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.EvaluateFormulaViaFcbResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ValidateFormulaResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FcbFormulaMapper {

    public static final Map<String, String> FRIENDLY_TO_FCB_ALIAS = Map.of(
            "approvedAmount", "LOAN_APPROVED_AMOUNT",
            "requestedAmount", "LOAN_REQUESTED_AMOUNT",
            "durationMonths", "LOAN_DURATION",
            "installmentCount", "INSTALLMENT_COUNT",
            "gracePeriodMonths", "BREAK_PERIOD_MONTH_DURATION",
            "interestRate", "LOAN_RATE",
            "penaltyRate", "LOAN_PENALTY_RATE");

    public static Map<String, String> translateToFcbAliases(Map<String, BigDecimal> friendlyVars) {
        Map<String, String> aliasValues = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : friendlyVars.entrySet()) {
            String fcbCode = FRIENDLY_TO_FCB_ALIAS.get(entry.getKey());
            if (fcbCode == null) {
                log.debug("Skipping friendly formula var '{}' with no FCB alias", entry.getKey());
                continue;
            }
            aliasValues.put(fcbCode, entry.getValue().toPlainString());
        }
        return aliasValues;
    }

    public static Result<Unit> mapValidation(String code, ValidateFormulaResponse response) {
        if (response.isValid()) {
            return Result.success();
        }
        String violations = response.getViolations() != null ? String.join("; ", response.getViolations()) : "";
        return Result.failure(
                Notification.ofError(NotificationError.of(CoreBankingErrors.FORMULA_INVALID_IN_FCB, code, violations)));
    }

    public static Result<BigDecimal> mapEvaluation(String code, EvaluateFormulaViaFcbResponse response) {
        String result = response.getResult();
        if (result == null || result.isBlank()) {
            return Result.failure(CoreBankingErrors.FORMULA_EVALUATION_FAILED_IN_FCB, code);
        }
        return Result.success(new BigDecimal(result));
    }
}
