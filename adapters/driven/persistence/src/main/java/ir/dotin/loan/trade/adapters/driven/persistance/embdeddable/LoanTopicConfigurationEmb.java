package ir.dotin.loan.trade.adapters.driven.persistance.embdeddable;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.Data;

@Data
@Embeddable
public class LoanTopicConfigurationEmb implements Serializable {

    @Column(name = "loan_topic_code", length = 100)
    private String loanTopicCode;

    @Column(name = "economic_sector_code", nullable = false, length = 50)
    private String economicSectorCode;

    @Column(name = "economic_sector_name", nullable = false, length = 100)
    private String economicSectorName;

    @Embedded
    private CurrencyType currencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", length = 50)
    private TradeRelationType relationType;
}
