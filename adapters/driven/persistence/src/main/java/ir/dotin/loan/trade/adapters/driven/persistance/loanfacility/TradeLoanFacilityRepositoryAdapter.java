package ir.dotin.loan.trade.adapters.driven.persistance.loanfacility;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.mapper.TradeLoanFacilityPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistance.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.application.ports.driven.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
public class TradeLoanFacilityRepositoryAdapter implements TradeLoanFacilityRepository {

    private final TradeLoanFacilityJpaRepository jpaRepository;
    private final TradeLoanFacilityPersistenceMapper mapper;

    public TradeLoanFacilityRepositoryAdapter(
            TradeLoanFacilityJpaRepository jpaRepository, TradeLoanFacilityPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TradeLoanFacility save(TradeLoanFacility facility) {
        requireNonNull(facility, "TradeLoanFacility cannot be null");
        var entity = mapper.map(facility);
        var saved = jpaRepository.save(entity);
        return mapper.map(saved);
    }

    @Override
    public Optional<TradeLoanFacility> findById(LoanFacilityId id) {
        requireNonNull(id, "ID cannot be null");
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public Result<Boolean> existsById(LoanFacilityId id) {
        return Result.success(jpaRepository.existsById(id.value()));
    }
}
