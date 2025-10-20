package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
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
}
