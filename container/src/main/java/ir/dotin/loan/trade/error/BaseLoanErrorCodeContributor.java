package ir.dotin.loan.trade.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.error.CodedMessage;
import ir.dotin.platform.pangaea.protocol.api.error.ErrorCodeContributor;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.error.InstallmentScheduleErrors;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.error.InterestCalculationMessages;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.error.LoanArrangementErrors;
import ir.dotin.loan.baseloan.core.domain.loanfacility.error.LoanFacilityErrors;
import ir.dotin.loan.baseloan.core.domain.loantype.error.LoanTypeErrors;
import ir.dotin.loan.baseloan.core.domain.shared.error.LoanValidationErrors;

/**
 * Contributes all error codes from the BaseLoan shared kernel.
 *
 * <p>Registered with {@code @Order(0)} so shared kernel codes load before product-specific codes, making collision
 * messages point to the product side.
 *
 * @since 2.0
 */
@Component
@Order(0)
public class BaseLoanErrorCodeContributor implements ErrorCodeContributor {

    @Override
    public Collection<? extends CodedMessage<?>> contributedCodes() {
        var all = new ArrayList<CodedMessage<?>>();
        Collections.addAll(all, InstallmentScheduleErrors.values());
        Collections.addAll(all, InterestCalculationMessages.values());
        Collections.addAll(all, LoanArrangementErrors.values());
        Collections.addAll(all, LoanFacilityErrors.values());
        Collections.addAll(all, LoanTypeErrors.values());
        Collections.addAll(all, LoanValidationErrors.values());
        return Collections.unmodifiableList(all);
    }
}
