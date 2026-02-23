package ir.dotin.loan.trade.adapters.driven.fcbmessaging.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Profile;
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
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.response.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.i18n.FcbKafkaLocalizedMessageCodes;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaValidationMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("kafka-fcb")
@RequiredArgsConstructor
public class FcbValidationKafkaAdapter
        implements LoanServicePort,
                CustomerServicePort,
                DepositServicePort,
                CollateralServicePort,
                FetchSanctionDetailsPort {

    private final FcbKafkaClient kafkaClient;
    private final FcbKafkaProperties properties;

    // ── LoanServicePort ──

    @Override
    public Result<EconomicSector> loadEconomicalSectorByCode(EconomicSector economicSector) {
        return sendAndMap(
                new LoadEconomicSectorRequest(economicSector.code()),
                EconomicSectorKafkaResponse.class,
                KafkaValidationMapper::mapToEconomicSector);
    }

    @Override
    public Result<EconomicalSectorResponse> loadEconomicalSector(EconomicSector economicSector) {
        return sendAndMap(
                new LoadEconomicSectorRequest(economicSector.code()),
                EconomicSectorKafkaResponse.class,
                KafkaValidationMapper::mapToEconomicalSectorResponse);
    }

    @Override
    public Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            EconomicSector economicSector, LoanTypeCode loanTypeCode) {
        return sendAndMap(
                new ValidateEcoSectorRequest(economicSector.code(), loanTypeCode.value()),
                EcoSectorValidationKafkaResponse.class,
                KafkaValidationMapper::mapToEcoSectorValidation);
    }

    @Override
    public Result<ReasonType> loadReasonTypeForCreate(String reasonTypeCode) {
        return sendAndMap(
                new LoadReasonTypeForCreateRequest(reasonTypeCode),
                ReasonTypeKafkaResponse.class,
                KafkaValidationMapper::mapToReasonType);
    }

    @Override
    public Result<ReasonType> loadReasonTypeForRevoke(String reasonTypeCode) {
        return sendAndMap(
                new LoadReasonTypeForRevokeRequest(reasonTypeCode),
                ReasonTypeKafkaResponse.class,
                KafkaValidationMapper::mapToReasonType);
    }

    @Override
    public Result<SubSource> loadResourceByCode(String subSourceCode) {
        return sendAndMap(
                new LoadResourceRequest(subSourceCode),
                ResourceKafkaResponse.class,
                KafkaValidationMapper::mapToSubSource);
    }

    @Override
    public Result<List<TopicInfo>> loadTopicByCode(List<String> topicCodes) {
        return sendAndMap(
                new LoadTopicRequest(topicCodes),
                TopicInfoListKafkaResponse.class,
                KafkaValidationMapper::mapToTopicInfoList);
    }

    @Override
    public Result<List<BranchCode>> loadCoveredBranches(BranchCode branchCode) {
        return sendAndMap(
                new LoadCoveredBranchesRequest(branchCode.value()),
                BranchCodeListKafkaResponse.class,
                KafkaValidationMapper::mapToBranchCodeList);
    }

    @Override
    public Result<ApplicationNumber> getApplicationNumber(Branch branch, LoanTypeCode loanTypeCode, Party party) {
        var request =
                new GetApplicationNumberRequest(branch.code().value(), loanTypeCode.value(), party.customerNumber());
        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.defaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!(raw instanceof ApplicationNumberKafkaResponse response)) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "getApplicationNumber"));
        }
        return KafkaValidationMapper.mapToApplicationNumber(response, branch, loanTypeCode, party);
    }

    @Override
    public Result<BranchDetails> loadBranch(BranchCode branchCode) {
        return sendAndMap(
                new LoadBranchRequest(branchCode.value()),
                BranchDetailsKafkaResponse.class,
                KafkaValidationMapper::mapToBranchDetails);
    }

    // ── CustomerServicePort ──

    @Override
    public Result<PartyInfoResponse> loadCustomerInfo(
            String customerNumber,
            @NotNull PartyRole role,
            @Nullable BigDecimal guaranteePercentage,
            CustomerInfoLoadOptions options) {
        var request = new LoadCustomerInfoRequest(
                customerNumber,
                options.sequenceCode(),
                options.subsystem(),
                options.includeCapability(),
                options.includeBlackList(),
                options.includeBaseInfo(),
                options.includeGrayList());
        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.defaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!(raw instanceof CustomerInfoKafkaResponse response)) {
            return Result.failure(
                    Notification.ofError(FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, "loadCustomerInfo"));
        }
        return KafkaValidationMapper.mapToPartyInfoResponse(response, role, guaranteePercentage);
    }

    @Override
    public Result<List<PartyInfoResponse>> findRelatedCustomers(List<String> customerNumbers) {
        return sendAndMap(
                new FindRelatedCustomersRequest(customerNumbers),
                CustomerListKafkaResponse.class,
                KafkaValidationMapper::mapToPartyInfoResponseList);
    }

    @Override
    public Result<PartyBirthInfo> loadCustomerBirthInfo(String customerNumber) {
        return sendAndMap(
                new LoadCustomerBirthInfoRequest(customerNumber),
                CustomerBirthInfoKafkaResponse.class,
                KafkaValidationMapper::mapToPartyBirthInfo);
    }

    // ── DepositServicePort ──

    @Override
    public Result<DepositInfo> getDepositInfo(DepositNumber depositNumber) {
        return sendAndMap(
                new LoadDepositInfoRequest(depositNumber.value()),
                DepositInfoKafkaResponse.class,
                KafkaValidationMapper::mapToDepositInfo);
    }

    @Override
    public Result<DepositClosedStatus> isDepositClosed(DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                new IsDepositClosedRequest(depositNumber.value(), currencyType.getCode()),
                DepositClosedKafkaResponse.class,
                KafkaValidationMapper::mapToDepositClosedStatus);
    }

    @Override
    public Result<DebtorDepositValidation> validateDebtorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                new ValidateDebtorDepositRequest(depositNumber.value(), currencyType.getCode()),
                ValidationResultKafkaResponse.class,
                KafkaValidationMapper::mapToDebtorDepositValidation);
    }

    @Override
    public Result<CreditorDepositValidation> validateCreditorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount) {
        return sendAndMap(
                new ValidateCreditorDepositRequest(depositNumber.value(), currencyType.getCode(), amount),
                ValidationResultKafkaResponse.class,
                KafkaValidationMapper::mapToCreditorDepositValidation);
    }

    @Override
    public Result<CurrencyValidation> hasDepositAllowedCurrencies(
            DepositNumber depositNumber, List<CurrencyType> currencyTypes) {
        List<String> currencies =
                currencyTypes.stream().map(CurrencyType::getCode).collect(Collectors.toList());
        return sendAndMap(
                new HasDepositAllowedCurrenciesRequest(depositNumber.value(), currencies),
                CurrencyValidationKafkaResponse.class,
                KafkaValidationMapper::mapToCurrencyValidation);
    }

    @Override
    public Result<List<PartyInfoResponse>> getAllDepositSignerOwnerCustomer(String depositNumber) {
        return sendAndMap(
                new GetDepositSignerOwnerRequest(depositNumber),
                CustomerListKafkaResponse.class,
                KafkaValidationMapper::mapToPartyInfoResponseList);
    }

    // ── CollateralServicePort ──

    @Override
    public Result<CollateralValidation> validateAddAssuranceToFile(
            List<CollateralSerial> collateralSerials, List<Long> usedCosts, BranchCode branchCode) {
        List<String> serials =
                collateralSerials.stream().map(CollateralSerial::value).collect(Collectors.toList());
        List<String> branchCodes = Collections.nCopies(collateralSerials.size(), branchCode.value());
        return sendAndMap(
                new ValidateAssuranceRequest(serials, usedCosts, branchCodes),
                CollateralValidationKafkaResponse.class,
                KafkaValidationMapper::mapToCollateralValidation);
    }

    @Override
    public Result<List<CollateralSerial>> reserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID requestId,
            Integer reserveDurationMin,
            Money usedAmount) {
        return sendAndMap(
                new ReserveCollateralRequest(
                        collateralSerial.value(),
                        applicationNumber.formattedApplicationNumber(),
                        UUID.randomUUID().toString(),
                        reserveDurationMin,
                        usedAmount.value()),
                CollateralSerialsKafkaResponse.class,
                KafkaValidationMapper::mapToCollateralSerials);
    }

    @Override
    public Result<CollateralDetails> loadCollateral(String assuranceSerial, String uniqueTrackingCode) {
        return sendAndMap(
                new LoadCollateralRequest(assuranceSerial, uniqueTrackingCode),
                CollateralDetailsKafkaResponse.class,
                KafkaValidationMapper::mapToCollateralDetails);
    }

    @Override
    public Result<CollateralSerial> unReserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID transactionId,
            UUID rollBackId) {
        var request = new UnReserveCollateralRequest(
                collateralSerial.value(),
                applicationNumber.formattedApplicationNumber(),
                transactionId.toString(),
                rollBackId.toString());
        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.defaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success(collateralSerial);
    }

    // ── FetchSanctionDetailsPort ──

    @Override
    public Result<SanctionDetails> fetchBySanctionSerial(String sanctionSerial) {
        return sendAndMap(
                new FetchSanctionDetailsRequest(sanctionSerial),
                SanctionDetailsKafkaResponse.class,
                KafkaValidationMapper::mapToSanctionDetails);
    }

    // ── Internal helpers ──

    private <R extends FcbKafkaBaseResponse, T> Result<T> sendAndMap(
            FcbKafkaBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.defaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!responseType.isInstance(raw)) {
            return Result.failure(Notification.ofError(
                    FcbKafkaLocalizedMessageCodes.KAFKA_INVALID_RESPONSE, request.getOperationName()));
        }
        return responseMapper.apply(responseType.cast(raw));
    }
}
