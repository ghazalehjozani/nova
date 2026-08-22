package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.ScheduleSource;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;

import static java.util.Objects.requireNonNull;

@Component
public class ProductProfileResolver {

    // why: keyed on scheduleSource rather than schedule type — the equal-instalments use case legitimately serves
    // both SINGLE_INSTALLMENT and EQUAL_INSTALLMENTS, which share a source but not a type.
    public Result<ProductProfile> resolveFor(FacilityOriginationContext context, ScheduleSource servedSource) {
        return ProductProfile.from(
                        context.arrangement().getDisbursementType(),
                        requireNonNull(context.arrangement().getInstallmentPolicy())
                                .installmentPaymentType())
                .flatMap(profile -> profile.scheduleSource() == servedSource
                        ? Result.success(profile)
                        : Result.failure(
                                OriginateLoanFacilityErrorCodes.PRODUCT_NOT_SERVED_BY_THIS_USE_CASE,
                                profile.getLocalizationKey(),
                                servedSource));
    }
}
