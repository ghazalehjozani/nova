package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.AccountInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ApplicationNumberResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BatchCloseAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BatchOpenAccountResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BranchCodeListResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BranchDetailsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CollateralDetailsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CollateralSerialsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CollateralValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CurrencyValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CustomerBirthInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CustomerInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CustomerListResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.DepositClosedResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.DepositInfoResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.EcoSectorValidationResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.EconomicSectorResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReasonTypeResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconStateResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReemitOutboxResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ResourceResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.SanctionDetailsResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.SimpleSuccessResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.TopicInfoListResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.TransactionResultResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ValidateSamatResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ValidationResultResponse;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "operationName",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = EconomicSectorResponse.class, name = "load-economicalSection-by-code"),
    @JsonSubTypes.Type(value = EcoSectorValidationResponse.class, name = "validate-ecoSection-loanType"),
    @JsonSubTypes.Type(value = ReasonTypeResponse.class, name = "load-reason-type-for-create"),
    @JsonSubTypes.Type(value = ReasonTypeResponse.class, name = "load-reason-type-for-revoke"),
    @JsonSubTypes.Type(value = ResourceResponse.class, name = "load-resource-by-code"),
    @JsonSubTypes.Type(value = TopicInfoListResponse.class, name = "load-topic-by-code"),
    @JsonSubTypes.Type(value = BranchCodeListResponse.class, name = "load-covered-branches"),
    @JsonSubTypes.Type(value = ApplicationNumberResponse.class, name = "get-loan-file-number"),
    @JsonSubTypes.Type(value = BranchDetailsResponse.class, name = "load-branch-nova"),
    @JsonSubTypes.Type(value = CustomerInfoResponse.class, name = "load-customer-info"),
    @JsonSubTypes.Type(value = CustomerListResponse.class, name = "find-related-customers"),
    @JsonSubTypes.Type(value = CustomerBirthInfoResponse.class, name = "load-customer-birth-info"),
    @JsonSubTypes.Type(value = DepositInfoResponse.class, name = "load-deposit-by-number"),
    @JsonSubTypes.Type(value = DepositClosedResponse.class, name = "is-deposit-closed"),
    @JsonSubTypes.Type(value = ValidationResultResponse.class, name = "validate-debtor-deposit"),
    @JsonSubTypes.Type(value = ValidationResultResponse.class, name = "validate-creditor-deposit"),
    @JsonSubTypes.Type(value = CurrencyValidationResponse.class, name = "has-deposit-allowed-currencies"),
    @JsonSubTypes.Type(value = CustomerListResponse.class, name = "get-all-deposit-signer-owner-customer"),
    @JsonSubTypes.Type(value = CollateralValidationResponse.class, name = "validate-add-assurance-to-file"),
    @JsonSubTypes.Type(value = CollateralSerialsResponse.class, name = "reserve-assurance-for-file"),
    @JsonSubTypes.Type(value = CollateralDetailsResponse.class, name = "load-assurance-service"),
    @JsonSubTypes.Type(value = SimpleSuccessResponse.class, name = "un-reserve-assurance-for-file"),
    @JsonSubTypes.Type(value = SanctionDetailsResponse.class, name = "fetch-sanction-details"),
    @JsonSubTypes.Type(value = AccountInfoResponse.class, name = "electronic-bill-create-account"),
    @JsonSubTypes.Type(value = AccountInfoResponse.class, name = "nova-open-account"),
    @JsonSubTypes.Type(value = AccountInfoResponse.class, name = "nova-delete-account"),
    @JsonSubTypes.Type(value = AccountInfoResponse.class, name = "load-account-by-account-number-service"),
    @JsonSubTypes.Type(value = AccountInfoResponse.class, name = "find-or-create-account"),
    @JsonSubTypes.Type(value = ValidateSamatResponse.class, name = "validate-samat"),
    @JsonSubTypes.Type(value = ReconStateResponse.class, name = "nova-loanfile-recon-state"),
    @JsonSubTypes.Type(value = ReemitOutboxResponse.class, name = "nova-reemit-outbox"),
    @JsonSubTypes.Type(value = TransactionResultResponse.class, name = "issue-general-document"),
    @JsonSubTypes.Type(value = SimpleSuccessResponse.class, name = "cancel-transfer-money-loan"),
    @JsonSubTypes.Type(value = BatchOpenAccountResponse.class, name = "nova-batch-open-accounts"),
    @JsonSubTypes.Type(value = BatchCloseAccountResponse.class, name = "nova-batch-close-accounts")
})
public abstract class FcbBaseResponse {

    // set by Jackson deserialization (present on every reply)
    @SuppressWarnings("NullAway.Init")
    private String correlationId;

    private boolean success;

    // optional: only populated by FCB when success=false
    private @Nullable String errorCode;

    // optional: only populated by FCB when success=false
    private @Nullable String errorMessage;

    // set by Jackson deserialization (echoed from the request, present on every reply)
    @SuppressWarnings("NullAway.Init")
    private String operationName;

    protected FcbBaseResponse() {}

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public @Nullable String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(@Nullable String errorCode) {
        this.errorCode = errorCode;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public boolean isError() {
        return !success;
    }
}
