package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.feignclient.CollateralValidation;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CollateralMapper {

    public Result<CollateralValidation> mapToDomainAssuranceValidation(FcbValidationResponse fcbResponse) {
        try {
            Result<CollateralValidation> result =
                    CollateralValidation.of(fcbResponse.isValid(), fcbResponse.getSuccessMessage());

            if (result.isFailure()) {
                log.error(
                        "Failed to create AssuranceValidation: {}",
                        result.notification().getErrorMessages());
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain AssuranceValidation", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse assurance validation result: " + e.getMessage()));
        }
    }
}
