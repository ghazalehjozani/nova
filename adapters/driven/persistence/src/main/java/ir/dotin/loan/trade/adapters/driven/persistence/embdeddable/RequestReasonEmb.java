package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class RequestReasonEmb implements Serializable {

    @Column(name = "request_reason_code", length = 500)
    private String code;
}
