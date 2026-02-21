package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.math.BigDecimal;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CoApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbKafkaLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class KafkaValidationMapper {

    // ── LoanServicePort mappings ──

    public Result<EconomicSector> mapToEconomicSector(EconomicSectorKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadEconomicalSectorByCode"));
        }
        return EconomicSector.of(response.getCode());
    }

    public Result<EconomicalSectorResponse> mapToEconomicalSectorResponse(EconomicSectorKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadEconomicalSector"));
        }
        return EconomicalSectorResponse.of(
                response.getCode(), response.getName(), response.getHasChild(), response.getParentCode());
    }

    public Result<EconomicalSectorValidation> mapToEcoSectorValidation(EcoSectorValidationKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "validateEconomicalSectorForLoanType"));
        }
        return Result.success(new EconomicalSectorValidation(response.getValid(), response.getMessage()));
    }

    public Result<ReasonType> mapToReasonType(ReasonTypeKafkaResponse response) {
        if (response.getCode() == null || response.getDescription() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadReasonType"));
        }
        return ReasonType.of(
                response.getCode(),
                response.getCentralBankCode(),
                response.getDescription(),
                response.getReasonType(),
                response.isShouldHasSerial(),
                response.isExemptionOfInquiryNumber());
    }

    public Result<SubSource> mapToSubSource(ResourceKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadResourceByCode"));
        }
        return SubSource.of(response.getCode());
    }

    public Result<List<TopicInfo>> mapToTopicInfoList(TopicInfoListKafkaResponse response) {
        if (response.getTopics() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadTopicByCode"));
        }
        List<TopicInfo> result = new ArrayList<>();
        for (TopicInfoDto dto : response.getTopics()) {
            Result<TopicInfo> topicResult = TopicInfo.of(
                    dto.id(),
                    dto.title(),
                    dto.code(),
                    dto.isDebtor(),
                    dto.isUnderLine(),
                    dto.type(),
                    dto.hasOppositeAccount(),
                    dto.numOfOpenableAccounts(),
                    dto.isPermanent());
            if (topicResult.isSuccessWithValue()) {
                result.add(topicResult.orElseThrow());
            }
        }
        return Result.success(result);
    }

    public Result<List<BranchCode>> mapToBranchCodeList(BranchCodeListKafkaResponse response) {
        if (response.getBranches() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadCoveredBranches"));
        }
        List<BranchCode> result = new ArrayList<>();
        for (BranchCodeListKafkaResponse.BranchCodeDto dto : response.getBranches()) {
            result.add(new BranchCode(dto.code()));
        }
        return Result.success(result);
    }

    public Result<ApplicationNumber> mapToApplicationNumber(
            ApplicationNumberKafkaResponse response, Branch branch, LoanTypeCode loanTypeCode, Party party) {
        if (response.getFileNumber() == null || response.getFileNumber().isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "getApplicationNumber"));
        }
        return ApplicationNumber.of(branch, loanTypeCode, party, response.getFileNumber());
    }

    public Result<BranchDetails> mapToBranchDetails(BranchDetailsKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadBranch"));
        }
        return BranchDetails.of(
                response.getCode(),
                response.getName(),
                response.getForeignName(),
                response.getGlobalCode(),
                response.getManagerName(),
                response.getSamCode(),
                response.getSwiftCode(),
                response.getClearBranch(),
                response.getCityCode(),
                response.getBankCode());
    }

    // ── CustomerServicePort mappings ──

    public Result<PartyInfoResponse> mapToPartyInfoResponse(
            CustomerInfoKafkaResponse response, PartyRole role, @Nullable BigDecimal guaranteePercentage) {
        if (response.getCustomerNumber() == null || response.getCustomerNumber().isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadCustomerInfo"));
        }
        CustomerName customerName =
                new CustomerName(response.getFirstName(), response.getLastName(), response.getTitle());
        PartyType partyType =
                Boolean.TRUE.equals(response.getReal()) ? PartyType.REAL : PartyType.LEGAL;

        Party party =
                switch (role) {
                    case PRIMARY_APPLICANT ->
                        new ApplicantParty(response.getCustomerNumber(), partyType, customerName);
                    case CO_APPLICANT ->
                        new CoApplicantParty(response.getCustomerNumber(), partyType, customerName);
                    case GUARANTOR ->
                        GuarantorParty.of(
                                        response.getCustomerNumber(),
                                        partyType,
                                        customerName,
                                        guaranteePercentage != null
                                                ? GuaranteePercentage.of(guaranteePercentage)
                                                : null)
                                .orElseThrow();
                };

        Result<NationalCode> nationalCodeResult = NationalCode.valueOf(response.getNationalCode());
        if (nationalCodeResult.isFailure()) {
            return Result.failure(nationalCodeResult.notification());
        }

        return Result.success(new PartyInfoResponse(
                party,
                nationalCodeResult.orElseThrow(),
                Boolean.TRUE.equals(response.getIsInBlackList()),
                Boolean.TRUE.equals(response.getIsIncapable()),
                Boolean.TRUE.equals(response.getIsInGrayList())));
    }

    public Result<List<PartyInfoResponse>> mapToPartyInfoResponseList(CustomerListKafkaResponse response) {
        if (response.getCustomers() == null) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "findRelatedCustomers"));
        }
        List<PartyInfoResponse> result = new ArrayList<>();
        for (CustomerListKafkaResponse.CustomerInfoDto dto : response.getCustomers()) {
            CustomerName customerName = new CustomerName(dto.firstName(), dto.lastName(), dto.title());
            PartyType partyType = Boolean.TRUE.equals(dto.real()) ? PartyType.REAL : PartyType.LEGAL;
            Party party = new ApplicantParty(dto.customerNumber(), partyType, customerName);

            Result<NationalCode> nationalCodeResult = NationalCode.valueOf(dto.nationalCode());
            if (nationalCodeResult.isFailure()) {
                continue;
            }
            result.add(new PartyInfoResponse(
                    party,
                    nationalCodeResult.orElseThrow(),
                    Boolean.TRUE.equals(dto.isInBlackList()),
                    Boolean.TRUE.equals(dto.isIncapable()),
                    Boolean.TRUE.equals(dto.isInGrayList())));
        }
        return Result.success(result);
    }

    public Result<PartyBirthInfo> mapToPartyBirthInfo(CustomerBirthInfoKafkaResponse response) {
        return Result.success(new PartyBirthInfo(
                response.getCustomerNumber(),
                response.getAge(),
                response.isGrowthOrder(),
                response.isUnderEighteenYearsOld(),
                response.getBirthDate(),
                response.isCheckGrowthAg()));
    }

    // ── DepositServicePort mappings ──

    public Result<DepositInfo> mapToDepositInfo(DepositInfoKafkaResponse response) {
        if (response.getNumber() == null || response.getNumber().isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "getDepositInfo"));
        }
        Result<DepositNumber> numberResult = DepositNumber.valueOf(response.getNumber());
        if (numberResult.isFailure()) {
            return Result.failure(numberResult.notification());
        }

        CurrencyType currencyType = null;
        if (response.getCurrency() != null) {
            Result<CurrencyType> currencyResult = CurrencyType.valueOf(response.getCurrency());
            if (currencyResult.isSuccessWithValue()) {
                currencyType = currencyResult.orElseThrow();
            }
        }

        return DepositInfo.of(
                numberResult.orElseThrow(),
                response.getTitle(),
                response.getType(),
                currencyType,
                response.getStatus(),
                response.isExternalDeposit(),
                response.getBranchCode(),
                response.getOwnerNationalCodes() != null ? response.getOwnerNationalCodes() : List.of());
    }

    public Result<DepositClosedStatus> mapToDepositClosedStatus(DepositClosedKafkaResponse response) {
        if (response.getClosed() == null || response.getCurrencyTypeCode() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "isDepositClosed"));
        }
        return Result.success(new DepositClosedStatus(response.getClosed(), response.getCurrencyTypeCode()));
    }

    public Result<DebtorDepositValidation> mapToDebtorDepositValidation(ValidationResultKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "validateDebtorDeposit"));
        }
        return Result.success(new DebtorDepositValidation(response.getValid()));
    }

    public Result<CreditorDepositValidation> mapToCreditorDepositValidation(ValidationResultKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "validateCreditorDeposit"));
        }
        return Result.success(new CreditorDepositValidation(response.getValid()));
    }

    public Result<CurrencyValidation> mapToCurrencyValidation(CurrencyValidationKafkaResponse response) {
        if (response.getAllowed() == null) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "hasDepositAllowedCurrencies"));
        }
        return Result.success(new CurrencyValidation(response.getAllowed(), response.getMessage()));
    }

    // ── CollateralServicePort mappings ──

    public Result<CollateralValidation> mapToCollateralValidation(CollateralValidationKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "validateAddAssuranceToFile"));
        }
        return Result.success(new CollateralValidation(response.getValid(), response.getMessage()));
    }

    public Result<List<CollateralSerial>> mapToCollateralSerials(CollateralSerialsKafkaResponse response) {
        if (response.getSerials() == null) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "reserveCollateral"));
        }
        List<CollateralSerial> result = new ArrayList<>();
        for (String serial : response.getSerials()) {
            result.add(new CollateralSerial(serial));
        }
        return Result.success(result);
    }

    public Result<CollateralDetails> mapToCollateralDetails(CollateralDetailsKafkaResponse response) {
        return Result.success(new CollateralDetails(
                response.getSerial(),
                response.getCustomerNo(),
                response.getAssuranceTypeCode(),
                response.getAssuranceTypeName(),
                response.getGuaranteeAmount(),
                response.getPrice(),
                response.getUsedMortgagePrice(),
                response.getGuaranteeDuration(),
                response.getGuaranteeNumber(),
                response.getGuaranteeIssuer(),
                response.getGuaranteeBranchCode(),
                response.getLoanFileNumber(),
                response.getBranchCode(),
                response.getCurrency(),
                response.isActive(),
                response.isEscrowed(),
                response.isReleaseAllowed(),
                response.isSpecial(),
                response.getAddress()));
    }

    // ── FetchSanctionDetailsPort mappings ──

    public Result<SanctionDetails> mapToSanctionDetails(SanctionDetailsKafkaResponse response) {
        if (response.getSanctionSerial() == null || response.getSanctionSerial().isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "fetchSanctionDetails"));
        }

        SanctionType sanctionType = null;
        if (response.getSanctionType() != null) {
            try {
                sanctionType = SanctionType.valueOf(response.getSanctionType());
            } catch (IllegalArgumentException ignored) {
            }
        }

        CurrencyType currency = null;
        if (response.getCurrency() != null) {
            Result<CurrencyType> currencyResult = CurrencyType.valueOf(response.getCurrency());
            if (currencyResult.isSuccessWithValue()) {
                currency = currencyResult.orElseThrow();
            }
        }

        Period gracePeriod = parsePeriod(response.getGracePeriod());
        Period loanDuration = parsePeriod(response.getLoanDuration());

        DisbursementMethod disbursementMethod = null;
        if (response.getDisbursementMethod() != null) {
            try {
                disbursementMethod = DisbursementMethod.valueOf(response.getDisbursementMethod());
            } catch (IllegalArgumentException ignored) {
            }
        }

        ConfirmType confirmType = null;
        if (response.getConfirmType() != null) {
            Result<ConfirmType> ctResult = ConfirmType.of(response.getConfirmType());
            if (ctResult.isSuccessWithValue()) {
                confirmType = ctResult.orElseThrow();
            }
        }

        return Result.success(new SanctionDetails(
                response.getSanctionSerial(),
                sanctionType,
                response.getApprovedAmount(),
                currency,
                gracePeriod,
                response.getInstallmentCount(),
                loanDuration,
                disbursementMethod,
                response.getLifeInsuranceId(),
                response.getCollateralSerial(),
                response.getRevocationReason(),
                confirmType));
    }

    // ── Helpers ──

    private Period parsePeriod(@Nullable String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Period.parse(value);
        } catch (Exception ignored) {
        }
        try {
            int months = Integer.parseInt(value);
            return Period.ofMonths(months);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
