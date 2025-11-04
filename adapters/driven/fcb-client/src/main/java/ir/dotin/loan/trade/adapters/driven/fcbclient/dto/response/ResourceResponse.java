package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.ResourceDTO")
public class ResourceResponse extends FcbBaseResponse {

    @XStreamAlias("applyDate")
    private String applyDate;

    @XStreamAlias("code")
    private String code;

    @XStreamAlias("disabled")
    private Boolean disabled;

    @XStreamAlias("centralBankCode")
    private String centralBankCode;

    @XStreamAlias("hasAgreeCustomer")
    private Boolean hasAgreeCustomer;

    @XStreamAlias("hasChild")
    private Boolean hasChild;

    @XStreamAlias("hasParent")
    private Boolean hasParent;

    @XStreamAlias("modificationDate")
    private String modificationDate;

    @XStreamAlias("name")
    private String name;
}
