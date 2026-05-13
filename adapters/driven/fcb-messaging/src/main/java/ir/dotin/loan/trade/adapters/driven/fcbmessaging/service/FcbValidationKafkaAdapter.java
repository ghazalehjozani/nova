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

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.vo.DepositInfo;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbKafkaProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbKafkaBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.KafkaValidationMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;
import ir.dotin.loan.trade.core.application.ports.outbound.client.samat.ValidateSamatPort;

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
                FetchSanctionDetailsPort,
                ValidateSamatPort {

    private final FcbKafkaClient kafkaClient;
    private final FcbKafkaProperties properties;

    @Override
    public Result<EconomicSector> loadEconomicalSectorByCode(EconomicSector economicSector) {
        return sendAndMap(
                LoadEconomicSectorRequest.builder()
                        .economicalSectionCode(economicSector.code())
                        .build(),
                EconomicSectorKafkaResponse.class,
                KafkaValidationMapper::mapToEconomicSector);
    }

    @Override
    public Result<EconomicalSectorResponse> loadEconomicalSector(EconomicSector economicSector) {
        return sendAndMap(
                LoadEconomicSectorRequest.builder()
                        .economicalSectionCode(economicSector.code())
                        .build(),
                EconomicSectorKafkaResponse.class,
                KafkaValidationMapper::mapToEconomicalSectorResponse);
    }

    @Override
    public Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            EconomicSector economicSector, LoanTypeCode loanTypeCode) {
        return sendAndMap(
                ValidateEcoSectorRequest.builder()
                        .economicalSectionCode(economicSector.code())
                        .loanTypeCode(loanTypeCode.value())
                        .build(),
                EcoSectorValidationKafkaResponse.class,
                KafkaValidationMapper::mapToEcoSectorValidation);
    }

    @Override
    public Result<ReasonType> loadReasonTypeForCreate(String reasonTypeCode) {
        return sendAndMap(
                LoadReasonTypeForCreateRequest.builder()
                        .reasonTypeCode(reasonTypeCode)
                        .build(),
                ReasonTypeKafkaResponse.class,
                KafkaValidationMapper::mapToReasonType);
    }

    @Override
    public Result<ReasonType> loadReasonTypeForRevoke(String reasonTypeCode) {
        return sendAndMap(
                LoadReasonTypeForRevokeRequest.builder()
                        .reasonTypeCode(reasonTypeCode)
                        .build(),
                ReasonTypeKafkaResponse.class,
                KafkaValidationMapper::mapToReasonType);
    }

    @Override
    public Result<SubSource> loadResourceByCode(String subSourceCode) {
        return sendAndMap(
                LoadResourceRequest.builder().resourceCode(subSourceCode).build(),
                ResourceKafkaResponse.class,
                KafkaValidationMapper::mapToSubSource);
    }

    @Override
    public Result<List<TopicInfo>> loadTopicByCode(List<String> topicCodes) {
        return sendAndMap(
                LoadTopicRequest.builder().topicCodes(topicCodes).build(),
                TopicInfoListKafkaResponse.class,
                KafkaValidationMapper::mapToTopicInfoList);
    }

    @Override
    public Result<List<BranchCode>> loadCoveredBranches(BranchCode branchCode) {
        return sendAndMap(
                LoadCoveredBranchesRequest.builder()
                        .branchCode(branchCode.value())
                        .build(),
                BranchCodeListKafkaResponse.class,
                KafkaValidationMapper::mapToBranchCodeList);
    }

    @Override
    public Result<ApplicationNumber> getApplicationNumber(Branch branch, LoanTypeCode loanTypeCode, Party party) {
        var request = GetApplicationNumberRequest.builder()
                .branchCode(branch.code().value())
                .loanTypeCode(loanTypeCode.value())
                .customerNumber(party.customerNumber())
                .build();

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getDefaultTimeout());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!(raw instanceof ApplicationNumberKafkaResponse response)) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "getApplicationNumber"));
        }
        return KafkaValidationMapper.mapToApplicationNumber(response, branch, loanTypeCode, party);
    }

    @Override
    public Result<BranchDetails> loadBranch(BranchCode branchCode) {
        return sendAndMap(
                LoadBranchRequest.builder().branchCode(branchCode.value()).build(),
                BranchDetailsKafkaResponse.class,
                KafkaValidationMapper::mapToBranchDetails);
    }

    @Override
    public Result<PartyInfoResponse> loadCustomerInfo(
            String customerNumber,
            @NotNull PartyRole role,
            @Nullable BigDecimal guaranteePercentage,
            CustomerInfoLoadOptions options) {

        var request = LoadCustomerInfoRequest.builder()
                .customerNumber(customerNumber)
                .sequenceCode(options.sequenceCode())
                .subsystem(options.subsystem())
                .includeCapability(options.includeCapability())
                .includeBlackList(options.includeBlackList())
                .includeBaseInfo(options.includeBaseInfo())
                .includeGrayList(options.includeGrayList())
                .build();

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getDefaultTimeout());

        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!(raw instanceof CustomerInfoKafkaResponse response)) {
            return Result.failure(Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, "loadCustomerInfo"));
        }
        return KafkaValidationMapper.mapToPartyInfoResponse(response, role, guaranteePercentage);
    }

    @Override
    public Result<List<PartyInfoResponse>> findRelatedCustomers(List<String> customerNumbers) {
        return sendAndMap(
                FindRelatedCustomersRequest.builder()
                        .customerNumbers(customerNumbers)
                        .build(),
                CustomerListKafkaResponse.class,
                KafkaValidationMapper::mapToPartyInfoResponseList);
    }

    @Override
    public Result<PartyBirthInfo> loadCustomerBirthInfo(String customerNumber) {
        return sendAndMap(
                LoadCustomerBirthInfoRequest.builder()
                        .customerNumber(customerNumber)
                        .build(),
                CustomerBirthInfoKafkaResponse.class,
                KafkaValidationMapper::mapToPartyBirthInfo);
    }

    @Override
    public Result<DepositInfo> getDepositInfo(DepositNumber depositNumber) {
        return sendAndMap(
                LoadDepositInfoRequest.builder()
                        .depositNumber(depositNumber.value())
                        .build(),
                DepositInfoKafkaResponse.class,
                KafkaValidationMapper::mapToDepositInfo);
    }

    @Override
    public Result<DepositClosedStatus> isDepositClosed(DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                IsDepositClosedRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencySwiftCode(currencyType.getCode())
                        .build(),
                DepositClosedKafkaResponse.class,
                KafkaValidationMapper::mapToDepositClosedStatus);
    }

    @Override
    public Result<DebtorDepositValidation> validateDebtorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                ValidateDebtorDepositRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencySwiftCode(currencyType.getCode())
                        .build(),
                ValidationResultKafkaResponse.class,
                KafkaValidationMapper::mapToDebtorDepositValidation);
    }

    @Override
    public Result<CreditorDepositValidation> validateCreditorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount) {
        return sendAndMap(
                ValidateCreditorDepositRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencySwiftCode(currencyType.getCode())
                        .amount(amount)
                        .build(),
                ValidationResultKafkaResponse.class,
                KafkaValidationMapper::mapToCreditorDepositValidation);
    }

    @Override
    public Result<CurrencyValidation> hasDepositAllowedCurrencies(
            DepositNumber depositNumber, List<CurrencyType> currencyTypes) {
        List<String> currencies =
                currencyTypes.stream().map(CurrencyType::getCode).collect(Collectors.toList());
        return sendAndMap(
                HasDepositAllowedCurrenciesRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencies(currencies)
                        .build(),
                CurrencyValidationKafkaResponse.class,
                KafkaValidationMapper::mapToCurrencyValidation);
    }

    @Override
    public Result<List<PartyInfoResponse>> getAllDepositSignerOwnerCustomer(String depositNumber) {
        return sendAndMap(
                GetDepositSignerOwnerRequest.builder()
                        .depositNumber(depositNumber)
                        .build(),
                CustomerListKafkaResponse.class,
                KafkaValidationMapper::mapToPartyInfoResponseList);
    }

    @Override
    public Result<CollateralValidation> validateAddAssuranceToFile(
            List<CollateralSerial> collateralSerials, List<Long> usedCosts, BranchCode branchCode) {
        List<String> serials =
                collateralSerials.stream().map(CollateralSerial::value).collect(Collectors.toList());
        List<String> branchCodes = Collections.nCopies(collateralSerials.size(), branchCode.value());
        return sendAndMap(
                ValidateAssuranceRequest.builder()
                        .assuranceSerials(serials)
                        .usedCosts(usedCosts)
                        .branchCodes(branchCodes)
                        .build(),
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
                ReserveCollateralRequest.builder()
                        .assuranceSerial(collateralSerial.value())
                        .fileNumber(applicationNumber.formattedApplicationNumber())
                        .transactionId(UUID.randomUUID().toString())
                        .reserveDurationMin(reserveDurationMin)
                        .amount(usedAmount.value())
                        .build(),
                CollateralSerialsKafkaResponse.class,
                KafkaValidationMapper::mapToCollateralSerials);
    }

    @Override
    public Result<CollateralDetails> loadCollateral(String assuranceSerial, String uniqueTrackingCode) {
        return sendAndMap(
                LoadCollateralRequest.builder()
                        .assuranceSerial(assuranceSerial)
                        .uniqueTrackingCode(uniqueTrackingCode)
                        .build(),
                CollateralDetailsKafkaResponse.class,
                KafkaValidationMapper::mapToCollateralDetails);
    }

    @Override
    public Result<CollateralSerial> unReserveCollateral(
            CollateralSerial collateralSerial,
            ApplicationNumber applicationNumber,
            UUID transactionId,
            UUID rollBackId) {

        var request = UnReserveCollateralRequest.builder()
                .assuranceSerial(collateralSerial.value())
                .fileNumber(applicationNumber.formattedApplicationNumber())
                .transactionId(transactionId.toString())
                .rollBackId(rollBackId.toString())
                .build();

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getDefaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }
        return Result.success(collateralSerial);
    }

    @Override
    public Result<SanctionDetails> fetchBySanctionSerial(String sanctionSerial) {
        return sendAndMap(
                FetchSanctionDetailsRequest.builder()
                        .sanctionSerial(sanctionSerial)
                        .build(),
                SanctionDetailsKafkaResponse.class,
                KafkaValidationMapper::mapToSanctionDetails);
    }

    @Override
    public Result<Void> validateSamat(Samat samat, String loanTypeCode, String economicalSectionCode) {
        ValidateSamatRequest samatRequest = ValidateSamatRequest.builder()
                .consumptionPlaceCode(samat.consumptionPlaceCode())
                .exceptionCode(samat.exceptionCode())
                .loanTypeCode(loanTypeCode)
                .economicalSectionCode(economicalSectionCode)
                .trackingNumber(samat.trackingNumber())
                .isicEconomicSector(samat.isicEconomicSector())
                .subIsicEconomicSector(samat.subIsicEconomicSector())
                .useType(samat.useType())
                .build();

        return sendAndMap(
                samatRequest, ValidateSamatKafkaResponse.class, KafkaValidationMapper::mapSamatViolationToNotification);
    }

    private <R extends FcbKafkaBaseResponse, T> Result<T> sendAndMap(
            FcbKafkaBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbKafkaBaseResponse> result = kafkaClient.sendAndReceive(request, properties.getDefaultTimeout());
        if (result.isFailure()) {
            return Result.failure(result.notification());
        }

        FcbKafkaBaseResponse raw = result.orElseThrow();
        if (!responseType.isInstance(raw)) {
            return Result.failure(
                    Notification.ofError(CoreBankingErrors.KAFKA_INVALID_RESPONSE, request.getOperationName()));
        }

        return responseMapper.apply(responseType.cast(raw));
    }
}
