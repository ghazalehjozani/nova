package ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.repository;

import org.springframework.stereotype.Repository;

import ir.dotin.platform.pangaea.outbox.jpa.repository.BaseOutboxRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantypegroup.entity.LoanTypeGroupOutboxEventEntity;

@Repository
public interface LoanTypeGroupOutboxEventRepository extends BaseOutboxRepository<LoanTypeGroupOutboxEventEntity> {}
