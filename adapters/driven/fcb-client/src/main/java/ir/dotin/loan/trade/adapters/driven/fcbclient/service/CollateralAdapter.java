package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.AssuranceResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ReserveAssuranceForFileResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.UnReserveAssuranceForFileResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.CollateralMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
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
    public Result<CollateralDetails> loadCollateral(String assuranceSerial, String uniqueTrackingCode) {

        log.info("Loading assurance: serial={}, trackingCode={}", assuranceSerial, uniqueTrackingCode);

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(Parameter.builder()
                .key("assuranceSerial")
                .value(assuranceSerial)
                .build());

        parameters.add(Parameter.builder()
                .key("uniqueTrackingCode")
                .value(uniqueTrackingCode)
                .build());

        Usecases usecases = requestBuilder.buildUseCase("load-assurance-service", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-assurance-service usecase");

        Result<AssuranceResponse> fcbResult = fcbService.executeUsecase(fcbRequest, AssuranceResponse.class);

        if (fcbResult.isFailure()) {
            log.error("FCB load assurance failed: {}", fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        AssuranceResponse fcbResponse = fcbResult.orElseThrow();

        Result<CollateralDetails> domainResult = CollateralMapper.mapToCollateralDetails(fcbResponse);

        if (domainResult.isSuccess()) {
            CollateralDetails collateralDetails = domainResult.orElseThrow();
            log.info(
                    "Assurance loaded: serial={}, type={}, amount={}",
                    collateralDetails.serial(),
                    collateralDetails.assuranceTypeName(),
                    collateralDetails.guaranteeAmount());
        }

        return domainResult;
    }

    @Override
    public Result<List<CollateralSerial>> reserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID requestId,
            Integer reserveDurationMin,
            Money usedAmount) {

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
                        .value(requestId.toString())
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

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        return CollateralMapper.mapToCollateralSerials(result.getValue());
    }

    @Override
    public Result<CollateralSerial> unReserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID transactionId,
            UUID rollBackId) {

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
                        .key("rollBackId")
                        .value(rollBackId.toString())
                        .build());

        Usecases usecases = requestBuilder.buildUseCase("un-reserve-assurance-for-file", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();
        Map<String, Object> additionalContext = new HashMap<>();
        additionalContext.put("assuranceSerial", collateralSerial.value());
        additionalContext.put("fileNumber", applicationNumber.derivedValue());
        additionalContext.put("transactionId", transactionId.toString());
        additionalContext.put("rollBackId", rollBackId.toString());
        FcbContext fcbContext =
                FcbContext.builder().additionalContext(additionalContext).build();
        Result<UnReserveAssuranceForFileResponse> result =
                fcbService.executeUsecase(fcbRequest, UnReserveAssuranceForFileResponse.class, fcbContext);

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        return Result.success(collateralSerial);
    }
}
