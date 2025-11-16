package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.TopicInfoDTO")
public class TopicResponse extends FcbBaseResponse {

    @XStreamAlias("id")
    private Long id;

    @XStreamAlias("title")
    private String title;

    @XStreamAlias("code")
    private String code;

    @XStreamAlias("isDebtor")
    private Boolean isDebtor;

    @XStreamAlias("isUnderLine")
    private Boolean isUnderLine;

    @XStreamAlias("type")
    private String type;

    @XStreamAlias("hasOppositeAccount")
    private Boolean hasOppositeAccount;

    @XStreamAlias("numOfOpenableAccounts")
    private String numOfOpenableAccounts;

    @XStreamAlias("isPermanent")
    private Boolean isPermanent;
}
