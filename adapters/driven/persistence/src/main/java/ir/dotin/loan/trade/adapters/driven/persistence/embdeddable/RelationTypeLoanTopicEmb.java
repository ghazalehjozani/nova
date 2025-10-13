package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeLoanTopicEmb implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_relation_type", nullable = false, length = 50)
    private TradeRelationType tradeRelationType;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "topic_code", nullable = false, length = 50)
    private String topicCode;

    @Column(name = "relation_type_code", nullable = false, length = 50)
    private String relationTypeCode;

    @Column(name = "relation_type_name", nullable = false, length = 100)
    private String relationTypeName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "code",
                column = @Column(name = "topic_economic_sector_code", nullable = false, length = 50)),
        @AttributeOverride(
                name = "name",
                column = @Column(name = "topic_economic_sector_name", nullable = false, length = 100))
    })
    private EconomicSectorEmb economicSector;
}
