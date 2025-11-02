package ir.dotin.loan.trade.adapters.driven.fcbclient.dto.response.base;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Data;

@Data
public abstract class FcbBaseResponse {

    @XStreamAlias("rsCode")
    private String rsCode;

    @XStreamAlias("transactionCode")
    private String transactionCode;

    @XStreamAlias("errorMessage")
    private String errorMessage;

    @XStreamAlias("responseTimeMillis")
    private Long responseTimeMillis;

    @XStreamAlias("requestDateTime")
    private String requestDateTime;

    @XStreamAlias("responseDateTime")
    private String responseDateTime;

    @XStreamAlias("otherValues")
    private Object otherValues;

    public boolean isSuccess() {
        return !"EXCEPTION".equalsIgnoreCase(rsCode) && !"-1".equals(transactionCode);
    }

    public boolean isError() {
        return false;
    }

    public String getErrorDescription() {
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            return errorMessage;
        }
        if ("EXCEPTION".equalsIgnoreCase(rsCode)) {
            return "FCB Service returned EXCEPTION with transaction code: " + transactionCode;
        }
        if ("-1".equals(transactionCode)) {
            return "FCB Service returned error transaction code: -1";
        }
        return "Unknown error";
    }
}
