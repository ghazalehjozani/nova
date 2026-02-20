package ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper;

import java.math.BigDecimal;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
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
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsParameter;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps domain objects to/from JMS parameters for the validation queue. Covers: LoanServicePort, CustomerServicePort,
 * DepositServicePort, CollateralServicePort, FetchSanctionDetailsPort.
 */
@Slf4j
@UtilityClass
public class JmsValidationMapper {

    private static final String SEPARATOR = "#";

    // ── LoanServicePort mappings ──

    public List<FcbJmsParameter> mapEconomicalSectorByCodeParams(EconomicSector economicSector) {
        return List.of(new FcbJmsParameter("economicalSectionCode", economicSector.code()));
    }

    public Result<EconomicSector> mapToEconomicSector(Map<String, Object> payload) {
        String code = asString(payload.get("code"));
        if (code == null) {
            return Result.failure(Notification.ofError(
                    FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadEconomicalSectorByCode"));
        }
        return EconomicSector.of(code);
    }

    public Result<EconomicalSectorResponse> mapToEconomicalSectorResponse(Map<String, Object> payload) {
        String code = asString(payload.get("code"));
        String name = asString(payload.get("name"));
        Boolean hasChild = asBoolean(payload.get("hasChild"));
        String parentCode = asString(payload.get("parentCode"));
        if (code == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadEconomicalSector"));
        }
        return EconomicalSectorResponse.of(code, name, hasChild, parentCode);
    }

    public List<FcbJmsParameter> mapValidateEcoSectorParams(EconomicSector economicSector, LoanTypeCode loanTypeCode) {
        return List.of(
                new FcbJmsParameter("economicalSectionCode", economicSector.code()),
                new FcbJmsParameter("loanTypeCode", loanTypeCode.value()));
    }

    public Result<EconomicalSectorValidation> mapToEcoSectorValidation(Map<String, Object> payload) {
        Boolean valid = asBoolean(payload.get("valid"));
        String message = asString(payload.get("message"));
        if (valid == null) {
            return Result.failure(Notification.ofError(
                    FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "validateEconomicalSectorForLoanType"));
        }
        return Result.success(new EconomicalSectorValidation(valid, message));
    }

    public List<FcbJmsParameter> mapReasonTypeParams(String reasonTypeCode) {
        return List.of(new FcbJmsParameter("reasonTypeCode", reasonTypeCode));
    }

    public Result<ReasonType> mapToReasonType(Map<String, Object> payload) {
        String code = asString(payload.get("code"));
        String centralBankCode = asString(payload.get("centralBankCode"));
        String description = asString(payload.get("description"));
        String reasonType = asString(payload.get("reasonType"));
        boolean shouldHasSerial = Boolean.TRUE.equals(asBoolean(payload.get("shouldHasSerial")));
        boolean exemptionOfInquiryNumber = Boolean.TRUE.equals(asBoolean(payload.get("exemptionOfInquiryNumber")));
        if (code == null || description == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadReasonType"));
        }
        return ReasonType.of(code, centralBankCode, description, reasonType, shouldHasSerial, exemptionOfInquiryNumber);
    }

    public List<FcbJmsParameter> mapResourceByCodeParams(String resourceCode) {
        return List.of(new FcbJmsParameter("resourceCode", resourceCode));
    }

    public Result<SubSource> mapToSubSource(Map<String, Object> payload) {
        String code = asString(payload.get("code"));
        if (code == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadResourceByCode"));
        }
        return SubSource.of(code);
    }

    public List<FcbJmsParameter> mapTopicByCodeParams(List<String> topicCodes) {
        String topicsValue = String.join(SEPARATOR, topicCodes);
        return List.of(new FcbJmsParameter("topicCode", topicsValue));
    }

    @SuppressWarnings("unchecked")
    public Result<List<TopicInfo>> mapToTopicInfoList(Map<String, Object> payload) {
        Object topicsObj = payload.get("topics");
        if (topicsObj instanceof List<?> topics) {
            List<TopicInfo> result = new ArrayList<>();
            for (Object item : topics) {
                if (item instanceof Map<?, ?> topicMap) {
                    Long id = asLong(topicMap.get("id"));
                    String title = asString(topicMap.get("title"));
                    String code = asString(topicMap.get("code"));
                    Boolean isDebtor = asBoolean(topicMap.get("isDebtor"));
                    Boolean isUnderLine = asBoolean(topicMap.get("isUnderLine"));
                    String type = asString(topicMap.get("type"));
                    Boolean hasOppositeAccount = asBoolean(topicMap.get("hasOppositeAccount"));
                    String numOfOpenableAccounts = asString(topicMap.get("numOfOpenableAccounts"));
                    Boolean isPermanent = asBoolean(topicMap.get("isPermanent"));
                    Result<TopicInfo> topicResult = TopicInfo.of(
                            id,
                            title,
                            code,
                            isDebtor,
                            isUnderLine,
                            type,
                            hasOppositeAccount,
                            numOfOpenableAccounts,
                            isPermanent);
                    if (topicResult.isSuccessWithValue()) {
                        result.add(topicResult.orElseThrow());
                    }
                }
            }
            return Result.success(result);
        }
        return Result.failure(
                Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadTopicByCode"));
    }

    public List<FcbJmsParameter> mapCoveredBranchesParams(BranchCode branchCode) {
        return List.of(new FcbJmsParameter("branchCode", branchCode.value()));
    }

    @SuppressWarnings("unchecked")
    public Result<List<BranchCode>> mapToBranchCodeList(Map<String, Object> payload) {
        Object branchesObj = payload.get("branches");
        if (branchesObj instanceof List<?> branches) {
            List<BranchCode> result = new ArrayList<>();
            for (Object item : branches) {
                if (item instanceof Map<?, ?> branchMap) {
                    result.add(new BranchCode(asString(branchMap.get("code"))));
                } else if (item instanceof String code) {
                    result.add(new BranchCode(code));
                }
            }
            return Result.success(result);
        }
        return Result.failure(
                Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadCoveredBranches"));
    }

    public List<FcbJmsParameter> mapGetApplicationNumberParams(Branch branch, LoanTypeCode loanTypeCode, Party party) {
        return List.of(
                new FcbJmsParameter("branchCode", branch.code().value()),
                new FcbJmsParameter("loanTypeCode", loanTypeCode.value()),
                new FcbJmsParameter("customerNumber", party.customerNumber()));
    }

    public Result<ApplicationNumber> mapToApplicationNumber(
            Map<String, Object> payload, Branch branch, LoanTypeCode loanTypeCode, Party party) {
        String fileNumber = asString(payload.get("fileNumber"));
        if (fileNumber == null || fileNumber.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "getApplicationNumber"));
        }
        return ApplicationNumber.of(branch, loanTypeCode, party, fileNumber);
    }

    public List<FcbJmsParameter> mapLoadBranchParams(BranchCode branchCode) {
        return List.of(new FcbJmsParameter("branchCode", branchCode.value()));
    }

    public Result<BranchDetails> mapToBranchDetails(Map<String, Object> payload) {
        String code = asString(payload.get("code"));
        String name = asString(payload.get("name"));
        if (code == null) {
            return Result.failure(Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadBranch"));
        }
        return BranchDetails.of(
                code,
                name,
                asString(payload.get("foreignName")),
                asLong(payload.get("globalCode")),
                asString(payload.get("managerName")),
                asString(payload.get("samCode")),
                asString(payload.get("swiftCode")),
                asString(payload.get("clearBranch")),
                asString(payload.get("cityCode")),
                asString(payload.get("bankCode")));
    }

    // ── CustomerServicePort mappings ──

    public List<FcbJmsParameter> mapCustomerInfoParams(String customerNumber, CustomerInfoLoadOptions options) {
        List<FcbJmsParameter> params = new ArrayList<>();
        params.add(new FcbJmsParameter("customerNumber", customerNumber));
        params.add(new FcbJmsParameter("sequenceCode", options.sequenceCode()));
        params.add(new FcbJmsParameter("subsystem", options.subsystem()));
        params.add(new FcbJmsParameter("includeCapability", String.valueOf(options.includeCapability())));
        params.add(new FcbJmsParameter("includeBlackList", String.valueOf(options.includeBlackList())));
        params.add(new FcbJmsParameter("includeBaseInfo", String.valueOf(options.includeBaseInfo())));
        params.add(new FcbJmsParameter("includeGrayList", String.valueOf(options.includeGrayList())));
        return params;
    }

    public List<FcbJmsParameter> mapFindRelatedCustomersParams(List<String> customerNumbers) {
        String value = String.join(SEPARATOR, customerNumbers);
        return List.of(new FcbJmsParameter("customerNumbers", value));
    }

    public List<FcbJmsParameter> mapCustomerBirthInfoParams(String customerNumber) {
        return List.of(new FcbJmsParameter("customerNumber", customerNumber));
    }

    public Result<PartyBirthInfo> mapToPartyBirthInfo(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadCustomerBirthInfo"));
        }
        return Result.success(new PartyBirthInfo(
                asString(payload.get("customerNumber")),
                asInteger(payload.get("age")),
                Boolean.TRUE.equals(asBoolean(payload.get("isGrowthOrder"))),
                Boolean.TRUE.equals(asBoolean(payload.get("isUnderEighteenYearsOld"))),
                asString(payload.get("birthDate")),
                Boolean.TRUE.equals(asBoolean(payload.get("isCheckGrowthAg")))));
    }

    // ── DepositServicePort mappings ──

    public List<FcbJmsParameter> mapDepositInfoParams(DepositNumber depositNumber) {
        return List.of(new FcbJmsParameter("depositNumber", depositNumber.value()));
    }

    public List<FcbJmsParameter> mapDepositClosedParams(DepositNumber depositNumber, CurrencyType currencyType) {
        return List.of(
                new FcbJmsParameter("depositNumber", depositNumber.value()),
                new FcbJmsParameter("currencySwiftCode", currencyType.getCode()));
    }

    public Result<DepositClosedStatus> mapToDepositClosedStatus(Map<String, Object> payload) {
        Boolean closed = asBoolean(payload.get("closed"));
        String currencyTypeCode = asString(payload.get("currencyTypeCode"));
        if (closed == null || currencyTypeCode == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "isDepositClosed"));
        }
        return Result.success(new DepositClosedStatus(closed, currencyTypeCode));
    }

