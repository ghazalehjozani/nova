package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.loan.valueobjects.uivo.LoanFileNumberVO")
public class LoanFileNumberResponse extends FcbBaseResponse {

    @XStreamAlias("loanFileNumber")
    private String loanFileNumber;
}
