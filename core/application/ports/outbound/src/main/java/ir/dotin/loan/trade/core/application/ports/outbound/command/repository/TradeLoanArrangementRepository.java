package ir.dotin.loan.trade.core.application.ports.outbound.command.repository;

import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

public interface TradeLoanArrangementRepository {

    void save(TradeLoanArrangement arrangement);

    Optional<TradeLoanArrangement> findById(LoanArrangementId id);

    boolean existsByCode(String code);
}
