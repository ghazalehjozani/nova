package ir.dotin.loan.trade.core.application.service.shared.formula;

import ir.dotin.platform.formula.api.exception.CyclicReferenceException;
import ir.dotin.platform.formula.api.exception.EvaluationException;
import ir.dotin.platform.formula.api.exception.FormulaException;
import ir.dotin.platform.formula.api.exception.FormulaNotFoundException;
import ir.dotin.platform.formula.api.exception.NestingDepthExceededException;
import ir.dotin.platform.formula.api.exception.ParseException;
import ir.dotin.platform.formula.api.exception.ProviderResolutionException;
import ir.dotin.platform.formula.service.exception.FormulaAlreadyExistsException;
import ir.dotin.platform.formula.service.exception.FormulaDependencyException;
import ir.dotin.platform.formula.service.exception.InvalidFormulaException;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;

public final class FormulaFailureTranslator {

    private FormulaFailureTranslator() {}

    public static FailureCause toFailureCause(String code, RuntimeException exception) {
        return switch (exception) {
            case FormulaNotFoundException ignored ->
                FailureCause.notFound(Notification.ofError(TradeLoanApplicationServiceErrors.FORMULA_NOT_FOUND, code));
            case FormulaAlreadyExistsException ignored ->
                FailureCause.conflict(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FORMULA_ALREADY_EXISTS, code));
            case FormulaDependencyException ignored ->
                FailureCause.conflict(
                        Notification.ofError(TradeLoanApplicationServiceErrors.FORMULA_HAS_DEPENDENTS, code));
            case InvalidFormulaException invalid ->
                FailureCause.validation(
                        Notification.ofError(TradeLoanApplicationServiceErrors.INVALID_FORMULA, code, reason(invalid)));
            case ParseException parse ->
                FailureCause.validation(
                        Notification.ofError(TradeLoanApplicationServiceErrors.INVALID_FORMULA, code, reason(parse)));
            case ProviderResolutionException provider -> providerFailure(code, provider);
            case CyclicReferenceException cyclic ->
                FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FORMULA_EVALUATION_FAILED, code, reason(cyclic)));
            case NestingDepthExceededException nesting ->
                FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FORMULA_EVALUATION_FAILED, code, reason(nesting)));
            case EvaluationException evaluation ->
                FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FORMULA_EVALUATION_FAILED, code, reason(evaluation)));
            case FormulaException formula ->
                FailureCause.businessRule(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FORMULA_EVALUATION_FAILED, code, reason(formula)));
            case IllegalArgumentException illegal ->
                FailureCause.validation(
                        Notification.ofError(TradeLoanApplicationServiceErrors.INVALID_FORMULA, code, reason(illegal)));
            default -> FailureCause.technical(exception, false);
        };
    }

    private static String reason(RuntimeException exception) {
        String message = exception.getMessage();
        return message != null ? message : exception.getClass().getSimpleName();
    }

    public static FailureCauseException toException(String code, RuntimeException exception) {
        return new FailureCauseException(toFailureCause(code, exception));
    }

    private static FailureCause providerFailure(String code, ProviderResolutionException provider) {
        if (provider.isAggregateNotFound()) {
            String aggregateId = provider.getAggregateId();
            return FailureCause.notFound(Notification.ofError(
                    TradeLoanApplicationServiceErrors.FORMULA_NOT_FOUND, aggregateId != null ? aggregateId : code));
        }
        return FailureCause.validation(Notification.ofError(
                TradeLoanApplicationServiceErrors.FORMULA_EVALUATION_FAILED, code, reason(provider)));
    }
}
