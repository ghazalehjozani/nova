package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.ValidationResultDTO")
public class EconomicalSectorValidationResponse extends FcbBaseResponse {

    @XStreamAlias("successMessage")
    private String successMessage;

    @XStreamAlias("valid")
    private boolean valid;
}
