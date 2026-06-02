package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.math.BigDecimal;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.NotificationError;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CoApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.*;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;

@Slf4j
@UtilityClass
public class KafkaValidationMapper {

    // ── LoanServicePort mappings ──

    public Result<EconomicSector> mapToEconomicSector(EconomicSectorKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadEconomicalSectorByCode"));
        }
        return EconomicSector.of(response.getCode());
    }

    public Result<EconomicalSectorResponse> mapToEconomicalSectorResponse(EconomicSectorKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadEconomicalSector"));
        }
        // name/parentCode/hasChild are optional on the wire (a root sector has no parent); the target requires
        // non-null, so coalesce absent values to safe defaults at the anti-corruption boundary.
        Boolean hasChild = response.getHasChild();
        return EconomicalSectorResponse.of(
                response.getCode(),
                response.getName() != null ? response.getName() : "",
                hasChild != null ? hasChild : Boolean.FALSE,
                response.getParentCode() != null ? response.getParentCode() : "");
    }

    public Result<EconomicalSectorValidation> mapToEcoSectorValidation(EcoSectorValidationKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "validateEconomicalSectorForLoanType");
        }
        return Result.success(new EconomicalSectorValidation(response.getValid(), response.getMessage()));
    }

    public Result<ReasonType> mapToReasonType(ReasonTypeKafkaResponse response) {
        if (response.getCode() == null || response.getDescription() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadReasonType");
        }
        return ReasonType.of(
                response.getCode(),
                nz(response.getCentralBankCode()),
                response.getDescription(),
                nz(response.getReasonType()),
                response.isShouldHasSerial(),
                response.isExemptionOfInquiryNumber());
    }

    public Result<SubSource> mapToSubSource(ResourceKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadResourceByCode");
        }
        return SubSource.of(response.getCode());
    }

    public Result<List<TopicInfo>> mapToTopicInfoList(TopicInfoListKafkaResponse response) {
        if (response.getTopics() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadTopicByCode");
        }
        List<TopicInfo> result = new ArrayList<>();
        for (TopicInfoDto dto : response.getTopics()) {
            if (dto.id() == null || dto.title() == null || dto.code() == null) {
                continue; // skip malformed topic entries missing identity fields
            }
            Result<TopicInfo> topicResult = TopicInfo.of(
                    dto.id(),
                    dto.title(),
                    dto.code(),
                    nz(dto.isDebtor()),
                    nz(dto.isUnderLine()),
                    nz(dto.type()),
                    nz(dto.hasOppositeAccount()),
                    nz(dto.numOfOpenableAccounts()),
                    nz(dto.isPermanent()));
            if (topicResult.isSuccess()) {
                result.add(topicResult.unwrap());
            }
        }
        return Result.success(result);
    }

    public Result<List<BranchCode>> mapToBranchCodeList(BranchCodeListKafkaResponse response) {
        if (response.getBranches() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadCoveredBranches"));
        }
        List<BranchCode> result = new ArrayList<>();
        for (BranchCodeListKafkaResponse.BranchCodeDto dto : response.getBranches()) {
            if (dto.code() == null) {
                continue; // skip branch entries missing the code
            }
            result.add(new BranchCode(dto.code()));
        }
        return Result.success(result);
    }

    public Result<ApplicationNumber> mapToApplicationNumber(
            ApplicationNumberKafkaResponse response, Branch branch, LoanTypeCode loanTypeCode, Party party) {
        if (response.getFileNumber() == null || response.getFileNumber().isBlank()) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "getApplicationNumber"));
        }
        String fileNumber = response.getFileNumber();

        String[] parts = fileNumber.split("-");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid fileNumber format: " + fileNumber);
        }

        String serialNumber = parts[parts.length - 1];

        return ApplicationNumber.of(branch, loanTypeCode, party, serialNumber);
    }

    public Result<BranchDetails> mapToBranchDetails(BranchDetailsKafkaResponse response) {
        if (response.getCode() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadBranch");
        }
        return BranchDetails.of(
                response.getCode(),
                nz(response.getName()),
                nz(response.getForeignName()),
                requireNonNull(response.getGlobalCode(), "FCB loadBranch reply missing globalCode"),
                nz(response.getManagerName()),
                nz(response.getSamCode()),
                nz(response.getSwiftCode()),
                nz(response.getClearBranch()),
                nz(response.getCityCode()),
                nz(response.getBankCode()));
    }

    // ── CustomerServicePort mappings ──

    public Result<PartyInfoResponse> mapToPartyInfoResponse(
            CustomerInfoKafkaResponse response, PartyRole role, @Nullable BigDecimal guaranteePercentage) {
        if (response.getCustomerNumber() == null || response.getCustomerNumber().isBlank()) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadCustomerInfo");
        }
        CustomerName customerName =
                new CustomerName(nz(response.getFirstName()), nz(response.getLastName()), nz(response.getTitle()));
        PartyType partyType = Boolean.TRUE.equals(response.getReal()) ? PartyType.REAL : PartyType.LEGAL;

        Party party =
                switch (role) {
                    case PRIMARY_APPLICANT -> new ApplicantParty(response.getCustomerNumber(), partyType, customerName);
                    case CO_APPLICANT -> new CoApplicantParty(response.getCustomerNumber(), partyType, customerName);
                    case GUARANTOR ->
                        GuarantorParty.of(
                                        response.getCustomerNumber(),
                                        partyType,
                                        customerName,
                                        guaranteePercentage != null
                                                ? GuaranteePercentage.of(guaranteePercentage)
                                                : null)
                                .unwrap();
                };

        Result<NationalCode> nationalCodeResult = NationalCode.valueOf(
                requireNonNull(response.getNationalCode(), "FCB loadCustomerInfo reply missing nationalCode"));
        if (nationalCodeResult.isFailure()) {
            return Result.failure(nationalCodeResult.err().orElseThrow());
        }

        return Result.success(new PartyInfoResponse(
                party,
                nationalCodeResult.unwrap(),
                Boolean.TRUE.equals(response.getIsInBlackList()),
                Boolean.TRUE.equals(response.getIsIncapable()),
                Boolean.TRUE.equals(response.getIsInGrayList()),
                // Absent (old FCB during rolling deploy) -> treat as active so screening never false-rejects.
                !Boolean.FALSE.equals(response.getActive())));
    }

    public Result<List<PartyInfoResponse>> mapToPartyInfoResponseList(CustomerListKafkaResponse response) {
        if (response.getCustomers() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "findRelatedCustomers"));
        }
        List<PartyInfoResponse> result = new ArrayList<>();
        for (CustomerListKafkaResponse.CustomerInfoDto dto : response.getCustomers()) {
            if (dto.customerNumber() == null || dto.nationalCode() == null) {
                continue; // skip entries missing identity fields
            }
            CustomerName customerName = new CustomerName(nz(dto.firstName()), nz(dto.lastName()), nz(dto.title()));
            PartyType partyType = Boolean.TRUE.equals(dto.real()) ? PartyType.REAL : PartyType.LEGAL;
            Party party = new ApplicantParty(dto.customerNumber(), partyType, customerName);

            Result<NationalCode> nationalCodeResult = NationalCode.valueOf(dto.nationalCode());
            if (nationalCodeResult.isFailure()) {
                continue;
            }
            result.add(new PartyInfoResponse(
                    party,
                    nationalCodeResult.unwrap(),
                    Boolean.TRUE.equals(dto.isInBlackList()),
                    Boolean.TRUE.equals(dto.isIncapable()),
                    Boolean.TRUE.equals(dto.isInGrayList()),
                    true)); // related-customers lookup does not screen active
        }
        return Result.success(result);
    }

    public Result<PartyBirthInfo> mapToPartyBirthInfo(CustomerBirthInfoKafkaResponse response) {
        return Result.success(new PartyBirthInfo(
                nz(response.getCustomerNumber()),
                requireNonNull(response.getAge(), "FCB loadCustomerBirthInfo reply missing age"),
                response.isGrowthOrder(),
                response.isUnderEighteenYearsOld(),
                nz(response.getBirthDate()),
                response.isCheckGrowthAg()));
    }

    // ── DepositServicePort mappings ──

    public Result<DepositInfo> mapToDepositInfo(DepositInfoKafkaResponse response) {
        if (response.getNumber() == null || response.getNumber().isBlank()) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "getDepositInfo");
        }
        Result<DepositNumber> numberResult = DepositNumber.valueOf(response.getNumber());
        if (numberResult.isFailure()) {
            return Result.failure(numberResult.err().orElseThrow());
        }

        // FCB's legacy core does not always populate a deposit currency (it is left null for
        // plain domestic deposits). Here getDepositInfo is an existence/ownership check only:
        // FacilityValidator.validateDeposit inspects success/failure and never reads
        // currencyType; the request-vs-deposit currency rule is validated separately via the
        // DepositServicePort.hasDepositAllowedCurrencies call (validateDepositCurrency). So a
        // missing currency must not fail the whole facility pre-flight — default it to the
        // domestic IRR. A present-but-malformed value is still surfaced as a real contract error.
        CurrencyType currencyType = CurrencyType.IRR;
        String rawCurrency = response.getCurrency();
        if (rawCurrency != null && !rawCurrency.isBlank()) {
            Result<CurrencyType> currencyResult = CurrencyType.valueOf(rawCurrency);
            if (currencyResult.isFailure()) {
                return Result.failure(currencyResult.err().orElseThrow());
            }
            currencyType = currencyResult.unwrap();
        }

        return DepositInfo.of(
                numberResult.unwrap(),
                nz(response.getTitle()),
                nz(response.getType()),
                currencyType,
                nz(response.getStatus()),
                response.isExternalDeposit(),
                nz(response.getBranchCode()),
                response.getOwnerNationalCodes() != null ? response.getOwnerNationalCodes() : List.of());
    }

    public Result<DepositClosedStatus> mapToDepositClosedStatus(DepositClosedKafkaResponse response) {
        if (response.getClosed() == null || response.getCurrencyTypeCode() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "isDepositClosed");
        }
        return Result.success(new DepositClosedStatus(response.getClosed(), response.getCurrencyTypeCode()));
    }

    public Result<DebtorDepositValidation> mapToDebtorDepositValidation(ValidationResultKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "validateDebtorDeposit"));
        }
        return Result.success(new DebtorDepositValidation(response.getValid()));
    }

    public Result<CreditorDepositValidation> mapToCreditorDepositValidation(ValidationResultKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "validateCreditorDeposit"));
        }
        return Result.success(new CreditorDepositValidation(response.getValid()));
    }

    public Result<CurrencyValidation> mapToCurrencyValidation(CurrencyValidationKafkaResponse response) {
        if (response.getAllowed() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "hasDepositAllowedCurrencies"));
        }
        return Result.success(new CurrencyValidation(response.getAllowed(), response.getMessage()));
    }

    // ── CollateralServicePort mappings ──

    public Result<CollateralValidation> mapToCollateralValidation(CollateralValidationKafkaResponse response) {
        if (response.getValid() == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "validateAddAssuranceToFile"));
        }
        return Result.success(new CollateralValidation(response.getValid(), response.getMessage()));
    }

    public Result<List<CollateralSerial>> mapToCollateralSerials(CollateralSerialsKafkaResponse response) {
        if (response.getSerials() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "reserveCollateral");
        }
        List<CollateralSerial> result = new ArrayList<>();
        for (String serial : response.getSerials()) {
            result.add(new CollateralSerial(serial));
        }
        return Result.success(result);
    }

    public Result<CollateralDetails> mapToCollateralDetails(CollateralDetailsKafkaResponse response) {
        return Result.success(new CollateralDetails(
                nz(response.getSerial()),
                nz(response.getCustomerNo()),
                nz(response.getAssuranceTypeCode()),
                nz(response.getAssuranceTypeName()),
                nz(response.getGuaranteeAmount()),
                nz(response.getPrice()),
                nz(response.getUsedMortgagePrice()),
                nz(response.getGuaranteeDuration()),
                nz(response.getGuaranteeNumber()),
                nz(response.getGuaranteeIssuer()),
                nz(response.getGuaranteeBranchCode()),
                nz(response.getLoanFileNumber()),
                nz(response.getBranchCode()),
                nz(response.getCurrency()),
                response.isActive(),
                response.isEscrowed(),
                response.isReleaseAllowed(),
                response.isSpecial(),
                nz(response.getAddress())));
    }

    // ── FetchSanctionDetailsPort mappings ──

    public Result<SanctionDetails> mapToSanctionDetails(SanctionDetailsKafkaResponse response) {
        if (response.getSanctionSerial() == null || response.getSanctionSerial().isBlank()) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "fetchSanctionDetails"));
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
            if (currencyResult.isSuccess()) {
                currency = currencyResult.unwrap();
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
            if (ctResult.isSuccess()) {
                confirmType = ctResult.unwrap();
            }
        }

        // SanctionDetails requires these core sanction terms non-null; a reply that omits/mis-encodes any of them is
        // not a usable sanction, so fail at the anti-corruption boundary rather than fabricate defaults.
        if (sanctionType == null
                || response.getApprovedAmount() == null
                || currency == null
                || gracePeriod == null
                || response.getInstallmentCount() == null
                || loanDuration == null
                || disbursementMethod == null
                || confirmType == null) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "fetchSanctionDetails"));
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

    /**
     * Anti-corruption coalescing for wire fields that are optional on the FCB reply but required (non-null) by the
     * target response DTO. FCB may legitimately omit descriptive/secondary fields; treating an absent value as empty
     * (rather than throwing) is the boundary policy for these. Identity/key fields are guarded separately and surface a
     * {@code KAFKA_INVALID_RESPONSE} failure instead.
     */
    private static String nz(@Nullable String value) {
        return value != null ? value : "";
    }

    private static boolean nz(@Nullable Boolean value) {
        return value != null ? value : false;
    }

    private static BigDecimal nz(@Nullable BigDecimal value) {
        return value != null ? value : ZERO;
    }

    private static int nz(@Nullable Integer value) {
        return value != null ? value : 0;
    }

    private @Nullable Period parsePeriod(@Nullable String value) {
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

    public static Result<Unit> mapSamatViolationToNotification(ValidateSamatKafkaResponse validateSamatKafkaResponse) {
        if (validateSamatKafkaResponse == null || validateSamatKafkaResponse.getViolations() == null) {
            return Result.failure(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "validateSamat");
        }

        List<SamatViolationDto> violations = validateSamatKafkaResponse.getViolations();

        if (violations.isEmpty()) {
            return Result.success();
        }

        Notification notification = violations.stream()
                .map(violation -> NotificationError.of(resolveViolationError(violation), buildViolationArgs(violation)))
                .collect(Notification::create, Notification::addError, Notification::merge);

        return Result.failure(notification);
    }

    private static CoreBankingErrors resolveViolationError(SamatViolationDto violation) {
        return switch (nz(violation.getViolationCode())) {
            case "INVALID_USE_TYPE" -> CoreBankingErrors.SAMAT_INVALID_USE_TYPE;
            case "INVALID_ISIC_ECONOMIC_SECTOR" -> CoreBankingErrors.SAMAT_INVALID_ISIC_ECONOMIC_SECTOR;
            case "INVALID_ISIC_SUB_COMBINATION" -> CoreBankingErrors.SAMAT_INVALID_ISIC_SUB_COMBINATION;
            case "INVALID_EXCEPTION_CODE" -> CoreBankingErrors.SAMAT_INVALID_EXCEPTION_CODE;
            case "INVALID_CONSUMPTION_PLACE_CODE" -> CoreBankingErrors.SAMAT_INVALID_CONSUMPTION_PLACE_CODE;
            default -> CoreBankingErrors.SAMAT_UNKNOWN_VIOLATION;
        };
    }

    private static Object[] buildViolationArgs(SamatViolationDto violation) {
        return switch (nz(violation.getViolationCode())) {
            case "INVALID_ISIC_SUB_COMBINATION" -> new Object[] {violation.getProvidedValue(), violation.getField()};
            case "SAMAT_UNKNOWN_VIOLATION" -> new Object[] {violation.getViolationCode(), violation.getMessage()};
            default -> new Object[] {violation.getProvidedValue()};
        };
    }
}
