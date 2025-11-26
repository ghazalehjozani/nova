package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ReserveAssuranceForFileResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CollateralMapper {

    public Result<CollateralValidation> mapToDomainAssuranceValidation(FcbValidationResponse fcbResponse) {

        CollateralValidation collateralValidation =
                new CollateralValidation(fcbResponse.isValid(), fcbResponse.getSuccessMessage());

        return Result.success(collateralValidation);
    }

    public Result<List<CollateralSerial>> mapToCollateralSerials(ReserveAssuranceForFileResponse response) {

        if (response == null) {
            log.error("Cannot map null ReserveAssuranceForFileResponse");
            return Result.failure(
                    Notification.ofError(FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Response is null"));
        }

        List<String> assuranceSerials = response.getAssuranceSerials();

        if (assuranceSerials == null || assuranceSerials.isEmpty()) {
            log.warn("No assurance serials found in response");
            return Result.success(Collections.emptyList());
        }

        log.debug("Mapping {} assurance serials to CollateralSerial objects", assuranceSerials.size());

        return mapSerials(assuranceSerials);
    }

    public Result<List<CollateralSerial>> mapSerials(List<String> serials) {
        if (serials == null || serials.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<CollateralSerial> collateralSerials = new ArrayList<>();
        Notification aggregatedErrors = Notification.ofError();
        int successCount = 0;
        int failureCount = 0;

        for (int index = 0; index < serials.size(); index++) {
            String serial = serials.get(index);

            Result<CollateralSerial> result = CollateralSerial.of(serial);

            if (result.isSuccess()) {
                collateralSerials.add(result.getValue());
                successCount++;
                log.debug("Successfully mapped serial at index {}: {}", index, serial);
            } else {
                failureCount++;
                log.warn(
                        "Failed to map serial at index {}: {}. Errors: {}",
                        index,
                        serial,
                        result.notification().errors());
            }
        }

        log.info(
                "Mapping complete: {} successful, {} failed out of {} serials",
                successCount,
                failureCount,
                serials.size());

        if (failureCount > 0) {
            return Result.failure(aggregatedErrors);
        }

        return Result.success(collateralSerials);
    }
}
