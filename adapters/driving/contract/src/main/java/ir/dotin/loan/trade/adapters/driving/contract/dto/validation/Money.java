package ir.dotin.loan.trade.adapters.driving.contract.dto.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Digits;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * SAW.101 §3.1.2.6 money constraint: a monetary amount carried as a string with at most 4 decimal places. Composes
 * {@link Digits} (≤4 fraction digits with a generous integer bound). Apply to monetary request fields only — interest
 * rates and percentages may legitimately carry more precision and MUST NOT use this.
 */
@Documented
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Digits(integer = 19, fraction = 4)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface Money {

    String message() default "amount must be a number with at most 4 decimal places";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
