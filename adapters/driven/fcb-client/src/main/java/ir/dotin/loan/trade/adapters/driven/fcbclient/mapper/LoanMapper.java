package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.LoanOperationType;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.*;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.BranchDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.TopicInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

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

            Result<EconomicSector> economicSector = EconomicSector.of(fcbResponse.getCode());

            if (economicSector.isFailure()) {
                log.error(
                        "Failed to create EconomicalSection: {}",
                        economicSector.notification().getErrorMessages());
            }

            return economicSector;

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain Economic Sector", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse economical section: " + e.getMessage()));
        }
    }

    public static Result<EconomicalSectorResponse> mapToDomainEconomicalSectionResponse(
            EconomicalSectionResponse fcbResponse) {

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

            Result<EconomicalSectorResponse> economicSector = EconomicalSectorResponse.of(
                    fcbResponse.getCode(),
                    fcbResponse.getName(),
                    fcbResponse.getHasChild(),
                    fcbResponse.getParentCode());

            if (economicSector.isFailure()) {
                log.error(
                        "Failed to create EconomicalSection: {}",
                        economicSector.notification().getErrorMessages());
            }

            return economicSector;

        } catch (Exception e) {
            log.error("Failed to map FCB response to domain Economic Sector", e);
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Failed to parse economical section: " + e.getMessage()));
        }
    }

    public Result<EconomicalSectorValidation> mapToDomainValidation(FcbValidationResponse fcbResponse) {

        EconomicalSectorValidation economicalSectorValidation =
                new EconomicalSectorValidation(fcbResponse.isValid(), fcbResponse.getSuccessMessage());

        return Result.success(economicalSectorValidation);
    }

    public static Result<ReasonType> mapToDomainReasonType(ReasonTypeResponse response) {
        log.debug("Mapping ReasonTypeResponse to domain ReasonType - code: {}", response.getCode());

        Notification notification = Notification.create();

        if (response.getCode() == null || response.getCode().isBlank()) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Reason type code is missing in response");
            return Result.failure(notification);
        }

        return ReasonType.of(
                response.getCode(),
                response.getCentralBankCode(),
                response.getDescription() != null ? response.getDescription() : "",
                response.getReasonType(),
                response.getShouldHasSerial() != null && response.getShouldHasSerial(),
                response.getExemptionOfInquiryNumber() != null && response.getExemptionOfInquiryNumber());
    }

    public static final Map<FacilityStatus, LoanOperationType> FACILITY_STATUS_TO_OPERATION_MAPPING = Map.of(
            FacilityStatus.APPLICATION_SUBMITTED, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.APPROVAL_SUBMITTED, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.APPROVED, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.REJECTED, LoanOperationType.REVOKE_CONTRACT,
            FacilityStatus.CANCELLED, LoanOperationType.REVOKE_CONTRACT,
            FacilityStatus.ISSUE_CONTRACT, LoanOperationType.ISSUE_SANCTION,
            FacilityStatus.FULLY_DISBURSED, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.CLOSED_PAID_OFF, LoanOperationType.REVOKE_CONTRACT,
            FacilityStatus.CLOSED_DEFAULTED, LoanOperationType.REVOKE_CONTRACT);

    public static Result<TopicInfo> mapToLoanTopic(TopicResponse response) {

        return TopicInfo.of(
                response.getId(),
                response.getTitle(),
                response.getCode(),
                response.getIsDebtor(),
                response.getIsUnderLine(),
                response.getType(),
                response.getHasOppositeAccount(),
                response.getNumOfOpenableAccounts(),
                response.getIsPermanent());
    }

    public static Result<SubSource> mapToResource(ResourceResponse fcbResponse) {

        Notification notification = Notification.create();

        if (fcbResponse.getCode() == null || fcbResponse.getCode().isBlank()) {
            log.error("FCB response missing resource code");
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Resource code is missing in response");
        }

        if (fcbResponse.getName() == null || fcbResponse.getName().isBlank()) {
            log.error("FCB response missing resource name");
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Resource name is missing in response");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        SubSource subSource = new SubSource(fcbResponse.getCode());

        return Result.success(subSource);
    }

    public static Result<BranchCode> mapToBranchCode(BranchResponse fcbResponse) {

        Notification notification = Notification.create();

        if (fcbResponse.getCode() == null || fcbResponse.getCode().isBlank()) {
            log.error("FCB response missing branch code");
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Branch code is missing in response");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        BranchCode branchCode = new BranchCode(fcbResponse.getCode());

        return Result.success(branchCode);
    }

    public static Result<List<BranchCode>> mapToBranchCodeList(List<BranchResponse> branchResponseList) {

        Notification notification = Notification.create();

        if (branchResponseList == null || branchResponseList.isEmpty()) {
            log.warn("FCB response has no branches in the list");
            return Result.success(List.of());
        }

        List<BranchCode> branchList = new ArrayList<>();

        for (int i = 0; i < branchResponseList.size(); i++) {
            BranchResponse branchResponse = branchResponseList.get(i);

            Result<BranchCode> branchResult = mapToBranchCode(branchResponse);

            if (branchResult.isFailure()) {
                log.error(
                        "Failed to map branch at index {}: {}",
                        i,
                        branchResult.notification().getErrorMessages());
                notification.merge(branchResult.notification());
            } else {
                branchList.add(branchResult.orElseThrow());
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        log.info("Successfully mapped {} branches from FCB response", branchList.size());
        return Result.success(branchList);
    }

    public static Result<List<TopicInfo>> mapToTopicInfoList(List<TopicResponse> responses) {
        Notification notification = Notification.create();

        if (isEmpty(responses)) {
            log.warn("FCB response has no topic in the list");
            return Result.success(List.of());
        }

        List<TopicInfo> topicInfoList = new ArrayList<>();

        for (TopicResponse response : responses) {

            Result<TopicInfo> topicInfoResult = mapToLoanTopic(response);

            if (topicInfoResult.isFailure()) {
                notification.merge(topicInfoResult.notification());
            } else {
                topicInfoList.add(topicInfoResult.orElseThrow());
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        log.info("Successfully mapped {} topicInfoList from FCB response", topicInfoList.size());
        return Result.success(topicInfoList);
    }

    public static Result<AccountId> mapToCreateAccountResult(Result<AccountInfoResponse> responseResult) {

        if (isNull(responseResult) || isNull(responseResult.value())) {
            return Result.failure(Notification.create());
        }

        return Result.success(new AccountId(responseResult.value().getAccountNumber()));
    }

    public static Result<ApplicationNumber> mapToApplicationNumber(
            LoanFileNumberResponse fcbResponse, Branch branch, LoanTypeCode loanTypeCode, Party party) {

        Notification notification = Notification.create();

        if (fcbResponse.getLoanFileNumber() == null
                || fcbResponse.getLoanFileNumber().isBlank()) {
            notification.addError(FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE);
            return Result.failure(notification);
        }

        String loanFileNumber = fcbResponse.getLoanFileNumber();

        Result<ApplicationNumberComponents> componentsResult = parseApplicationNumber(loanFileNumber);
        if (componentsResult.isFailure()) {
            return Result.failure(componentsResult.notification());
        }

        ApplicationNumberComponents components = componentsResult.getValue();

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return ApplicationNumber.of(branch, loanTypeCode, party, components.sequenceCode);
    }

    private record ApplicationNumberComponents(
            String branchCode, String loanTypeCode, String customerNumber, String sequenceCode) {}

    private static Result<ApplicationNumberComponents> parseApplicationNumber(String formattedNumber) {
        Notification notification = Notification.create();

        String[] parts = formattedNumber.split("-", 4);

        if (parts.length < 3) {
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Invalid application number format: " + formattedNumber);
            return Result.failure(notification);
        }

        String branchCode = parts[0];
        String loanTypeCode = parts[1];
        String customerNumber = parts[2];
        String sequenceCode = parts.length > 3 ? parts[3] : null;

        return Result.success(new ApplicationNumberComponents(branchCode, loanTypeCode, customerNumber, sequenceCode));
    }

    public static Result<BranchDetails> mapToBranchDetails(BranchResponse fcbResponse) {
        Notification notification = Notification.create();

        if (fcbResponse.getCode() == null || fcbResponse.getCode().isBlank()) {
            log.error("FCB response missing branch code");
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Branch code is missing in response");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }
        BranchDetails branchDetails = new BranchDetails(
                fcbResponse.getCode(),
                fcbResponse.getName(),
                fcbResponse.getForeignName(),
                fcbResponse.getGlobalCode(),
                fcbResponse.getManagerName(),
                fcbResponse.getSamCode(),
                fcbResponse.getSwiftCode(),
                fcbResponse.getClearBranch(),
                fcbResponse.getCity(),
                fcbResponse.getBankCode());

        return Result.success(branchDetails);
    }
}
