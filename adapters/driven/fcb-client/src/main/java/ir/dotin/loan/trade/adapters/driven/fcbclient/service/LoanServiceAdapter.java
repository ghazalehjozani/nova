package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.LoanOperationType;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CoveredBranchesResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.EconomicalSectionResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.LoanTopicResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ReasonTypeResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ResourceResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.LoanMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.LoanTopicInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceAdapter implements LoanServicePort {

    private static final String PARAMETER_SEPARATOR = "#";

    private final FcbService fcbService;
    private final FcbBaseRequestBuilder requestBuilder;

    @Override
    public Result<EconomicSector> loadEconomicalSectorByCode(EconomicSector economicSector) {
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
    public Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
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

        Result<EconomicalSectorValidation> domainResult = LoanMapper.mapToDomainValidation(fcbResponse);

        if (!domainResult.isFailure()) {
            EconomicalSectorValidation validation = domainResult.orElseThrow();
            log.info(
                    "Economical section validation completed: valid={}, message={}",
                    validation.isValid(),
                    validation.message());
        }

        return domainResult;
    }

    public Result<ReasonType> loadReasonType(String reasonTypeCode, FacilityStatus facilityStatus) {

        LoanOperationType operation = LoanMapper.FACILITY_STATUS_TO_OPERATION_MAPPING.get(facilityStatus);
        List<Parameter> parameters = buildParameters(reasonTypeCode, operation);

        Usecases usecases = requestBuilder.buildUseCase("load-reason-type", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-reason-type usecase");

        Result<ReasonTypeResponse> fcbResult = fcbService.executeUsecase(fcbRequest, ReasonTypeResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load reason type failed: {}", fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        ReasonTypeResponse fcbResponse = fcbResult.orElseThrow();

        Result<ReasonType> domainResult = LoanMapper.mapToDomainReasonType(fcbResponse);

        return domainResult;
    }

    private List<Parameter> buildParameters(String reasonTypeCode, LoanOperationType operation) {
        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
                Parameter.builder().key("reasonTypeCode").value(reasonTypeCode).build());

        parameters.add(
                Parameter.builder().key("loanOperation").value(operation.name()).build());

        return parameters;
    }

    @Override
    public Result<LoanTopicInfo> loadTopicByCode(List<String> topicCodes) {
        log.info("Loading topic by codes for bill service: count={}, codes={}", topicCodes.size(), topicCodes);

        List<Parameter> parameters = buildLoanTopicParameters(topicCodes);

        Usecases usecases = requestBuilder.buildUseCase("load-topic-by-code-for-bill-service", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-topic-by-code-for-bill-service usecase");

        Result<LoanTopicResponse> fcbResult = fcbService.executeUsecase(fcbRequest, LoanTopicResponse.class);

        if (fcbResult.isFailure()) {
            log.error("FCB load topic failed: {}", fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        LoanTopicResponse fcbResponse = fcbResult.orElseThrow();

        Result<LoanTopicInfo> domainResult = LoanMapper.mapToLoanTopic(fcbResponse);

        if (!domainResult.isFailure()) {
            LoanTopicInfo topic = domainResult.orElseThrow();
            log.info(
                    "Topic loaded successfully: mainDebtor={}, bankCommitments={}",
                    topic.mainTopicIsDebtor(),
                    topic.bankCommitmentsTopicIsDebtor());
        }

        return domainResult;
    }

    private List<Parameter> buildLoanTopicParameters(List<String> topicCodes) {
        List<Parameter> parameters = new ArrayList<>();

        String topicsValue = String.join(PARAMETER_SEPARATOR, topicCodes);
        parameters.add(Parameter.builder().key("topics").value(topicsValue).build());

        log.debug("Built topic parameters: topics={}", topicsValue);
        return parameters;
    }

    @Override
    public Result<SubSource> loadResourceByCode(String resourceCode) {
        log.info("Loading resource by code: {}", resourceCode);

        if (resourceCode == null || resourceCode.isBlank()) {
            log.error("Resource code cannot be null or blank");
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_BAD_REQUEST, "Resource code is required"));
        }

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(
                Parameter.builder().key("resourceCode").value(resourceCode).build());

        Usecases usecases = requestBuilder.buildUseCase("load-resource-by-code", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-resource-by-code usecase");

        Result<ResourceResponse> fcbResult = fcbService.executeUsecase(fcbRequest, ResourceResponse.class);

        if (fcbResult.isFailure()) {
            log.error("FCB load resource failed: {}", fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        ResourceResponse fcbResponse = fcbResult.orElseThrow();

        Result<SubSource> domainResult = LoanMapper.mapToResource(fcbResponse);

        if (!domainResult.isFailure()) {
            SubSource resource = domainResult.orElseThrow();
            log.info("Resource loaded successfully: code={}", resource.code());
        }

        return domainResult;
    }

    @Override
    public Result<List<BranchCode>> loadCoveredBranches(BranchCode branchCode) {
        log.info("Loading covered branches for branch code: {}", branchCode);

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
                Parameter.builder().key("branchCode").value(branchCode.value()).build());

        Usecases usecases = requestBuilder.buildUseCase("load-covered-branches", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-covered-branches usecase");

        Result<CoveredBranchesResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, CoveredBranchesResponse.class);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load covered branches failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        CoveredBranchesResponse fcbResponse = fcbResult.orElseThrow();

        return LoanMapper.mapToBranchCodeList(fcbResponse);
    }
}
