package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerBirthInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.RelatedCustomersResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.CustomerMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyBirthInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceAdapter implements CustomerServicePort {

    private static final String ITEM_SEPARATOR = "#";

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    @Override
    public Result<PartyInfo> loadCustomerInfo(
            String customerNumber,
            @NotNull PartyRole role,
            @Nullable BigDecimal guaranteePercentage,
            CustomerInfoLoadOptions options) {

        log.info("Loading customer info: customerNumber={}, options={}", customerNumber, options);

        Notification inputValidation = validateInputs(customerNumber, options);
        if (inputValidation.hasErrors()) {
            log.error("Input validation failed: {}", inputValidation.getErrorMessages());
            return Result.failure(inputValidation);
        }

        List<Parameter> parameters = buildCustomerInfoParameters(customerNumber, options);
        Usecases usecases = requestBuilder.buildUseCase("load-customer-info", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-customer-info usecase");

        Result<CustomerInfoResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, CustomerInfoResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load-customer-info failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        CustomerInfoResponse fcbResponse = fcbResult.orElseThrow(); // TODO: add exception

        if (!fcbResponse.getRsCode().contains("01")) {
            log.error(
                    "FCB business error: rsCode={}, error={}",
                    fcbResponse.getRsCode(),
                    fcbResponse.getErrorDescription());
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_BUSINESS_EXCEPTION, fcbResponse.getErrorDescription()));
        }

        Result<PartyInfo> mappingResult =
                CustomerMapper.mapToDomainCustomerInfo(fcbResponse, role, guaranteePercentage);

        if (mappingResult.isFailure()) {
            log.error(
                    "Failed to map FCB response: {}",
                    mappingResult.notification().getErrorMessages());
            return mappingResult;
        }

        log.info("Successfully loaded customer info for: {}", customerNumber);
        return mappingResult;
    }

    private Notification validateInputs(String customerNumber, CustomerInfoLoadOptions options) {
        Notification notification = Notification.create();

        if (customerNumber == null || customerNumber.isBlank()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Customer number cannot be null or blank");
        }

        if (options == null) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Customer info load options cannot be null");
        }

        return notification;
    }

    private List<Parameter> buildCustomerInfoParameters(String customerNumber, CustomerInfoLoadOptions options) {

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
                Parameter.builder().key("customerNumber").value(customerNumber).build());

        parameters.add(Parameter.builder()
                .key("sequenceCode")
                .value(options.sequenceCode())
                .build());

        parameters.add(
                Parameter.builder().key("subsystem").value(options.subsystem()).build());

        parameters.add(Parameter.builder()
                .key("includeCapability")
                .value(String.valueOf(options.includeCapability()))
                .build());

        parameters.add(Parameter.builder()
                .key("includeBlackList")
                .value(String.valueOf(options.includeBlackList()))
                .build());

        parameters.add(Parameter.builder()
                .key("includeBaseInfo")
                .value(String.valueOf(options.includeBaseInfo()))
                .build());

        parameters.add(Parameter.builder()
                .key("includeGrayList")
                .value(String.valueOf(options.includeGrayList()))
                .build());

        log.debug("Built customer info parameters for customer: {}", customerNumber);

        return parameters;
    }

    private List<Parameter> buildFindRelatedCustomersParameters(List<String> customerNumbers) {
        List<Parameter> parameters = new ArrayList<>();

        String customerNumbersValue = String.join(ITEM_SEPARATOR, customerNumbers);

        parameters.add(Parameter.builder()
                .key("customerNumbers")
                .value(customerNumbersValue)
                .build());

        log.debug("Built find related customers parameters: customerNumbers={}", customerNumbersValue);

        return parameters;
    }

    @Override
    public Result<List<PartyInfo>> findRelatedCustomers(List<String> customerNumbers) {
        log.info("Finding related customers: count={}, numbers={}", customerNumbers.size(), customerNumbers);

        List<Parameter> parameters = buildFindRelatedCustomersParameters(customerNumbers);

        Usecases usecases = requestBuilder.buildUseCase("find-related-customers", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB find-related-customers usecase");

        Result<RelatedCustomersResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, RelatedCustomersResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB find-related-customers failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        RelatedCustomersResponse fcbResponse = fcbResult.orElseThrow();

        return CustomerMapper.mapToCustomerInfoList(fcbResponse.getCustomers());
    }

    @Override
    public Result<PartyBirthInfo> loadCustomerBirthInfo(String customerNumber) {
        log.info("Loading customer birth info: customerNumber={}", customerNumber);

        if (customerNumber == null || customerNumber.isBlank()) {
            log.error("Customer number cannot be null or blank");
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Customer number is required"));
        }

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(
                Parameter.builder().key("customerNumber").value(customerNumber).build());

        Usecases usecases = requestBuilder.buildUseCase("load-customer-birth-info", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-customer-birth-info usecase");

        Result<CustomerBirthInfoResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, CustomerBirthInfoResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load customer birth info failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        CustomerBirthInfoResponse fcbResponse = fcbResult.orElseThrow();

        return CustomerMapper.mapToCustomerBirthInfo(fcbResponse);
    }
}
