package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositClosedResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.DepositInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ValidateCreditorDepositResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ValidateDebtorDepositResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.DepositMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DebtorCreditorDepositValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.DepositClosedStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositServiceAdapter implements DepositServicePort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    public Result<DepositInfo> getDepositInfo(DepositNumber depositNumber) {
        log.debug("Loading deposit information for: {}", depositNumber);

        Parameter parameter = Parameter.builder()
                .key("depositNumber")
                .value(depositNumber.value())
                .build();

        Usecases usecases = requestBuilder.buildUseCase("load-deposit-by-number", Collections.singletonList(parameter));

        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        Result<DepositInfoResponse> depositInfoResponseResult =
                fcbService.executeUsecase(fcbRequest, DepositInfoResponse.class);

        return DepositMapper.mapToDepositInfo(depositInfoResponseResult.getValue());
    }

    @Override
    public Result<DepositClosedStatus> isDepositClosed(DepositNumber depositNumber, CurrencyType currencyType) {

        log.info(
                "Checking if deposit is closed: depositNumber={}, currency={}",
                depositNumber.value(),
                currencyType.getCode());

        Notification inputValidation = validateDepositInputs(depositNumber.value(), currencyType.getCode());

        if (inputValidation.hasErrors()) {
            log.error("Input validation failed: {}", inputValidation.getErrorMessages());
            return Result.failure(inputValidation);
        }

        List<Parameter> parameters = buildDepositParameters(depositNumber.value(), currencyType.getCode());

        Usecases usecases = requestBuilder.buildUseCase("is-deposit-closed", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB is-deposit-closed usecase");

        Result<DepositClosedResponse> fcbResult = fcbService.executeUsecase(fcbRequest, DepositClosedResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB is-deposit-closed failed: {}", fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        DepositClosedResponse fcbResponse = fcbResult.orElseThrow();

        Result<DepositClosedStatus> result = DepositMapper.mapToDomainDepositClosedStatus(fcbResponse);

        if (!result.isFailure()) {
            DepositClosedStatus status = result.orElseThrow();
            log.info(
                    "Deposit closed status checked: isClosed={}, currency={}",
                    status.isClosed(),
                    status.currencyTypeCode());
        }

        return result;
    }

    @Override
    public Result<DebtorCreditorDepositValidation> validateDebtorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType) {

        log.info(
                "Validating debtor deposit: depositNumber={}, currency={}",
                depositNumber.value(),
                currencyType.getCode());

        Notification inputValidation = validateDepositInputs(depositNumber.value(), currencyType.getCode());

        if (inputValidation.hasErrors()) {
            log.error("Input validation failed: {}", inputValidation.getErrorMessages());
            return Result.failure(inputValidation);
        }

        List<Parameter> parameters = buildDepositParameters(depositNumber.value(), currencyType.getCode());

        Usecases usecases = requestBuilder.buildUseCase("validate-debtor-deposit", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB validate-debtor-deposit usecase");

        Result<ValidateDebtorDepositResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, ValidateDebtorDepositResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB validate-debtor-deposit failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        ValidateDebtorDepositResponse fcbResponse = fcbResult.orElseThrow();

        return DepositMapper.mapToDomainDebtorDepositValidation(fcbResponse);
    }

    private Notification validateDepositInputs(String depositNumber, String currencySwiftCode) {
        Notification notification = Notification.create();

        if (depositNumber == null || depositNumber.isBlank()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Deposit number cannot be null or blank");
        }

        if (currencySwiftCode == null || currencySwiftCode.isBlank()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Currency swift code cannot be null or blank");
        }

        return notification;
    }

    private List<Parameter> buildDepositParameters(String depositNumber, String currencySwiftCode) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
                Parameter.builder().key("depositNumber").value(depositNumber).build());

        parameters.add(Parameter.builder()
                .key("currencySwiftCode")
                .value(currencySwiftCode)
                .build());

        log.debug("Built deposit parameters - depositNumber: {}, currency: {}", depositNumber, currencySwiftCode);

        return parameters;
    }

    @Override
    public Result<DebtorCreditorDepositValidation> validateCreditorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount) {

        log.info(
                "Validating creditor deposit: depositNumber={}, currency={}, amount={}",
                depositNumber.value(),
                currencyType.getCode(),
                amount);

        Notification inputValidation = validateDepositInputs(depositNumber.value(), currencyType.getCode());
        if (inputValidation.hasErrors()) {
            log.error("Input validation failed: {}", inputValidation.getErrorMessages());
            return Result.failure(inputValidation);
        }

        List<Parameter> parameters =
                buildCreditorDepositParameters(depositNumber.value(), currencyType.getCode(), amount);

        Usecases usecases = requestBuilder.buildUseCase("validate-creditor-deposit", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB validate-creditor-deposit usecase");

        Result<ValidateCreditorDepositResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, ValidateCreditorDepositResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB validate-creditor-deposit failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        ValidateCreditorDepositResponse fcbResponse = fcbResult.orElseThrow();

        return DepositMapper.mapToDomainCreditorDepositValidation(fcbResponse);
    }

    private List<Parameter> buildCreditorDepositParameters(
            String depositNumber, String currencySwiftCode, BigDecimal amount) {

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
                Parameter.builder().key("depositNumber").value(depositNumber).build());

        parameters.add(Parameter.builder()
                .key("currencySwiftCode")
                .value(currencySwiftCode)
                .build());

        parameters.add(
                Parameter.builder().key("amount").value(String.valueOf(amount)).build());

        log.debug(
                "Built creditor deposit parameters - depositNumber: {}, currency: {}, amount: {}",
                depositNumber,
                currencySwiftCode,
                amount);

        return parameters;
    }
}
