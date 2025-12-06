package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.AssuranceDTO")
public class AssuranceResponse extends FcbBaseResponse {

    @XStreamAlias("active")
    private Boolean active;

    @XStreamAlias("bankShare")
    private BigDecimal bankShare;

    @XStreamAlias("customerNo")
    private String customerNo;

    @XStreamAlias("date")
    private String date;

    @XStreamAlias("deliveryLocation")
    private String deliveryLocation;

    @XStreamAlias("guaranteeAmount")
    private BigDecimal guaranteeAmount;

    @XStreamAlias("guaranteeBranch")
    private String guaranteeBranch;

    @XStreamAlias("guaranteeBranchCode")
    private String guaranteeBranchCode;

    @XStreamAlias("guaranteeDuration")
    private Integer guaranteeDuration;

    @XStreamAlias("guaranteeIssuer")
    private String guaranteeIssuer;

    @XStreamAlias("guaranteeNumber")
    private String guaranteeNumber;

    @XStreamAlias("guaranteeStartDate")
    private String guaranteeStartDate;

    @XStreamAlias("isEscrowed")
    private Boolean isEscrowed;

    @XStreamAlias("isReleaseAllowed")
    private Boolean isReleaseAllowed;

    @XStreamAlias("isSpecial")
    private Boolean isSpecial;

    @XStreamAlias("loanFileNumber")
    private String loanFileNumber;

    @XStreamAlias("mortgagePrice")
    private BigDecimal mortgagePrice;

    @XStreamAlias("price")
    private BigDecimal price;

    @XStreamAlias("serial")
    private String serial;

    @XStreamAlias("usedMortgagePrice")
    private BigDecimal usedMortgagePrice;

    @XStreamAlias("branchCode")
    private String branchCode;

    @XStreamAlias("currency")
    private String currency;

    @XStreamAlias("address")
    private String address;

    @XStreamAlias("assuranceType")
    private AssuranceTypeDTO assuranceType;

    @XStreamAlias("attachedCheques")
    private List<ChequeInformationVO> attachedCheques;

    @XStreamAlias("promissoryNoteInfos")
    private List<PromissoryNoteInfoVO> promissoryNoteInfos;

    @XStreamAlias("attachedDeposits")
    private List<AttachedDepositDTO> attachedDeposits;

    // Nested DTOs
    @Data
    public static class AssuranceTypeDTO {
        @XStreamAlias("code")
        private String code;

        @XStreamAlias("name")
        private String name;
    }

    @Data
    public static class ChequeInformationVO {
        @XStreamAlias("chequeNumber")
        private String chequeNumber;

        @XStreamAlias("amount")
        private BigDecimal amount;

        @XStreamAlias("bankName")
        private String bankName;

        @XStreamAlias("accountNumber")
        private String accountNumber;
    }

    @Data
    public static class PromissoryNoteInfoVO {
        @XStreamAlias("noteNumber")
        private String noteNumber;

        @XStreamAlias("amount")
        private BigDecimal amount;

        @XStreamAlias("issueDate")
        private String issueDate;
    }

    @Data
    public static class AttachedDepositDTO {
        @XStreamAlias("depositNumber")
        private String depositNumber;

        @XStreamAlias("amount")
        private BigDecimal amount;

        @XStreamAlias("depositType")
        private String depositType;
    }
}
