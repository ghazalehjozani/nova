package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.util.Map;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.request.LoanOperationType;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.EconomicalSectionResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.FcbValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.LoanTopicResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ReasonTypeResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.ResourceResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.EconomicalSectorValidation;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.LoanTopicInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.ReasonType;

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

        ReasonType reasonType = new ReasonType(
                response.getCode(),
                response.getCentralBankCode(),
                response.getDescription() != null ? response.getDescription() : "",
                response.getReasonType(),
                response.getShouldHasSerial() != null && response.getShouldHasSerial(),
                response.getExemptionOfInquiryNumber() != null && response.getExemptionOfInquiryNumber());

        return Result.success(reasonType);
    }

    public static final Map<FacilityStatus, LoanOperationType> FACILITY_STATUS_TO_OPERATION_MAPPING = Map.of(
            FacilityStatus.APPLICATION_SUBMITTED, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.APPROVAL_SUBMITTED, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.APPROVED, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.REJECTED, LoanOperationType.REVOKE_CONTRACT,
            FacilityStatus.CANCELLED, LoanOperationType.REVOKE_CONTRACT,
            FacilityStatus.ISSUE_CONTRACT, LoanOperationType.ISSUE_SANCTION,
            FacilityStatus.ACTIVE, LoanOperationType.RECEIVE_LOAN,
            FacilityStatus.CLOSED_PAID_OFF, LoanOperationType.REVOKE_CONTRACT,
            FacilityStatus.CLOSED_DEFAULTED, LoanOperationType.REVOKE_CONTRACT);

    public static Result<LoanTopicInfo> mapToLoanTopic(LoanTopicResponse fcbResponse) {

        Boolean mainTopicIsDebtor = fcbResponse.getMainTopicIsDebtor();
        Boolean bankCommitmentsTopicIsDebtor = fcbResponse.getBankCommitmentsTopicIsDebtor();
        Boolean customerCommitmentsTopicIsDebtor = fcbResponse.getCustomerCommitmentsTopicIsDebtor();
        Boolean temporaryDebtorsTopicIsDebtor = fcbResponse.getTemporaryDebtorsTopicIsDebtor();

        LoanTopicInfo topic = new LoanTopicInfo(
                mainTopicIsDebtor != null && mainTopicIsDebtor,
                bankCommitmentsTopicIsDebtor != null && bankCommitmentsTopicIsDebtor,
                customerCommitmentsTopicIsDebtor != null && customerCommitmentsTopicIsDebtor,
                temporaryDebtorsTopicIsDebtor != null && temporaryDebtorsTopicIsDebtor);

        return Result.success(topic);
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
}
