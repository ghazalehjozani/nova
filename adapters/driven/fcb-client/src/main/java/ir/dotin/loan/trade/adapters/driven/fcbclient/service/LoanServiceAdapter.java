package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.feignclient.EconomicalSectionValidation;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.EconomicalSectionResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.LoanMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceAdapter implements LoanServicePort {

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    @Override
    public Result<EconomicSector> loadEconomicalSectionByCode(EconomicSector economicSector) {
        log.info("Loading economical section by code: {}", economicSector.code());

        if (economicSector.code().isBlank()) {
            log.error("Economical section code cannot be null or blank");
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Economical section code is required"));
        }

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder()
                .key("economicalSectionCode")
                .value(economicSector.code())
                .build());

        Usecases usecases = requestBuilder.buildUseCase("load-economicalSection-by-code", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load economical section usecase");

        Result<EconomicalSectionResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, EconomicalSectionResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load economical section failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        EconomicalSectionResponse fcbResponse = fcbResult.orElseThrow();

        Result<EconomicSector> domainResult = LoanMapper.mapToDomainEconomicalSection(fcbResponse);

        if (!domainResult.isFailure()) {
            log.info(
                    "Economical section loaded successfully: code={}, name={}",
                    fcbResponse.getCode(),
                    fcbResponse.getName());
        }

        return domainResult;
    }

    @Override
    public Result<EconomicalSectionValidation> validateEconomicalSectionForLoanType(
            EconomicSector economicSector, LoanTypeCode loanTypeCode) {

        log.info("Validating economical section {} for loan type {}", economicSector.code(), loanTypeCode.value());

        Notification inputValidation = Notification.create();
        if (loanTypeCode.value().isBlank()) {
            inputValidation.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Economical section code is required");
        }
        if (loanTypeCode.value().isBlank()) {
            inputValidation.addError(FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Loan type code is required");
        }

        if (inputValidation.hasErrors()) {
            log.error("Validation input parameters failed: {}", inputValidation.getErrorMessages());
            return Result.failure(inputValidation);
        }

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder()
                .key("economicalSectionCode")
                .value(economicSector.code())
                .build());
        parameters.add(Parameter.builder()
                .key("loanTypeCode")
                .value(loanTypeCode.value())
                .build());

        Usecases usecases = requestBuilder.buildUseCase("validate-ecoSection-loanType", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB validate economical section usecase");

        Result<FcbValidationResponse> fcbResult = fcbService.executeUsecase(fcbRequest, FcbValidationResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB validate economical section failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        FcbValidationResponse fcbResponse = fcbResult.orElseThrow();

        Result<EconomicalSectionValidation> domainResult = LoanMapper.mapToDomainValidation(fcbResponse);

        if (!domainResult.isFailure()) {
            EconomicalSectionValidation validation = domainResult.orElseThrow();
            log.info(
                    "Economical section validation completed: valid={}, message={}",
                    validation.isValid(),
                    validation.message());
        }

        return domainResult;
    }
}
