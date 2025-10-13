package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.messaging.persistence.entity.AbstractOutboxEventEntity;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_facility_outbox_events")
@NoArgsConstructor
public class TradeLoanFacilityOutboxEventEntity extends AbstractOutboxEventEntity {}
