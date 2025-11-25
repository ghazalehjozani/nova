package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.SharedAccountResult")
public class AccountInfoResponse extends FcbBaseResponse {

    @XStreamAlias("branchCode")
    private String branchCode;

    @XStreamAlias("accountNumber")
    private String accountNumber;

    @XStreamAlias("amount")
    private BigDecimal amount;

    @XStreamAlias("title")
    private String title;

    @XStreamAlias("topicCode")
    private String topicCode;

    @XStreamAlias("currencySwiftCode")
    private String currencySwiftCode;

    @XStreamAlias("minAmount")
    private BigDecimal minAmount;

    @XStreamAlias("maxAmount")
    private BigDecimal maxAmount;

    @XStreamAlias("baseCurrencyAmount")
    private BigDecimal baseCurrencyAmount;

    @XStreamAlias("debtorAmount")
    private BigDecimal debtorAmount;

    @XStreamAlias("creditorAmount")
    private BigDecimal creditorAmount;

    @XStreamAlias("openingDate")
    private String openingDate;

    @XStreamAlias("lastBuildDateTime")
    private String lastBuildDateTime;

    @XStreamAlias("lastSettleDateTime")
    private String lastSettleDateTime;

    @XStreamAlias("status")
    private String status;
}