    public List<FcbJmsParameter> mapValidateDebtorDepositParams(
            DepositNumber depositNumber, CurrencyType currencyType) {
        return List.of(
                new FcbJmsParameter("depositNumber", depositNumber.value()),
                new FcbJmsParameter("currencySwiftCode", currencyType.getCode()));
    }

    public Result<DebtorDepositValidation> mapToDebtorDepositValidation(Map<String, Object> payload) {
        Boolean valid = asBoolean(payload.get("valid"));
        if (valid == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "validateDebtorDeposit"));
        }
        return Result.success(new DebtorDepositValidation(valid));
    }

    public List<FcbJmsParameter> mapValidateCreditorDepositParams(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount) {
        return List.of(
                new FcbJmsParameter("depositNumber", depositNumber.value()),
                new FcbJmsParameter("currencySwiftCode", currencyType.getCode()),
                new FcbJmsParameter("amount", String.valueOf(amount)));
    }

    public Result<CreditorDepositValidation> mapToCreditorDepositValidation(Map<String, Object> payload) {
        Boolean valid = asBoolean(payload.get("valid"));
        if (valid == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "validateCreditorDeposit"));
        }
        return Result.success(new CreditorDepositValidation(valid));
    }

    public List<FcbJmsParameter> mapHasDepositAllowedCurrenciesParams(
            DepositNumber depositNumber, List<CurrencyType> currencyTypes) {
        String currenciesValue =
                currencyTypes.stream().map(CurrencyType::getCode).collect(Collectors.joining(SEPARATOR));
        return List.of(
                new FcbJmsParameter("depositNumber", depositNumber.value()),
                new FcbJmsParameter("currencies", currenciesValue));
    }

    public Result<CurrencyValidation> mapToCurrencyValidation(Map<String, Object> payload) {
        Boolean allowed = asBoolean(payload.get("allowed"));
        String message = asString(payload.get("message"));
        if (allowed == null) {
            return Result.failure(Notification.ofError(
                    FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "hasDepositAllowedCurrencies"));
        }
        return Result.success(new CurrencyValidation(allowed, message));
    }

    public List<FcbJmsParameter> mapGetDepositSignerOwnerParams(String depositNumber) {
        return List.of(new FcbJmsParameter("depositNumber", depositNumber));
    }

    // ── CollateralServicePort mappings ──

    public List<FcbJmsParameter> mapValidateAssuranceParams(
            List<CollateralSerial> serials, List<Long> usedCosts, BranchCode branchCode) {
        String serialsValue = serials.stream().map(CollateralSerial::value).collect(Collectors.joining(SEPARATOR));
        String costsValue = usedCosts.stream().map(String::valueOf).collect(Collectors.joining(SEPARATOR));
        String repeatedBranch =
                Collections.nCopies(serials.size(), branchCode.value()).stream().collect(Collectors.joining(SEPARATOR));
        return List.of(
                new FcbJmsParameter("assuranceSerial", serialsValue),
                new FcbJmsParameter("usedCost", costsValue),
                new FcbJmsParameter("branchCode", repeatedBranch));
    }

    public Result<CollateralValidation> mapToCollateralValidation(Map<String, Object> payload) {
        Boolean valid = asBoolean(payload.get("valid"));
        String message = asString(payload.get("message"));
        if (valid == null) {
            return Result.failure(Notification.ofError(
                    FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "validateAddAssuranceToFile"));
        }
        return Result.success(new CollateralValidation(valid, message));
    }

    public List<FcbJmsParameter> mapReserveCollateralParams(
            CollateralSerial serial,
            ApplicationNumber appNumber,
            java.util.UUID requestId,
            Integer reserveDurationMin,
            Money usedAmount) {
        return List.of(
                new FcbJmsParameter("assuranceSerial", serial.value()),
                new FcbJmsParameter("fileNumber", appNumber.formattedApplicationNumber()),
                new FcbJmsParameter("transactionId", java.util.UUID.randomUUID().toString()),
                new FcbJmsParameter("reserveDurationMin", String.valueOf(reserveDurationMin)),
                new FcbJmsParameter("amount", usedAmount.value().toString()));
    }

    @SuppressWarnings("unchecked")
    public Result<List<CollateralSerial>> mapToCollateralSerials(Map<String, Object> payload) {
        Object serialsObj = payload.get("serials");
        if (serialsObj instanceof List<?> serials) {
            List<CollateralSerial> result = new ArrayList<>();
            for (Object item : serials) {
                result.add(new CollateralSerial(asString(item)));
            }
            return Result.success(result);
        }
        return Result.failure(
                Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "reserveCollateral"));
    }

    public List<FcbJmsParameter> mapLoadCollateralParams(String assuranceSerial, String uniqueTrackingCode) {
        return List.of(
                new FcbJmsParameter("assuranceSerial", assuranceSerial),
                new FcbJmsParameter("uniqueTrackingCode", uniqueTrackingCode));
    }

    public Result<CollateralDetails> mapToCollateralDetails(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadCollateral"));
        }
        return Result.success(new CollateralDetails(
                asString(payload.get("serial")),
                asString(payload.get("customerNo")),
                asString(payload.get("assuranceTypeCode")),
                asString(payload.get("assuranceTypeName")),
                asBigDecimal(payload.get("guaranteeAmount")),
                asBigDecimal(payload.get("price")),
                asBigDecimal(payload.get("usedMortgagePrice")),
                asInteger(payload.get("guaranteeDuration")),
                asString(payload.get("guaranteeNumber")),
                asString(payload.get("guaranteeIssuer")),
                asString(payload.get("guaranteeBranchCode")),
                asString(payload.get("loanFileNumber")),
                asString(payload.get("branchCode")),
                asString(payload.get("currency")),
                Boolean.TRUE.equals(asBoolean(payload.get("active"))),
                Boolean.TRUE.equals(asBoolean(payload.get("isEscrowed"))),
                Boolean.TRUE.equals(asBoolean(payload.get("isReleaseAllowed"))),
                Boolean.TRUE.equals(asBoolean(payload.get("isSpecial"))),
                asString(payload.get("address"))));
    }

    public List<FcbJmsParameter> mapUnReserveCollateralParams(
            CollateralSerial serial,
            ApplicationNumber appNumber,
            java.util.UUID transactionId,
            java.util.UUID rollBackId) {
        return List.of(
                new FcbJmsParameter("assuranceSerial", serial.value()),
                new FcbJmsParameter("fileNumber", appNumber.formattedApplicationNumber()),
                new FcbJmsParameter("transactionId", transactionId.toString()),
                new FcbJmsParameter("rollBackId", rollBackId.toString()));
    }

    // ── CustomerServicePort response mappings ──

    public Result<PartyInfoResponse> mapToPartyInfoResponse(
            Map<String, Object> payload, PartyRole role, @Nullable BigDecimal guaranteePercentage) {
        String customerNumber = asString(payload.get("customerNumber"));
        if (customerNumber == null || customerNumber.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "loadCustomerInfo"));
        }
        String firstName = asString(payload.get("firstName"));
        String lastName = asString(payload.get("lastName"));
        String title = asString(payload.get("title"));
        CustomerName customerName = new CustomerName(firstName, lastName, title);
        PartyType partyType = Boolean.TRUE.equals(asBoolean(payload.get("real"))) ? PartyType.REAL : PartyType.LEGAL;

        Party party =
                switch (role) {
                    case PRIMARY_APPLICANT -> new ApplicantParty(customerNumber, partyType, customerName);
                    case CO_APPLICANT -> new CoApplicantParty(customerNumber, partyType, customerName);
                    case GUARANTOR ->
                        GuarantorParty.of(
                                        customerNumber,
                                        partyType,
                                        customerName,
                                        guaranteePercentage != null
                                                ? GuaranteePercentage.of(guaranteePercentage)
                                                : null)
                                .orElseThrow();
                };

        String nationalCodeStr = asString(payload.get("nationalCode"));
        Result<NationalCode> nationalCodeResult = NationalCode.valueOf(nationalCodeStr);
        if (nationalCodeResult.isFailure()) {
            return Result.failure(nationalCodeResult.notification());
        }

        return Result.success(new PartyInfoResponse(
                party,
                nationalCodeResult.orElseThrow(),
                Boolean.TRUE.equals(asBoolean(payload.get("isInBlackList"))),
                Boolean.TRUE.equals(asBoolean(payload.get("isIncapable"))),
                Boolean.TRUE.equals(asBoolean(payload.get("isInGrayList")))));
    }

    @SuppressWarnings("unchecked")
    public Result<List<PartyInfoResponse>> mapToPartyInfoResponseList(Map<String, Object> payload) {
        Object customersObj = payload.get("customers");
        if (customersObj instanceof List<?> customers) {
            List<PartyInfoResponse> result = new ArrayList<>();
            for (Object item : customers) {
                if (item instanceof Map<?, ?> customerMap) {
                    Result<PartyInfoResponse> mapped = mapToPartyInfoResponse(
                            (Map<String, Object>) customerMap, PartyRole.PRIMARY_APPLICANT, null);
                    if (mapped.isSuccessWithValue()) {
                        result.add(mapped.orElseThrow());
                    }
                }
            }
            return Result.success(result);
        }
        return Result.failure(
                Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "findRelatedCustomers"));
    }

    // ── DepositServicePort response mappings ──

    @SuppressWarnings("unchecked")
    public Result<DepositInfo> mapToDepositInfo(Map<String, Object> payload) {
        String number = asString(payload.get("number"));
        if (number == null || number.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "getDepositInfo"));
        }
        Result<DepositNumber> numberResult = DepositNumber.valueOf(number);
        if (numberResult.isFailure()) {
            return Result.failure(numberResult.notification());
        }

        CurrencyType currencyType = null;
        Object currencyObj = payload.get("currency");
        if (currencyObj instanceof Map<?, ?> currencyMap) {
            String currencyCode = asString(currencyMap.get("code"));
            if (currencyCode != null) {
                Result<CurrencyType> currencyResult = CurrencyType.valueOf(currencyCode);
                if (currencyResult.isSuccessWithValue()) {
                    currencyType = currencyResult.orElseThrow();
                }
            }
        } else if (currencyObj instanceof String currencyCode) {
            Result<CurrencyType> currencyResult = CurrencyType.valueOf(currencyCode);
            if (currencyResult.isSuccessWithValue()) {
                currencyType = currencyResult.orElseThrow();
            }
        }

        List<String> ownerNationalCodes = new ArrayList<>();
        Object ownersObj = payload.get("ownerNationalCodes");
        if (ownersObj instanceof List<?> owners) {
            for (Object o : owners) {
                if (o instanceof String s) ownerNationalCodes.add(s);
            }
        }

        return DepositInfo.of(
                numberResult.orElseThrow(),
                asString(payload.get("title")),
                asString(payload.get("type")),
                currencyType,
                asString(payload.get("status")),
                Boolean.TRUE.equals(asBoolean(payload.get("isExternalDeposit"))),
                asString(payload.get("branchCode")),
                ownerNationalCodes);
    }

    // ── FetchSanctionDetailsPort mappings ──

    public List<FcbJmsParameter> mapFetchSanctionParams(String sanctionSerial) {
        return List.of(new FcbJmsParameter("sanctionSerial", sanctionSerial));
    }

    public Result<SanctionDetails> mapToSanctionDetails(Map<String, Object> payload) {
        String sanctionSerial = asString(payload.get("sanctionSerial"));
        if (sanctionSerial == null || sanctionSerial.isBlank()) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, "fetchSanctionDetails"));
        }

        SanctionType sanctionType = null;
        String sanctionTypeStr = asString(payload.get("sanctionType"));
        if (sanctionTypeStr != null) {
            try {
                sanctionType = SanctionType.valueOf(sanctionTypeStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        BigDecimal approvedAmount = asBigDecimal(payload.get("approvedAmount"));

        CurrencyType currency = null;
        String currencyCode = asString(payload.get("currency"));
        if (currencyCode != null) {
            Result<CurrencyType> currencyResult = CurrencyType.valueOf(currencyCode);
            if (currencyResult.isSuccessWithValue()) {
                currency = currencyResult.orElseThrow();
            }
        }

        Period gracePeriod = parsePeriod(payload.get("gracePeriod"));
        Integer installmentCount = asInteger(payload.get("installmentCount"));
        Period loanDuration = parsePeriod(payload.get("loanDuration"));

        DisbursementMethod disbursementMethod = null;
        String disbursementMethodStr = asString(payload.get("disbursementMethod"));
        if (disbursementMethodStr != null) {
            try {
                disbursementMethod = DisbursementMethod.valueOf(disbursementMethodStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        ConfirmType confirmType = null;
        String personCode = asString(payload.get("confirmType"));
        if (personCode != null) {
            Result<ConfirmType> ctResult = ConfirmType.of(personCode);
            if (ctResult.isSuccessWithValue()) {
                confirmType = ctResult.orElseThrow();
            }
        }

        return Result.success(new SanctionDetails(
                sanctionSerial,
                sanctionType,
                approvedAmount,
                currency,
                gracePeriod,
                installmentCount,
                loanDuration,
                disbursementMethod,
                asString(payload.get("lifeInsuranceId")),
                asString(payload.get("collateralSerial")),
                asString(payload.get("revocationReason")),
                confirmType));
    }

    // ── Helper methods ──

    private Period parsePeriod(Object value) {
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Period.parse(s);
            } catch (Exception ignored) {
            }
            try {
                int months = Integer.parseInt(s);
                return Period.ofMonths(months);
            } catch (NumberFormatException ignored) {
            }
        }
        if (value instanceof Number n) {
            return Period.ofMonths(n.intValue());
        }
        return null;
    }

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Boolean asBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return null;
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s && s.matches("-?\\d+")) return Integer.parseInt(s);
        return null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s && s.matches("-?\\d+")) return Long.parseLong(s);
        return null;
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (value instanceof String s) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
