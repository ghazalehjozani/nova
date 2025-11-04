package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("ir.dotin.lc.dto.ilccredit.bill.SharedTopicElectronicBillVO")
public class LoanTopicResponse extends FcbBaseResponse {

    @XStreamAlias("mainTopicIsDebtor")
    private Boolean mainTopicIsDebtor;

    @XStreamAlias("bankCommitmentsTopicIsDebtor")
    private Boolean bankCommitmentsTopicIsDebtor;

    @XStreamAlias("customerCommitmentsTopicIsDebtor")
    private Boolean customerCommitmentsTopicIsDebtor;

    @XStreamAlias("temporaryDebtorsTopicIsDebtor")
    private Boolean temporaryDebtorsTopicIsDebtor;
}
