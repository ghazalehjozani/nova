package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.BranchDTO")
public class BranchResponse extends FcbBaseResponse {

    @XStreamAlias("assistantName")
    private String assistantName;

    @XStreamAlias("code")
    private String code;

    @XStreamAlias("foreignName")
    private String foreignName;

    @XStreamAlias("globalCode")
    private Long globalCode;

    @XStreamAlias("managerName")
    private String managerName;

    @XStreamAlias("name")
    private String name;

    @XStreamAlias("samCode")
    private String samCode;

    @XStreamAlias("swiftCode")
    private String swiftCode;

    @XStreamAlias("clearBranch")
    private String clearBranch;

    @XStreamAlias("city")
    private String city;

    @XStreamAlias("bankCode")
    private String bankCode;

    @XStreamAlias("lastModifyDateTime")
    private String lastModifyDateTime;

    @XStreamAlias("branchActivation")
    private Boolean branchActivation;

    @XStreamAlias("closingDate")
    private String closingDate;

    @XStreamAlias("englishName")
    private String englishName;

    @XStreamAlias("parentBranch")
    private String parentBranch;

    @XStreamAlias("baseCurrency")
    private String baseCurrency;

    @XStreamAlias("branchOwnershipType")
    private String branchOwnershipType;

    @XStreamAlias("branchRankName")
    private String branchRankName;

    @XStreamAlias("branchRankId")
    private Long branchRankId;

    @XStreamAlias("type")
    private String type;
}
