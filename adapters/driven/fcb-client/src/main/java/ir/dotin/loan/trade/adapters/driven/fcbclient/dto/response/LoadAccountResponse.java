package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.gl.valueobjects.LoadAccountByAccountNumberResponseVO")
public class LoadAccountResponse extends FcbBaseResponse {

    @XStreamAlias("accountNumber")
    private String accountNumber;

    @XStreamAlias("id")
    private Long id;

    @XStreamAlias("amount")
    private BigDecimal amount;

    @XStreamAlias("title")
    private String title;

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

    @XStreamAlias("statusName")
    private String statusName;

    @XStreamAlias("statusCode")
    private String statusCode;

    @XStreamAlias("topicVO")
    private ServiceTopicVO topicVO;

    @XStreamAlias("branchVO")
    private ServiceBranchVO branchVO;

    @XStreamAlias("currencyVO")
    private ServiceCurrencyVO currencyVO;

    @Data
    public static class ServiceTopicVO {
        @XStreamAlias("code")
        private String code;

        @XStreamAlias("title")
        private String title;

        @XStreamAlias("isDebtor")
        private Boolean isDebtor;
    }

    @Data
    public static class ServiceBranchVO {
        @XStreamAlias("code")
        private String code;

        @XStreamAlias("name")
        private String name;
    }

    @Data
    public static class ServiceCurrencyVO {
        @XStreamAlias("id")
        private Long id;

        @XStreamAlias("code")
        private String code;

        @XStreamAlias("name")
        private String name;

        @XStreamAlias("swiftCode")
        private String swiftCode;
    }
}
