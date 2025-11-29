package ir.dotin.loan.trade.adapters.driven.fcbclient.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.adapters.driven.fcbclient.context.FcbContext;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.FcbRequest;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Parameter;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.Usecases;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.*;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbclient.mapper.LoanMapper;
import ir.dotin.loan.trade.adapters.driven.fcbclient.util.FcbBaseRequestBuilder;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.TopicInfo;

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

        Map<String, Object> additionalContext = Map.of("economicSectorCode", economicSector.code());
        FcbContext fcbContext =
                FcbContext.builder().additionalContext(additionalContext).build();

        Result<EconomicalSectionResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, EconomicalSectionResponse.class, fcbContext);

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

        Map<String, Object> additionalContext = Map.of(
                "loanTypeCode", loanTypeCode.value(),
                "economicSectorCode", economicSector.code());
        FcbContext fcbContext =
                FcbContext.builder().additionalContext(additionalContext).build();
        Result<FcbValidationResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, FcbValidationResponse.class, fcbContext);

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

    @Override
    public Result<ReasonType> loadReasonTypeForCreate(String reasonTypeCode) {

        log.info("FCB service Loading load-reason-type-for-create for reasonTypeCode :{}", reasonTypeCode);

        Parameter reasonTypeCodeParam =
                Parameter.builder().key("reasonTypeCode").value(reasonTypeCode).build();

        List<Parameter> parameters = List.of(reasonTypeCodeParam);

        Usecases usecases = requestBuilder.buildUseCase("load-reason-type-for-create", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-reason-type-for-create use case");

        Map<String, Object> additionalContext = Map.of("reasonTypeCode", reasonTypeCode);
        FcbContext fcbContext =
                FcbContext.builder().additionalContext(additionalContext).build();

        Result<ReasonTypeResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, ReasonTypeResponse.class, fcbContext);

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load-reason-type-for-create failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        ReasonTypeResponse fcbResponse = fcbResult.orElseThrow();

        Result<ReasonType> mapDtoResult = LoanMapper.mapToDomainReasonType(fcbResponse);
        if (mapDtoResult.isFailure()) {
            log.error(
                    "map load-reason-type-for-create result failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        return mapDtoResult;
    }

    @Override
    public Result<ReasonType> loadReasonTypeForRevoke(String reasonTypeCode) {

        log.info("FCB service Loading load-reason-type-for-revoke for reasonTypeCode :{}", reasonTypeCode);

        Parameter reasonTypeCodeParam =
                Parameter.builder().key("reasonTypeCode").value(reasonTypeCode).build();

        List<Parameter> parameters = List.of(reasonTypeCodeParam);

        Usecases usecases = requestBuilder.buildUseCase("load-reason-type-for-revoke", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-reason-type-for-revoke use case");

        Result<ReasonTypeResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, ReasonTypeResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load-reason-type-for-revoke failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        ReasonTypeResponse fcbResponse = fcbResult.orElseThrow();

        Result<ReasonType> mapDtoResult = LoanMapper.mapToDomainReasonType(fcbResponse);
        if (mapDtoResult.isFailure()) {
            log.error(
                    "map load-reason-type-for-revoke result failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        return mapDtoResult;
    }

    @Override
    public Result<List<TopicInfo>> loadTopicByCode(List<String> topicCodes) {
        log.info("Loading load-topic-by-code: count={}, codes={}", topicCodes.size(), topicCodes);

        List<Parameter> parameters = buildLoanTopicParameters(topicCodes);

        Usecases usecases = requestBuilder.buildUseCase("load-topic-by-code", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB load-topic-by-code use case");
        Result<LoadTopicByCodeResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, LoadTopicByCodeResponse.class, FcbContext.empty());
        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load-topic-by-code failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }
        List<TopicResponse> responses = (List<TopicResponse>) fcbResult.orElseThrow();

        Result<List<TopicInfo>> mapDtoResult = LoanMapper.mapToTopicInfoList(responses);
        if (mapDtoResult.isFailure()) {
            log.error(
                    "map load-topic-by-code failed: {}",
                    mapDtoResult.notification().getErrorMessages());
            return Result.failure(mapDtoResult.notification());
        }
        return mapDtoResult;
    }

    private List<Parameter> buildLoanTopicParameters(List<String> topicCodes) {
        List<Parameter> parameters = new ArrayList<>();

        String topicsValue = String.join(PARAMETER_SEPARATOR, topicCodes);
        parameters.add(Parameter.builder().key("topicCode").value(topicsValue).build());

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

        log.debug("Executing FCB load-resource-by-code use case");

        Map<String, Object> additionalContext = Map.of("resourceCode", resourceCode);
        FcbContext fcbContext =
                FcbContext.builder().additionalContext(additionalContext).build();

        Result<ResourceResponse> fcbResult = fcbService.executeUsecase(fcbRequest, ResourceResponse.class, fcbContext);

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
                fcbService.executeUsecase(fcbRequest, CoveredBranchesResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB load covered branches failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        CoveredBranchesResponse fcbResponse = fcbResult.orElseThrow();

        return LoanMapper.mapToBranchCodeList(fcbResponse);
    }

    @Override
    public Result<ApplicationNumber> getApplicationNumber(Branch branch, LoanTypeCode loanTypeCode, Party party) {

        log.info(
                "Getting loan file number: branch={}, loanType={}, customer={}",
                branch.code().value(),
                loanTypeCode.value(),
                party.customerNumber());

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(Parameter.builder()
                .key("branchCode")
                .value(branch.code().value())
                .build());

        parameters.add(Parameter.builder()
                .key("loanTypeCode")
                .value(loanTypeCode.value())
                .build());

        parameters.add(Parameter.builder()
                .key("customerNumber")
                .value(party.customerNumber())
                .build());

        Usecases usecases = requestBuilder.buildUseCase("get-loan-file-number", parameters);
        FcbRequest fcbRequest = FcbRequest.builder().usecase(usecases).build();

        log.debug("Executing FCB get-loan-file-number usecase");

        Result<LoanFileNumberResponse> fcbResult =
                fcbService.executeUsecase(fcbRequest, LoanFileNumberResponse.class, FcbContext.empty());

        if (fcbResult.isFailure()) {
            log.error(
                    "FCB get loan file number failed: {}",
                    fcbResult.notification().getErrorMessages());
            return Result.failure(fcbResult.notification());
        }

        LoanFileNumberResponse fcbResponse = fcbResult.orElseThrow();

        Result<ApplicationNumber> domainResult =
                LoanMapper.mapToApplicationNumber(fcbResponse, branch, loanTypeCode, party);

        if (domainResult.isSuccess()) {
            ApplicationNumber applicationNumber = domainResult.orElseThrow();
            log.info(
                    "Loan file number retrieved and mapped to ApplicationNumber: {}",
                    applicationNumber.formattedApplicationNumber());
        }

        return domainResult;
    }
}
