package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base.FcbBaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@XStreamAlias("com.fanap.business.cmplexpenditure.dto.HasAllowedCurrencyResultDTO")
public class HasAllowedCurrencyResponse extends FcbBaseResponse {

    @XStreamAlias("successMessage")
    private String successMessage;

    @XStreamAlias("allowed")
    private Boolean allowed;

    public HasAllowedCurrencyResponse(Boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isAllowed() {
        return allowed != null && allowed;
    }
}
