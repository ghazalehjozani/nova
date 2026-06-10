package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.mapper.TradeLoanArrangementPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.projection.TradeLoanArrangementIdProjection;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository.TradeLoanArrangementJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TradeLoanArrangementRepositoryAdapter implements TradeLoanArrangementRepository {

    private final TradeLoanArrangementJpaRepository jpaRepository;
    private final TradeLoanArrangementPersistenceMapper mapper;

    @Override
    @Transactional
    public void save(TradeLoanArrangement arrangement) {
        var entity = mapper.map(arrangement);
        jpaRepository.save(requireNonNull(entity));
    }

    @Override
    public Optional<TradeLoanArrangement> findById(LoanArrangementId id) {
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public Optional<TradeLoanArrangement> findByCode(LoanArrangementCode code) {
        return jpaRepository.getByCode(code.value()).map(mapper::map);
    }

    @Override
    public Optional<UUID> getIdByCode(LoanArrangementCode code) {
        return jpaRepository.findByCode(code.value()).map(TradeLoanArrangementIdProjection::getId);
    }

    @Override
    public boolean existsByCode(LoanArrangementCode code) {
        return jpaRepository.existsByCode(code.value());
    }
}
