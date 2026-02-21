package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.*;

import org.jspecify.annotations.Nullable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "operationName",
        visible = true)
@JsonSubTypes({
    // Validation operations
    @JsonSubTypes.Type(value = LoadEconomicSectorRequest.class, name = "load-economicalSection-by-code"),
    @JsonSubTypes.Type(value = ValidateEcoSectorRequest.class, name = "validate-ecoSection-loanType"),
    @JsonSubTypes.Type(value = LoadReasonTypeForCreateRequest.class, name = "load-reason-type-for-create"),
    @JsonSubTypes.Type(value = LoadReasonTypeForRevokeRequest.class, name = "load-reason-type-for-revoke"),
    @JsonSubTypes.Type(value = LoadResourceRequest.class, name = "load-resource-by-code"),
    @JsonSubTypes.Type(value = LoadTopicRequest.class, name = "load-topic-by-code"),
    @JsonSubTypes.Type(value = LoadCoveredBranchesRequest.class, name = "load-covered-branches"),
    @JsonSubTypes.Type(value = GetApplicationNumberRequest.class, name = "get-loan-file-number"),
    @JsonSubTypes.Type(value = LoadBranchRequest.class, name = "load-branch-nova"),
    @JsonSubTypes.Type(value = LoadCustomerInfoRequest.class, name = "load-customer-info"),
    @JsonSubTypes.Type(value = FindRelatedCustomersRequest.class, name = "find-related-customers"),
    @JsonSubTypes.Type(value = LoadCustomerBirthInfoRequest.class, name = "load-customer-birth-info"),
    @JsonSubTypes.Type(value = LoadDepositInfoRequest.class, name = "load-deposit-by-number"),
    @JsonSubTypes.Type(value = IsDepositClosedRequest.class, name = "is-deposit-closed"),
    @JsonSubTypes.Type(value = ValidateDebtorDepositRequest.class, name = "validate-debtor-deposit"),
    @JsonSubTypes.Type(value = ValidateCreditorDepositRequest.class, name = "validate-creditor-deposit"),
    @JsonSubTypes.Type(value = HasDepositAllowedCurrenciesRequest.class, name = "has-deposit-allowed-currencies"),
    @JsonSubTypes.Type(value = GetDepositSignerOwnerRequest.class, name = "get-all-deposit-signer-owner-customer"),
    @JsonSubTypes.Type(value = ValidateAssuranceRequest.class, name = "validate-add-assurance-to-file"),
    @JsonSubTypes.Type(value = ReserveCollateralRequest.class, name = "reserve-assurance-for-file"),
    @JsonSubTypes.Type(value = LoadCollateralRequest.class, name = "load-assurance-service"),
    @JsonSubTypes.Type(value = UnReserveCollateralRequest.class, name = "un-reserve-assurance-for-file"),
    @JsonSubTypes.Type(value = FetchSanctionDetailsRequest.class, name = "fetch-sanction-details"),
    // Account operations
    @JsonSubTypes.Type(value = OpenAccountByTopicRequest.class, name = "electronic-bill-create-account"),
    @JsonSubTypes.Type(value = OpenAccountRequest.class, name = "nova-open-account"),
    @JsonSubTypes.Type(value = DeleteAccountRequest.class, name = "nova-delete-account"),
    @JsonSubTypes.Type(value = ValidateAccountNumberRequest.class, name = "load-account-by-account-number-service"),
    @JsonSubTypes.Type(value = FindOrCreateAccountRequest.class, name = "find-or-create-account"),
    // Transaction operations
    @JsonSubTypes.Type(value = PostTransactionRequest.class, name = "issue-general-document"),
    @JsonSubTypes.Type(value = ReverseTransactionRequest.class, name = "cancel-transfer-money-loan")
})
public abstract class FcbKafkaBaseRequest {

    private final String operationName;
    private final String eventUid;
    private final Instant dateTime;
    private final @Nullable String version;
    private final @Nullable String responseTopic;
    private final @Nullable Map<String, String> tags;

    protected FcbKafkaBaseRequest(String operationName) {
        this.operationName = operationName;
        this.eventUid = UUID.randomUUID().toString();
        this.dateTime = Instant.now();
        this.version = "1.0";
        this.responseTopic = null;
        this.tags = null;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getEventUid() {
        return eventUid;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public @Nullable String getVersion() {
        return version;
    }

    public @Nullable String getResponseTopic() {
        return responseTopic;
    }

    public @Nullable Map<String, String> getTags() {
        return tags;
    }
}
