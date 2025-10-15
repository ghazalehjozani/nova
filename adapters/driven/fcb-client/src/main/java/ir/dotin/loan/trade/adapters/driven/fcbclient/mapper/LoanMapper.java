package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicalSectionValidation;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.EconomicalSectionResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.EconomicalSectorValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LoanMapper {

    public Result<EconomicSector> mapToDomainEconomicalSection(EconomicalSectionResponse fcbResponse) {

        try {
            if (fcbResponse.getCode() == null || fcbResponse.getCode().isBlank()) {
                log.error("FCB response missing economical section code");
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                        "Economical section code is missing in response"));
            }

            if (fcbResponse.getName() == null || fcbResponse.getName().isBlank()) {
                log.error("FCB response missing economical section name");
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                        "Economical section name is missing in response"));
            }

            EconomicSector economicSector = new EconomicSector(fcbResponse.getCode(), fcbResponse.getName());

            return Result.success(economicSector);

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain EconomicalSection", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse economical section: " + e.getMessage()));
        }
    }

    public Result<EconomicalSectionValidation> mapToDomainValidation(EconomicalSectorValidationResponse fcbResponse) {
        try {
            EconomicalSectionValidation validation =
                    new EconomicalSectionValidation(fcbResponse.isValid(), fcbResponse.getSuccessMessage());

            return Result.success(validation);

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain EconomicalSectionValidation", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse validation result: " + e.getMessage()));
        }
    }
}
