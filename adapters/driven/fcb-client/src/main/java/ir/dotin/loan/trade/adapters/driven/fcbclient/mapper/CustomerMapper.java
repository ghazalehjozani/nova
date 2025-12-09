package ir.dotin.loan.trade.adapters.driven.fcbclient.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.Direction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTransaction;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CoApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.PersonName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.Article;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.BoxTarget;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositTarget;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerBirthInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbclient.i18n.FcbBusinessLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyBirthInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CustomerMapper {

    private static Result<String> mapArticleToFcbItem(Article article, int index) {
        String targetType;
        String targetIdentifier;

        switch (article.target()) {
            case AccountTarget(AccountId accountId, RelationType<?> relationType) -> {
                targetType = "ACCOUNT";
                targetIdentifier = accountId.value();
            }
            case DepositTarget(DepositNumber depositNumber) -> {
                targetType = "DEPOSIT";
                targetIdentifier = depositNumber.value();
            }
            case BoxTarget boxTarget -> {
                targetType = "BOX";
                targetIdentifier = "";
            }
            default -> {
                log.error(
                        "Unknown article target type at index {}: {}",
                        index,
                        article.target().getClass());
                return Result.failure(Notification.ofError(
                        FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                        "Unknown article target type: "
                                + article.target().getClass().getSimpleName()));
            }
        }

        boolean isDebtor = article.direction() == Direction.DEBIT;

        BigDecimal amountValue = article.amount().value();

        String fcbItem;
        if (targetType.equals("BOX")) {
            fcbItem = String.format("%s,%s,%s", targetType, isDebtor, amountValue);
        } else {
            fcbItem = String.format("%s,%s,%s,%s", targetType, targetIdentifier, isDebtor, amountValue);
        }

        log.debug("Mapped article {} to FCB item: {}", index, fcbItem);
        return Result.success(fcbItem);
    }

    private static String getTargetTypeInPersian(Article article) {
        return switch (article.target()) {
            case AccountTarget accountTarget -> "حساب";
            case DepositTarget depositTarget -> "سپرده";
            case BoxTarget boxTarget -> "صندوق";
            default -> "نامشخص";
        };
    }

    public static String extractBranchCode(LoanTransaction loanTransaction) {
        return loanTransaction.document().branchCode().value();
    }

    public static String extractDocumentComment(LoanTransaction loanTransaction) {
        return loanTransaction.document().description();
    }

    public Result<PartyInfo> mapToDomainCustomerInfo(
            CustomerInfoResponse fcbResponse, @NotNull PartyRole role, @Nullable BigDecimal guaranteePercentage) {
        String firstName = fcbResponse.getFirstName();
        String lastName = fcbResponse.getLastName();
        PersonName personName = new PersonName(firstName, lastName);
        PartyType partyType = fcbResponse.getReal() ? PartyType.REAL : PartyType.LEGAL;
        String customerNumber = String.valueOf(fcbResponse.getCustomerNumber());

        Party party =
                switch (role) {
                    case PRIMARY_APPLICANT -> new ApplicantParty(customerNumber, partyType, personName);
                    case CO_APPLICANT -> new CoApplicantParty(customerNumber, partyType, personName);
                    case GUARANTOR ->
                        GuarantorParty.of(
                                        customerNumber,
                                        partyType,
                                        personName,
                                        guaranteePercentage != null
                                                ? GuaranteePercentage.of(guaranteePercentage)
                                                : null)
                                .orElseThrow();
                };
        Result<NationalCode> nationalCode = NationalCode.valueOf(fcbResponse.getNationalCode());

        if (fcbResponse.getCustomerNumber() == null) {
            log.error("FCB response missing customer number");
            return Result.failure(Notification.ofError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE,
                    "Customer number is missing in FCB response"));
        }

        Boolean isInBlackListValue = fcbResponse.getIsInBlackList();
        Boolean isIncapableValue = fcbResponse.getIsIncapable();
        Boolean isInGrayListValue = fcbResponse.getIsInGrayList();

        PartyInfo partyInfo = new PartyInfo(
                party,
                nationalCode.orElseThrow(),
                isInBlackListValue != null && isInBlackListValue,
                isIncapableValue != null && isIncapableValue,
                isInGrayListValue != null && isInGrayListValue);

        return Result.success(partyInfo);
    }

    public static Result<List<PartyInfo>> mapToCustomerInfoList(List<CustomerInfoResponse> customers) {

        Notification notification = Notification.create();

        if (customers == null || customers.isEmpty()) {
            log.warn("FCB response has no customers in the list");
            return Result.success(List.of());
        }

        List<PartyInfo> customerInfoList = new ArrayList<>();

        for (int i = 0; i < customers.size(); i++) {
            CustomerInfoResponse customerResponse = customers.get(i);

            if (true) {
                throw new UnsupportedOperationException("Not supported yet, Handle PartyRole");
            }
            Result<PartyInfo> customerResult =
                    mapToDomainCustomerInfo(customerResponse, PartyRole.PRIMARY_APPLICANT, null);

            if (customerResult.isFailure()) {
                log.error(
                        "Failed to map customer at index {}: {}",
                        i,
                        customerResult.notification().getErrorMessages());
                notification.merge(customerResult.notification());
            } else {
                customerInfoList.add(customerResult.orElseThrow());
            }
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        log.info("Successfully mapped {} customers from FCB response", customerInfoList.size());
        return Result.success(customerInfoList);
    }

    public static Result<PartyBirthInfo> mapToCustomerBirthInfo(CustomerBirthInfoResponse fcbResponse) {

        Notification notification = Notification.create();

        if (fcbResponse.getCustomerNumber() == null) {
            log.error("FCB response missing customer number");
            notification.addError(
                    FcbBusinessLocalizedMessageCodes.FCB_INVALID_RESPONSE, "Customer number is missing in response");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        PartyBirthInfo birthInfo = getPartyBirthInfo(fcbResponse);

        return Result.success(birthInfo);
    }

    private static PartyBirthInfo getPartyBirthInfo(CustomerBirthInfoResponse fcbResponse) {
        String customerNumberStr = String.valueOf(fcbResponse.getCustomerNumber());

        return new PartyBirthInfo(
                customerNumberStr,
                fcbResponse.getAge(),
                fcbResponse.getGrowthOrder() != null && fcbResponse.getGrowthOrder(),
                fcbResponse.getIsUnderEighteenYearsOld() != null && fcbResponse.getIsUnderEighteenYearsOld(),
                fcbResponse.getBirthDate(),
                fcbResponse.getCheckGrowthAge() != null && fcbResponse.getCheckGrowthAge());
    }
}
