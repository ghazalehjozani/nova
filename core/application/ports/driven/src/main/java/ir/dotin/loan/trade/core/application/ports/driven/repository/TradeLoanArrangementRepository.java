package ir.dotin.loan.trade.core.application.ports.driven.repository;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

public interface TradeLoanArrangementRepository {

    TradeLoanArrangement save(TradeLoanArrangement arrangement);

    boolean existsByCode(String code);

    Optional<TradeLoanArrangement> findById(UUID id);

    Optional<TradeLoanArrangement> findByCode(String code);
}
