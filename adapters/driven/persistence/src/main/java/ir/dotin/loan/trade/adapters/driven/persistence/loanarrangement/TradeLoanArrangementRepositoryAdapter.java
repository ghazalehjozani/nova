package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement;

import java.util.Optional;

import org.springframework.stereotype.Service;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.mapper.TradeLoanArrangementPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository.TradeLoanArrangementJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class TradeLoanArrangementRepositoryAdapter implements TradeLoanArrangementRepository {

    private final TradeLoanArrangementJpaRepository jpaRepository;
    private final TradeLoanArrangementPersistenceMapper mapper;

    @Override
    public void save(TradeLoanArrangement arrangement) {
        var entity = mapper.map(arrangement);
        jpaRepository.save(requireNonNull(entity));
    }

    @Override
    public Optional<TradeLoanArrangement> findById(LoanArrangementId id) {
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }
}
