package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import ir.dotin.platform.adapter.messaging.persistence.entity.AbstractOutboxEventEntity;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "installment_schedule_outbox_events")
@NoArgsConstructor
public class InstallmentScheduleOutboxEventEntity extends AbstractOutboxEventEntity {}
