package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbMessagingProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsParameter;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbJmsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbJmsLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.JmsValidationMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("activemq")
public class FcbValidationJmsAdapter
        implements LoanServicePort,
                CustomerServicePort,
                DepositServicePort,
                CollateralServicePort,
                FetchSanctionDetailsPort {

    private final FcbJmsClient jmsClient;
    private final JmsTemplate jmsTemplate;
    private final FcbMessagingProperties properties;

    public FcbValidationJmsAdapter(
            FcbJmsClient jmsClient,
            @Qualifier("fcbValidationJmsTemplate") JmsTemplate jmsTemplate,
            FcbMessagingProperties properties) {
        this.jmsClient = jmsClient;
        this.jmsTemplate = jmsTemplate;
        this.properties = properties;
    }

    // ── LoanServicePort ──

    @Override
    public Result<EconomicSector> loadEconomicalSectorByCode(EconomicSector economicSector) {
        return sendAndMap(
                "load-economicalSection-by-code",
                JmsValidationMapper.mapEconomicalSectorByCodeParams(economicSector),
                response -> JmsValidationMapper.mapToEconomicSector(response.payload()));
    }

    @Override
    public Result<EconomicalSectorResponse> loadEconomicalSector(EconomicSector economicSector) {
        return sendAndMap(
                "load-economicalSection-by-code",
                JmsValidationMapper.mapEconomicalSectorByCodeParams(economicSector),
                response -> JmsValidationMapper.mapToEconomicalSectorResponse(response.payload()));
    }

    @Override
    public Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            EconomicSector economicSector, LoanTypeCode loanTypeCode) {
        return sendAndMap(
                "validate-ecoSection-loanType",
                JmsValidationMapper.mapValidateEcoSectorParams(economicSector, loanTypeCode),
                response -> JmsValidationMapper.mapToEcoSectorValidation(response.payload()));
    }

    @Override
    public Result<ReasonType> loadReasonTypeForCreate(String reasonTypeCode) {
        return sendAndMap(
                "load-reason-type-for-create",
                JmsValidationMapper.mapReasonTypeParams(reasonTypeCode),
                response -> JmsValidationMapper.mapToReasonType(response.payload()));
    }

    @Override
    public Result<ReasonType> loadReasonTypeForRevoke(String reasonTypeCode) {
        return sendAndMap(
                "load-reason-type-for-revoke",
                JmsValidationMapper.mapReasonTypeParams(reasonTypeCode),
                response -> JmsValidationMapper.mapToReasonType(response.payload()));
    }

    @Override
    public Result<SubSource> loadResourceByCode(String subSourceCode) {
        return sendAndMap(
                "load-resource-by-code",
                JmsValidationMapper.mapResourceByCodeParams(subSourceCode),
                response -> JmsValidationMapper.mapToSubSource(response.payload()));
    }

    @Override
    public Result<List<TopicInfo>> loadTopicByCode(List<String> topicCodes) {
        return sendAndMap(
                "load-topic-by-code",
                JmsValidationMapper.mapTopicByCodeParams(topicCodes),
                response -> JmsValidationMapper.mapToTopicInfoList(response.payload()));
    }

    @Override
    public Result<List<BranchCode>> loadCoveredBranches(BranchCode branchCode) {
        return sendAndMap(
                "load-covered-branches",
                JmsValidationMapper.mapCoveredBranchesParams(branchCode),
                response -> JmsValidationMapper.mapToBranchCodeList(response.payload()));
    }

    @Override
    public Result<ApplicationNumber> getApplicationNumber(Branch branch, LoanTypeCode loanTypeCode, Party party) {
        return sendAndMap(
                "get-loan-file-number",
                JmsValidationMapper.mapGetApplicationNumberParams(branch, loanTypeCode, party),
                response ->
                        JmsValidationMapper.mapToApplicationNumber(response.payload(), branch, loanTypeCode, party));
    }

    @Override
    public Result<BranchDetails> loadBranch(BranchCode branchCode) {
        return sendAndMap(
                "load-branch-nova",
                JmsValidationMapper.mapLoadBranchParams(branchCode),
                response -> JmsValidationMapper.mapToBranchDetails(response.payload()));
    }

    // ── CustomerServicePort ──

    @Override
    public Result<PartyInfoResponse> loadCustomerInfo(
            String customerNumber,
            @NotNull PartyRole role,
            @Nullable BigDecimal guaranteePercentage,
            CustomerInfoLoadOptions options) {
        return sendAndMap(
                "load-customer-info",
                JmsValidationMapper.mapCustomerInfoParams(customerNumber, options),
                response -> JmsValidationMapper.mapToPartyInfoResponse(response.payload(), role, guaranteePercentage));
    }

    @Override
    public Result<List<PartyInfoResponse>> findRelatedCustomers(List<String> customerNumbers) {
        return sendAndMap(
                "find-related-customers",
                JmsValidationMapper.mapFindRelatedCustomersParams(customerNumbers),
                response -> JmsValidationMapper.mapToPartyInfoResponseList(response.payload()));
    }

    @Override
    public Result<PartyBirthInfo> loadCustomerBirthInfo(String customerNumber) {
        return sendAndMap(
                "load-customer-birth-info",
                JmsValidationMapper.mapCustomerBirthInfoParams(customerNumber),
                response -> JmsValidationMapper.mapToPartyBirthInfo(response.payload()));
    }

    // ── DepositServicePort ──

    @Override
    public Result<DepositInfo> getDepositInfo(DepositNumber depositNumber) {
        return sendAndMap(
                "load-deposit-by-number",
                JmsValidationMapper.mapDepositInfoParams(depositNumber),
                response -> JmsValidationMapper.mapToDepositInfo(response.payload()));
    }

    @Override
    public Result<DepositClosedStatus> isDepositClosed(DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                "is-deposit-closed",
                JmsValidationMapper.mapDepositClosedParams(depositNumber, currencyType),
                response -> JmsValidationMapper.mapToDepositClosedStatus(response.payload()));
    }

    @Override
    public Result<DebtorDepositValidation> validateDebtorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                "validate-debtor-deposit",
                JmsValidationMapper.mapValidateDebtorDepositParams(depositNumber, currencyType),
                response -> JmsValidationMapper.mapToDebtorDepositValidation(response.payload()));
    }

    @Override
    public Result<CreditorDepositValidation> validateCreditorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount) {
        return sendAndMap(
                "validate-creditor-deposit",
                JmsValidationMapper.mapValidateCreditorDepositParams(depositNumber, currencyType, amount),
                response -> JmsValidationMapper.mapToCreditorDepositValidation(response.payload()));
    }

    @Override
    public Result<CurrencyValidation> hasDepositAllowedCurrencies(
            DepositNumber depositNumber, List<CurrencyType> currencyTypes) {
        return sendAndMap(
                "has-deposit-allowed-currencies",
                JmsValidationMapper.mapHasDepositAllowedCurrenciesParams(depositNumber, currencyTypes),
                response -> JmsValidationMapper.mapToCurrencyValidation(response.payload()));
    }

    @Override
    public Result<List<PartyInfoResponse>> getAllDepositSignerOwnerCustomer(String depositNumber) {
        return sendAndMap(
                "get-all-deposit-signer-owner-customer",
                JmsValidationMapper.mapGetDepositSignerOwnerParams(depositNumber),
                response -> JmsValidationMapper.mapToPartyInfoResponseList(response.payload()));
    }

    // ── CollateralServicePort ──

    @Override
    public Result<CollateralValidation> validateAddAssuranceToFile(
            List<CollateralSerial> collateralSerials, List<Long> usedCosts, BranchCode branchCode) {
        return sendAndMap(
                "validate-add-assurance-to-file",
                JmsValidationMapper.mapValidateAssuranceParams(collateralSerials, usedCosts, branchCode),
                response -> JmsValidationMapper.mapToCollateralValidation(response.payload()));
    }

    @Override
    public Result<List<CollateralSerial>> reserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID requestId,
            Integer reserveDurationMin,
            Money usedAmount) {
        return sendAndMap(
                "reserve-assurance-for-file",
                JmsValidationMapper.mapReserveCollateralParams(
                        collateralSerial, applicationNumber, requestId, reserveDurationMin, usedAmount),
                response -> JmsValidationMapper.mapToCollateralSerials(response.payload()));
    }

    @Override
    public Result<CollateralDetails> loadCollateral(String assuranceSerial, String uniqueTrackingCode) {
        return sendAndMap(
                "load-assurance-service",
                JmsValidationMapper.mapLoadCollateralParams(assuranceSerial, uniqueTrackingCode),
                response -> JmsValidationMapper.mapToCollateralDetails(response.payload()));
    }

    @Override
    public Result<CollateralSerial> unReserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID transactionId,
            UUID rollBackId) {
        Result<FcbJmsResponse> result = sendRequest(
                "un-reserve-assurance-for-file",
                JmsValidationMapper.mapUnReserveCollateralParams(
                        collateralSerial, applicationNumber, transactionId, rollBackId));
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success(collateralSerial);
    }

    // ── FetchSanctionDetailsPort ──

    @Override
    public Result<SanctionDetails> fetchBySanctionSerial(String sanctionSerial) {
        return sendAndMap(
                "fetch-sanction-details",
                JmsValidationMapper.mapFetchSanctionParams(sanctionSerial),
                response -> JmsValidationMapper.mapToSanctionDetails(response.payload()));
    }

    // ── Internal helpers ──

    private <T> Result<T> sendAndMap(
            String operationName,
            List<FcbJmsParameter> parameters,
            java.util.function.Function<FcbJmsResponse, Result<T>> responseMapper) {

        Result<FcbJmsResponse> result = sendRequest(operationName, parameters);
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbJmsResponse response = result.orElseThrow();
        if (response.payload() == null) {
            return Result.failure(
                    Notification.ofError(FcbJmsLocalizedMessageCodes.JMS_INVALID_RESPONSE, operationName));
        }
        return responseMapper.apply(response);
    }

    private Result<FcbJmsResponse> sendRequest(String operationName, List<FcbJmsParameter> parameters) {
        FcbJmsRequest request = new FcbJmsRequest(UUID.randomUUID().toString(), operationName, parameters, null);
        return jmsClient.sendAndReceive(jmsTemplate, request, properties.validation());
    }
}
