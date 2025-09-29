package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class CertificateEmb implements Serializable {

    @Column(name = "serial", nullable = false, length = 100)
    private String serial;
}
