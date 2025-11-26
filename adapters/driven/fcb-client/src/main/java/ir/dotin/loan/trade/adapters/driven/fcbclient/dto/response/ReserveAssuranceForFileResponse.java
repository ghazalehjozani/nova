package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.ReserveAssuranceForFileResult")
public class ReserveAssuranceForFileResponse extends FcbBaseResponse {

    @XStreamAlias("fileNumber")
    private String fileNumber;

    @XStreamAlias("assuranceSerials")
    private List<String> assuranceSerials;
}
