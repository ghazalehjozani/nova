package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ReserveAssuranceForFileResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.CollateralMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralValidation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollateralAdapter implements CollateralServicePort {

    private static final String PARAMETER_SEPARATOR = "#";

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    @Override
    public Result<CollateralValidation> validateAddAssuranceToFile(
            List<CollateralSerial> collateralSerialList, List<Long> usedCosts) {

        log.info("Validating add assurance to file: serials={}, costs={}", collateralSerialList, usedCosts);

        Notification inputValidation = validateInputs(collateralSerialList, usedCosts);
        if (inputValidation.hasErrors()) {
            log.error("Input validation failed: {}", inputValidation.getErrorMessages());
            return Result.failure(inputValidation);
        }

        List<Parameter> parameters = buildAssuranceParameters(collateralSerialList, usedCosts);

        Usecases usecases = requestBuilder.buildUseCase("validate-add-assurance-to-file", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB validate assurance usecase");

        Result<FcbValidationResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, FcbValidationResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB validate assurance failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        FcbValidationResponse fcbResponse = fcbResult.orElseThrow();

        Result<CollateralValidation> domainResult = CollateralMapper.mapToDomainAssuranceValidation(fcbResponse);

        if (!domainResult.isFailure()) {
            CollateralValidation validation = domainResult.orElseThrow();
            log.info(
                    "Assurance validation completed: valid={}, message={}", validation.isValid(), validation.message());
        }

        return domainResult;
    }

    private Notification validateInputs(List<CollateralSerial> assuranceSerials, List<Long> usedCosts) {
        Notification notification = Notification.create();

        if (assuranceSerials == null || assuranceSerials.isEmpty()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Assurance serials list cannot be null or empty");
        }

        if (usedCosts == null || usedCosts.isEmpty()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Used costs list cannot be null or empty");
        }

        if (notification.hasErrors()) {
            return notification;
        }

        if (assuranceSerials.size() != usedCosts.size()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST,
                    String.format(
                            "Assurance serials count (%d) must match used costs count (%d)",
                            assuranceSerials.size(), usedCosts.size()));
        }

        if (assuranceSerials.stream().anyMatch(Objects::isNull)) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST,
                    "Assurance serials cannot contain null or blank values");
        }

        if (usedCosts.stream().anyMatch(Objects::isNull)) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Used costs cannot contain null values");
        }

        return notification;
    }

    private List<Parameter> buildAssuranceParameters(List<CollateralSerial> assuranceSerials, List<Long> usedCosts) {

        List<Parameter> parameters = new ArrayList<>();

        String serialsValue =
                assuranceSerials.stream().map(String::valueOf).collect(Collectors.joining(PARAMETER_SEPARATOR));
        parameters.add(
                Parameter.builder().key("assuranceSerial").value(serialsValue).build());

        String costsValue = usedCosts.stream().map(String::valueOf).collect(Collectors.joining(PARAMETER_SEPARATOR));
        parameters.add(Parameter.builder().key("usedCost").value(costsValue).build());

        log.debug("Built assurance parameters - serials: {}, costs: {}", serialsValue, costsValue);

        return parameters;
    }

    @Override
    public Result<List<CollateralSerial>> reserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID transactionId,
            Integer reserveDurationMin,
            Money usedAmount) {
        log.debug(
                "Reserving assurance for file - Assurance Serial: {}, File Number: {}, Transaction ID: {}, "
                        + "Reserve Duration: {} minutes, Amount: {}",
                collateralSerial,
                applicationNumber,
                transactionId,
                reserveDurationMin,
                usedAmount);

        List<Parameter> parameters = Arrays.asList(
                Parameter.builder()
                        .key("assuranceSerial")
                        .value(collateralSerial.value())
                        .build(),
                Parameter.builder()
                        .key("fileNumber")
                        .value(applicationNumber.derivedValue())
                        .build(),
                Parameter.builder()
                        .key("transactionId")
                        .value(transactionId.toString())
                        .build(),
                Parameter.builder()
                        .key("reserveDurationMin")
                        .value(String.valueOf(reserveDurationMin))
                        .build(),
                Parameter.builder()
                        .key("amount")
                        .value(usedAmount.value().toString())
                        .build());

        Usecases usecases = requestBuilder.buildUseCase("reserve-assurance-for-file", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<ReserveAssuranceForFileResponse> result =
                fcbService.executeUsecase(fcbRequest, ReserveAssuranceForFileResponse.class, FcbContext.empty());

        if (result.isSuccess()) {
            log.debug("Assurance reserved successfully for file: {}", applicationNumber.derivedValue());
            ReserveAssuranceForFileResponse response = result.getValue();
            log.debug("Reserved assurance serials: {}", response.getAssuranceSerials());
        } else {
            log.error(
                    "Failed to reserve assurance for file: {}. Errors: {}",
                    applicationNumber.derivedValue(),
                    result.notification().errors());
        }

        return CollateralMapper.mapToCollateralSerials(result.getValue());
    }
}
