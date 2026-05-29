package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

import lombok.Data;

@Data
@Embeddable
public class SamatEmb implements Serializable {

    @Nullable
    @Column(name = "tracking_number", length = 16)
    private String trackingNumber;

    @Nullable
    @Column(name = "isic_economic_sector")
    private String isicEconomicSector;

    @Nullable
    @Column(name = "sub_isic_economic_sector")
    private String subIsicEconomicSector;

    @Nullable
    @Column(name = "use_type")
    private String useType;

    @Nullable
    @Column(name = "exception_code")
    private String exceptionCode;

    @Nullable
    @Column(name = "consumption_place_code")
    private String consumptionPlaceCode;
}
