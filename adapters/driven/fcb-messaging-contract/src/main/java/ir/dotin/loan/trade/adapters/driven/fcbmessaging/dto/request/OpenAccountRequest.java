package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.FcbBaseRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public final class OpenAccountRequest extends FcbBaseRequest {

    @Builder.Default
    @JsonIgnore
    private String operationName = "nova-open-account";

    private final String transactionId;
    private final String topic;
    private final String swiftCode;
    private final String branchCode;
    private final boolean createAccountGroup;
    private final String newAccBranchCode;
    private final String newAccAccountNumber;
    private final String newAccTitle;
    private final BigDecimal newAccAmount;
    private final BigDecimal newAccMinAmount;
    private final BigDecimal newAccMaxAmount;
    private final BigDecimal newAccBaseCurrencyAmount;
    private final BigDecimal newAccDebtorAmount;
    private final BigDecimal newAccCreditorAmount;
}
