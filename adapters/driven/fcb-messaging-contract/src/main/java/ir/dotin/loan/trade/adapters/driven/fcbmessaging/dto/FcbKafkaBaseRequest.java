package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.BatchCloseAccountRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.BatchOpenAccountRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.DeleteAccountRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.FetchSanctionDetailsRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.FindOrCreateAccountRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.FindRelatedCustomersRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.GetApplicationNumberRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.GetDepositSignerOwnerRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.HasDepositAllowedCurrenciesRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.IsDepositClosedRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadBranchRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadCollateralRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadCoveredBranchesRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadCustomerBirthInfoRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadCustomerInfoRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadDepositInfoRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadEconomicSectorRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadReasonTypeForCreateRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadReasonTypeForRevokeRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadResourceRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.LoadTopicRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.OpenAccountByTopicRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.OpenAccountRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.PostTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReconStateRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReemitOutboxRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReserveCollateralRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ReverseTransactionRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.UnReserveCollateralRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateAccountNumberRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateAssuranceRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateCreditorDepositRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateDebtorDepositRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateEcoSectorRequest;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request.ValidateSamatRequest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "operationName",
        visible = true)
@JsonSubTypes({
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
    @JsonSubTypes.Type(value = OpenAccountByTopicRequest.class, name = "electronic-bill-create-account"),
    @JsonSubTypes.Type(value = OpenAccountRequest.class, name = "nova-open-account"),
    @JsonSubTypes.Type(value = DeleteAccountRequest.class, name = "nova-delete-account"),
    @JsonSubTypes.Type(value = ValidateAccountNumberRequest.class, name = "load-account-by-account-number-service"),
    @JsonSubTypes.Type(value = FindOrCreateAccountRequest.class, name = "find-or-create-account"),
    @JsonSubTypes.Type(value = PostTransactionRequest.class, name = "issue-general-document"),
    @JsonSubTypes.Type(value = ReverseTransactionRequest.class, name = "cancel-transfer-money-loan"),
    @JsonSubTypes.Type(value = ValidateSamatRequest.class, name = "validate-samat"),
    @JsonSubTypes.Type(value = ReconStateRequest.class, name = "nova-loanfile-recon-state"),
    @JsonSubTypes.Type(value = ReemitOutboxRequest.class, name = "nova-reemit-outbox"),
    @JsonSubTypes.Type(value = BatchOpenAccountRequest.class, name = "nova-batch-open-accounts"),
    @JsonSubTypes.Type(value = BatchCloseAccountRequest.class, name = "nova-batch-close-accounts")
})
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class FcbKafkaBaseRequest {

    // set via constructor / builder / Jackson (the operation discriminator, always present)
    @SuppressWarnings("NullAway.Init")
    private String operationName;

    // stamped by FcbKafkaClient before send / set by Jackson on inbound
    @SuppressWarnings("NullAway.Init")
    private String producerCode;

    // stamped by FcbKafkaClient before send / set by Jackson on inbound
    @SuppressWarnings("NullAway.Init")
    private String eventUid;

    // optional wire field: never populated by Nova, absent on outbound requests
    private String @Nullable [] tags;

    // stamped by FcbKafkaClient before send / set by Jackson on inbound
    @SuppressWarnings("NullAway.Init")
    private Date dateTime;

    private int version;

    protected FcbKafkaBaseRequest(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getProducerCode() {
        return producerCode;
    }

    public void setProducerCode(String producerCode) {
        this.producerCode = producerCode;
    }

    public String getEventUid() {
        return eventUid;
    }

    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String @Nullable [] getTags() {
        return tags;
    }

    public void setTags(String @Nullable [] tags) {
        this.tags = tags;
    }
}
