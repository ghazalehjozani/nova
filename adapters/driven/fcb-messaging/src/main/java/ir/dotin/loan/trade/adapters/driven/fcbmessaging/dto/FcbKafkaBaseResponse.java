package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ir.dotin.platform.messaging.api.version.ContractVersion;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.AccountInfoKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ApplicationNumberKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BranchCodeListKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.BranchDetailsKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CollateralDetailsKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CollateralSerialsKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CollateralValidationKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CurrencyValidationKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CustomerBirthInfoKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CustomerInfoKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.CustomerListKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.DepositClosedKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.DepositInfoKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.EcoSectorValidationKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.EconomicSectorKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.HeartbeatKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReasonTypeKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ResourceKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.SanctionDetailsKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.SimpleSuccessKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.TopicInfoListKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.TransactionResultKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ValidateSamatKafkaResponse;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ValidationResultKafkaResponse;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "operationName",
        visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = EconomicSectorKafkaResponse.class, name = "load-economicalSection-by-code"),
    @JsonSubTypes.Type(value = EcoSectorValidationKafkaResponse.class, name = "validate-ecoSection-loanType"),
    @JsonSubTypes.Type(value = ReasonTypeKafkaResponse.class, name = "load-reason-type-for-create"),
    @JsonSubTypes.Type(value = ReasonTypeKafkaResponse.class, name = "load-reason-type-for-revoke"),
    @JsonSubTypes.Type(value = ResourceKafkaResponse.class, name = "load-resource-by-code"),
    @JsonSubTypes.Type(value = TopicInfoListKafkaResponse.class, name = "load-topic-by-code"),
    @JsonSubTypes.Type(value = BranchCodeListKafkaResponse.class, name = "load-covered-branches"),
    @JsonSubTypes.Type(value = ApplicationNumberKafkaResponse.class, name = "get-loan-file-number"),
    @JsonSubTypes.Type(value = BranchDetailsKafkaResponse.class, name = "load-branch-nova"),
    @JsonSubTypes.Type(value = CustomerInfoKafkaResponse.class, name = "load-customer-info"),
    @JsonSubTypes.Type(value = CustomerListKafkaResponse.class, name = "find-related-customers"),
    @JsonSubTypes.Type(value = CustomerBirthInfoKafkaResponse.class, name = "load-customer-birth-info"),
    @JsonSubTypes.Type(value = DepositInfoKafkaResponse.class, name = "load-deposit-by-number"),
    @JsonSubTypes.Type(value = DepositClosedKafkaResponse.class, name = "is-deposit-closed"),
    @JsonSubTypes.Type(value = ValidationResultKafkaResponse.class, name = "validate-debtor-deposit"),
    @JsonSubTypes.Type(value = ValidationResultKafkaResponse.class, name = "validate-creditor-deposit"),
    @JsonSubTypes.Type(value = CurrencyValidationKafkaResponse.class, name = "has-deposit-allowed-currencies"),
    @JsonSubTypes.Type(value = CustomerListKafkaResponse.class, name = "get-all-deposit-signer-owner-customer"),
    @JsonSubTypes.Type(value = CollateralValidationKafkaResponse.class, name = "validate-add-assurance-to-file"),
    @JsonSubTypes.Type(value = CollateralSerialsKafkaResponse.class, name = "reserve-assurance-for-file"),
    @JsonSubTypes.Type(value = CollateralDetailsKafkaResponse.class, name = "load-assurance-service"),
    @JsonSubTypes.Type(value = SimpleSuccessKafkaResponse.class, name = "un-reserve-assurance-for-file"),
    @JsonSubTypes.Type(value = SanctionDetailsKafkaResponse.class, name = "fetch-sanction-details"),
    @JsonSubTypes.Type(value = AccountInfoKafkaResponse.class, name = "electronic-bill-create-account"),
    @JsonSubTypes.Type(value = AccountInfoKafkaResponse.class, name = "nova-open-account"),
    @JsonSubTypes.Type(value = AccountInfoKafkaResponse.class, name = "nova-delete-account"),
    @JsonSubTypes.Type(value = AccountInfoKafkaResponse.class, name = "load-account-by-account-number-service"),
    @JsonSubTypes.Type(value = AccountInfoKafkaResponse.class, name = "find-or-create-account"),
    @JsonSubTypes.Type(value = ValidateSamatKafkaResponse.class, name = "validate-samat"),
    @JsonSubTypes.Type(value = TransactionResultKafkaResponse.class, name = "issue-general-document"),
    @JsonSubTypes.Type(value = SimpleSuccessKafkaResponse.class, name = "cancel-transfer-money-loan"),
    @JsonSubTypes.Type(value = HeartbeatKafkaResponse.class, name = "heartbeat")
})
@ContractVersion(1)
public abstract class FcbKafkaBaseResponse {

    private String correlationId;
    private boolean success;
    private String errorCode;
    private String errorMessage;
    private String operationName;

    protected FcbKafkaBaseResponse() {}

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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
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
