package ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
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
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.*;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.mapper.FcbValidationMapper;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.error.CoreBankingErrors;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.*;
import ir.dotin.loan.trade.core.application.ports.outbound.client.samat.ValidateSamatPort;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcbValidationAdapter
        implements LoanServicePort,
                CustomerServicePort,
                DepositServicePort,
                CollateralServicePort,
                CollateralReadPort,
                FetchSanctionDetailsPort,
                ValidateSamatPort {

    private final FcbRequestReplyClient requestReplyClient;

    /**
     * Per-call request/reply timeout, bound from Consul KV {@code nova.fcb.kafka.default-timeout} (defaults to 10s).
     */
    @Value("${nova.fcb.kafka.default-timeout:10s}")
    private Duration defaultTimeout;

    @Override
    @Cacheable(value = "fcb.economical-sector-by-code", unless = "#result.isFailure()")
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadEconomicalSectorByCode"})
    public Result<EconomicSector> loadEconomicalSectorByCode(EconomicSector economicSector) {
        return sendAndMap(
                LoadEconomicSectorRequest.builder()
                        .economicalSectionCode(economicSector.code())
                        .build(),
                EconomicSectorResponse.class,
                FcbValidationMapper::mapToEconomicSector);
    }

    @Override
    @Cacheable(value = "fcb.economical-sector", unless = "#result.isFailure()")
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadEconomicalSector"})
    public Result<EconomicalSectorResponse> loadEconomicalSector(EconomicSector economicSector) {
        return sendAndMap(
                LoadEconomicSectorRequest.builder()
                        .economicalSectionCode(economicSector.code())
                        .build(),
                EconomicSectorResponse.class,
                FcbValidationMapper::mapToEconomicalSectorResponse);
    }

    @Override
    @Cacheable(value = "fcb.sector-for-loan-type", unless = "#result.isFailure()")
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "validateEconomicalSectorForLoanType"})
    public Result<EconomicalSectorValidation> validateEconomicalSectorForLoanType(
            EconomicSector economicSector, LoanTypeCode loanTypeCode) {
        return sendAndMap(
                ValidateEcoSectorRequest.builder()
                        .economicalSectionCode(economicSector.code())
                        .loanTypeCode(loanTypeCode.value())
                        .build(),
                EcoSectorValidationResponse.class,
                FcbValidationMapper::mapToEcoSectorValidation);
    }

    @Override
    @Cacheable(value = "fcb.reason-type", unless = "#result.isFailure()")
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadReasonTypeForCreate"})
    public Result<ReasonType> loadReasonTypeForCreate(String reasonTypeCode) {
        return sendAndMap(
                LoadReasonTypeForCreateRequest.builder()
                        .reasonTypeCode(reasonTypeCode)
                        .build(),
                ReasonTypeResponse.class,
                FcbValidationMapper::mapToReasonType);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadReasonTypeForRevoke"})
    public Result<ReasonType> loadReasonTypeForRevoke(String reasonTypeCode) {
        return sendAndMap(
                LoadReasonTypeForRevokeRequest.builder()
                        .reasonTypeCode(reasonTypeCode)
                        .build(),
                ReasonTypeResponse.class,
                FcbValidationMapper::mapToReasonType);
    }

    @Override
    @Cacheable(value = "fcb.resource", unless = "#result.isFailure()")
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadResourceByCode"})
    public Result<SubSource> loadResourceByCode(String subSourceCode) {
        return sendAndMap(
                LoadResourceRequest.builder().resourceCode(subSourceCode).build(),
                ResourceResponse.class,
                FcbValidationMapper::mapToSubSource);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadTopicByCode"})
    public Result<List<TopicInfo>> loadTopicByCode(List<String> topicCodes) {
        return sendAndMap(
                LoadTopicRequest.builder().topicCodes(topicCodes).build(),
                TopicInfoListResponse.class,
                FcbValidationMapper::mapToTopicInfoList);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadCoveredBranches"})
    public Result<List<BranchCode>> loadCoveredBranches(BranchCode branchCode) {
        return sendAndMap(
                LoadCoveredBranchesRequest.builder()
                        .branchCode(branchCode.value())
                        .build(),
                BranchCodeListResponse.class,
                FcbValidationMapper::mapToBranchCodeList);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "getApplicationNumber"})
    public Result<ApplicationNumber> getApplicationNumber(Branch branch, LoanTypeCode loanTypeCode, Party party) {
        var request = GetApplicationNumberRequest.builder()
                .branchCode(branch.code().value())
                .loanTypeCode(loanTypeCode.value())
                .customerNumber(party.customerNumber())
                .build();

        Result<FcbBaseResponse> result = requestReplyClient.sendAndReceive(request, defaultTimeout);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        FcbBaseResponse raw = result.unwrap();
        if (!(raw instanceof ApplicationNumberResponse response)) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, "getApplicationNumber");
        }
        return FcbValidationMapper.mapToApplicationNumber(response, branch, loanTypeCode, party);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadBranch"})
    public Result<BranchDetails> loadBranch(BranchCode branchCode) {
        return sendAndMap(
                LoadBranchRequest.builder().branchCode(branchCode.value()).build(),
                BranchDetailsResponse.class,
                FcbValidationMapper::mapToBranchDetails);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadCustomerInfo"})
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

        Result<FcbBaseResponse> result = requestReplyClient.sendAndReceive(request, defaultTimeout);

        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        FcbBaseResponse raw = result.unwrap();
        if (!(raw instanceof CustomerInfoResponse response)) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, "loadCustomerInfo");
        }
        return FcbValidationMapper.mapToPartyInfoResponse(response, role, guaranteePercentage);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "findRelatedCustomers"})
    public Result<List<PartyInfoResponse>> findRelatedCustomers(List<String> customerNumbers) {
        return sendAndMap(
                FindRelatedCustomersRequest.builder()
                        .customerNumbers(customerNumbers)
                        .build(),
                CustomerListResponse.class,
                FcbValidationMapper::mapToPartyInfoResponseList);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadCustomerBirthInfo"})
    public Result<PartyBirthInfo> loadCustomerBirthInfo(String customerNumber) {
        return sendAndMap(
                LoadCustomerBirthInfoRequest.builder()
                        .customerNumber(customerNumber)
                        .build(),
                CustomerBirthInfoResponse.class,
                FcbValidationMapper::mapToPartyBirthInfo);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "getDepositInfo"})
    public Result<DepositInfo> getDepositInfo(DepositNumber depositNumber) {
        return sendAndMap(
                LoadDepositInfoRequest.builder()
                        .depositNumber(depositNumber.value())
                        .build(),
                DepositInfoResponse.class,
                FcbValidationMapper::mapToDepositInfo);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "isDepositClosed"})
    public Result<DepositClosedStatus> isDepositClosed(DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                IsDepositClosedRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencySwiftCode(currencyType.getCode())
                        .build(),
                DepositClosedResponse.class,
                FcbValidationMapper::mapToDepositClosedStatus);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "validateDebtorDeposit"})
    public Result<DebtorDepositValidation> validateDebtorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType) {
        return sendAndMap(
                ValidateDebtorDepositRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencySwiftCode(currencyType.getCode())
                        .build(),
                ValidationResultResponse.class,
                FcbValidationMapper::mapToDebtorDepositValidation);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "validateCreditorDeposit"})
    public Result<CreditorDepositValidation> validateCreditorDeposit(
            DepositNumber depositNumber, CurrencyType currencyType, BigDecimal amount) {
        return sendAndMap(
                ValidateCreditorDepositRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencySwiftCode(currencyType.getCode())
                        .amount(amount)
                        .build(),
                ValidationResultResponse.class,
                FcbValidationMapper::mapToCreditorDepositValidation);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "hasDepositAllowedCurrencies"})
    public Result<CurrencyValidation> hasDepositAllowedCurrencies(
            DepositNumber depositNumber, List<CurrencyType> currencyTypes) {
        List<String> currencies =
                currencyTypes.stream().map(CurrencyType::getCode).collect(Collectors.toList());
        return sendAndMap(
                HasDepositAllowedCurrenciesRequest.builder()
                        .depositNumber(depositNumber.value())
                        .currencies(currencies)
                        .build(),
                CurrencyValidationResponse.class,
                FcbValidationMapper::mapToCurrencyValidation);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "getAllDepositSignerOwnerCustomer"})
    public Result<List<PartyInfoResponse>> getAllDepositSignerOwnerCustomer(String depositNumber) {
        return sendAndMap(
                GetDepositSignerOwnerRequest.builder()
                        .depositNumber(depositNumber)
                        .build(),
                CustomerListResponse.class,
                FcbValidationMapper::mapToPartyInfoResponseList);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "validateAddAssuranceToFile"})
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
                CollateralValidationResponse.class,
                FcbValidationMapper::mapToCollateralValidation);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "reserveCollateral"})
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
                CollateralSerialsResponse.class,
                FcbValidationMapper::mapToCollateralSerials);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "loadCollateral"})
    public Result<CollateralDetails> loadCollateral(String assuranceSerial, String uniqueTrackingCode) {
        return sendAndMap(
                LoadCollateralRequest.builder()
                        .assuranceSerial(assuranceSerial)
                        .uniqueTrackingCode(uniqueTrackingCode)
                        .build(),
                CollateralDetailsResponse.class,
                FcbValidationMapper::mapToCollateralDetails);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "unReserveCollateral"})
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

        Result<FcbBaseResponse> result = requestReplyClient.sendAndReceive(request, defaultTimeout);
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }
        return Result.success(collateralSerial);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "fetchBySanctionSerial"})
    public Result<SanctionDetails> fetchBySanctionSerial(String sanctionSerial) {
        return sendAndMap(
                FetchSanctionDetailsRequest.builder()
                        .sanctionSerial(sanctionSerial)
                        .build(),
                SanctionDetailsResponse.class,
                FcbValidationMapper::mapToSanctionDetails);
    }

    @Override
    @Timed(
            value = "fcb.outbound",
            extraTags = {"op", "validateSamat"})
    public Result<Unit> validateSamat(Samat samat, String loanTypeCode, String economicalSectionCode) {
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
                samatRequest, ValidateSamatResponse.class, FcbValidationMapper::mapSamatViolationToNotification);
    }

    private <R extends FcbBaseResponse, T> Result<T> sendAndMap(
            FcbBaseRequest request, Class<R> responseType, Function<R, Result<T>> responseMapper) {

        Result<FcbBaseResponse> result = requestReplyClient.sendAndReceive(request, defaultTimeout);
        if (result.isFailure()) {
            return Result.failure(result.err().orElseThrow());
        }

        FcbBaseResponse raw = result.unwrap();
        if (!responseType.isInstance(raw)) {
            return Result.failure(CoreBankingErrors.FCB_INVALID_RESPONSE, request.getOperationName());
        }

        return responseMapper.apply(responseType.cast(raw));
    }
}
