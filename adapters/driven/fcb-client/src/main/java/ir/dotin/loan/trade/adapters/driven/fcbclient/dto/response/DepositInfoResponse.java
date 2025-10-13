package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.DepositInfoDTO")
public class DepositInfoResponse extends FcbBaseResponse {

    @XStreamAlias("number")
    private String number;

    @XStreamAlias("title")
    private String title;

    @XStreamAlias("type")
    private String type;

    @XStreamAlias("mainAmount")
    private BigDecimal mainAmount;

    @XStreamAlias("blockedAmount")
    private BigDecimal blockedAmount;

    @XStreamAlias("bankClaimsAmount")
    private BigDecimal bankClaimsAmount;

    @XStreamAlias("interestBlockedAmount")
    private BigDecimal interestBlockedAmount;

    @XStreamAlias("withDrawableAmount")
    private BigDecimal withDrawableAmount;

    @XStreamAlias("currency")
    private CurrencyDTO currency;

    @XStreamAlias("status")
    private CategoryElementDTO status;

    @XStreamAlias("openingDate")
    private String openingDate;

    @XStreamAlias("expirationDate")
    private String expirationDate;

    @XStreamAlias("openingTime")
    private String openingTime;

    @XStreamAlias("externalBank")
    private BankDTO externalBank;

    @XStreamAlias("isExternalDeposit")
    private Boolean isExternalDeposit;

    @XStreamAlias("branchCode")
    private String branchCode;

    @XStreamAlias("iban")
    private String iban;

    @XStreamAlias("depositTypeTitle")
    private String depositTypeTitle;

    @XStreamAlias("isYarPayment")
    private Boolean isYarPayment;

    @XStreamAlias("ownerNationalCodes")
    private List<String> ownerNationalCodes;

    // Nested DTOs for complex objects
    @Data
    public static class CurrencyDTO {
        @XStreamAlias("id")
        private Long id;

        @XStreamAlias("code")
        private String code;

        @XStreamAlias("name")
        private String name;

        @XStreamAlias("symbol")
        private String symbol;
    }

    @Data
    public static class CategoryElementDTO {
        @XStreamAlias("id")
        private Long id;

        @XStreamAlias("code")
        private String code;

        @XStreamAlias("description")
        private String description;
    }

    @Data
    public static class BankDTO {
        @XStreamAlias("id")
        private Long id;

        @XStreamAlias("code")
        private String code;

        @XStreamAlias("name")
        private String name;
    }
}
