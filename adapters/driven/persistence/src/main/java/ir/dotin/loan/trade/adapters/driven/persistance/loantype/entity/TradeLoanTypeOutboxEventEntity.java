package ir.dotin.loan.trade.adapters.driven.persistance.loantype.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.messaging.persistence.entity.AbstractOutboxEventEntity;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_type_outbox_events")
@NoArgsConstructor
public class TradeLoanTypeOutboxEventEntity extends AbstractOutboxEventEntity {}
