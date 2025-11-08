package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.deposit.service.valueobjects.TransferMoneyReturnVO")
public class TransferMoneyResponse extends FcbBaseResponse {

    @XStreamAlias("depositNumber")
    private String depositNumber;

    @XStreamAlias("identifier")
    private String identifier;
}
