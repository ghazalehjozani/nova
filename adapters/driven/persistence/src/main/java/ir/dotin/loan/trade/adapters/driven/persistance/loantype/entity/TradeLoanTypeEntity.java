package ir.dotin.loan.trade.adapters.driven.persistance.loantype.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.persistence.entity.PersistentEntity;
import ir.dotin.loan.baseloan.core.domain.loantype.enums.SegmentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.GatewayType;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.AttributeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.EconomicSectorCurrencyEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.EditReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.LoanTypeCodeEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.RelationTypeLoanTopicEmb;
import ir.dotin.loan.trade.adapters.driven.persistance.embdeddable.TitleEmb;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loan_types")
@Getter
@Setter
@NoArgsConstructor
public class TradeLoanTypeEntity extends PersistentEntity {

    @Embedded
    private LoanTypeCodeEmb code;

    @Embedded
    private TitleEmb title;

    @Embedded
    private EditReasonEmb editReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_type", nullable = false, length = 30)
    private GatewayType gatewayType;

    @Column(name = "loan_application_allowed", nullable = false)
    private Boolean loanApplicationAllowed;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment_type", nullable = false, length = 30)
    private SegmentType segmentType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_type_economic_sectors",
            joinColumns = @JoinColumn(name = "loan_type_id"),
            indexes = @Index(name = "idx_trade_loan_type_economic_sector", columnList = "loan_type_id"))
    private Set<EconomicSectorCurrencyEmb> economicSectorCurrencies = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_type_income_ids",
            joinColumns = @JoinColumn(name = "loan_type_id"),
            indexes = @Index(name = "idx_trade_loan_type_income", columnList = "loan_type_id"))
    @Column(name = "income_id", nullable = false)
    private Set<UUID> incomeIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_type_arrangement_ids",
            joinColumns = @JoinColumn(name = "loan_type_id"),
            indexes = @Index(name = "idx_trade_loan_type_arrangement", columnList = "loan_type_id"))
    @Column(name = "arrangement_id", nullable = false)
    private Set<UUID> loanArrangementIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_type_topics",
            joinColumns = @JoinColumn(name = "loan_type_id"),
            indexes = @Index(name = "idx_trade_loan_type_topic", columnList = "loan_type_id"))
    private Set<RelationTypeLoanTopicEmb> relationTypeLoanTopics = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_type_attributes",
            joinColumns = @JoinColumn(name = "loan_type_id"),
            indexes = @Index(name = "idx_trade_loan_type_attribute", columnList = "loan_type_id"))
    private List<AttributeEmb> attributes;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "disable", nullable = false)
    private boolean disable = false;

    @Column(name = "previous_version_id")
    private UUID previousVersion;
}
