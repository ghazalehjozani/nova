package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class SamatEmb implements Serializable {

    @Column(name = "tracking_number", length = 16)
    private String trackingNumber;

    @Column(name = "isic_economic_sector")
    private String isicEconomicSector;

    @Column(name = "sub_isic_economic_sector")
    private String subIsicEconomicSector;

    @Column(name = "use_type")
    private String useType;

    @Column(name = "exception_code")
    private String exceptionCode;

    @Column(name = "consumption_place_code")
    private String consumptionPlaceCode;
}
