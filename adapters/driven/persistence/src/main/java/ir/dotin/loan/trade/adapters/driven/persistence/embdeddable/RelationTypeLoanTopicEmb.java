package ir.dotin.loan.trade.adapters.driven.persistence.embdeddable;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class RelationTypeLoanTopicEmb implements Serializable {

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_relation_type", nullable = false)
    private TradeRelationType tradeRelationType;

    @Nullable
    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Nullable
    @Column(name = "topic_code", nullable = false)
    private String topicCode;

    @Nullable
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "loan_topic_economic_sectors", columnDefinition = "jsonb")
    private Set<String> economicSectors;
}
