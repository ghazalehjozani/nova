package ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.mapper.TradeLoanArrangementPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistance.loanarrangement.repository.TradeLoanArrangementJpaRepository;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
public class TradeLoanArrangementRepositoryAdapter implements TradeLoanArrangementRepository {

    private final TradeLoanArrangementJpaRepository jpaRepository;
    private final TradeLoanArrangementPersistenceMapper mapper;

    public TradeLoanArrangementRepositoryAdapter(
            TradeLoanArrangementJpaRepository jpaRepository, TradeLoanArrangementPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TradeLoanArrangement save(TradeLoanArrangement arrangement) {
        var entity = mapper.map(arrangement);
        var saved = jpaRepository.save(requireNonNull(entity));
        return mapper.map(saved);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public Optional<TradeLoanArrangement> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::map);
    }

    @Override
    public Optional<TradeLoanArrangement> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::map);
    }
}
