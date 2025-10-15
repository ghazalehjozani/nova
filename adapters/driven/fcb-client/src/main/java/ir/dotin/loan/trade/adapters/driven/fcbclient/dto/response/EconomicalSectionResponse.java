package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.EconomicalSectionDTO")
public class EconomicalSectionResponse extends FcbBaseResponse {

    @XStreamAlias("code")
    private String code;

    @XStreamAlias("name")
    private String name;
}
