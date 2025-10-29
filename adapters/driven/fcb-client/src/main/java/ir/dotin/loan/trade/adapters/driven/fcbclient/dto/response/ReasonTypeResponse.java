package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.ReasonTypeDTO")
public class ReasonTypeResponse extends FcbBaseResponse {

    @XStreamAlias("centralBankCode")
    private String centralBankCode;

    @XStreamAlias("description")
    private String description;

    @XStreamAlias("modificationDate")
    private String modificationDate;

    @XStreamAlias("reasonType")
    private String reasonType;

    @XStreamAlias("shouldHasSerial")
    private Boolean shouldHasSerial;

    @XStreamAlias("exemptionOfInquiryNumber")
    private Boolean exemptionOfInquiryNumber;

    @XStreamAlias("applyDate")
    private String applyDate;

    @XStreamAlias("code")
    private String code;

    @XStreamAlias("disabled")
    private Boolean disabled;
}
