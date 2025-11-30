package ir.dotin.loan.trade.adapters.driven.persistence.loantype;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.mapper.TradeLoanTypePersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import static java.util.Objects.requireNonNull;

@Repository
@Transactional(readOnly = true)
public class TradeLoanTypeRepositoryAdapter implements TradeLoanTypeRepository {

    private final TradeLoanTypeJpaRepository jpaRepository;
    private final TradeLoanTypePersistenceMapper mapper;

    public TradeLoanTypeRepositoryAdapter(
            TradeLoanTypeJpaRepository jpaRepository, TradeLoanTypePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TradeLoanType save(TradeLoanType loanType) {
        var entity = mapper.map(loanType);
        var saved = jpaRepository.save(requireNonNull(entity));
        return mapper.map(saved);
    }

    @Override
    public Optional<TradeLoanType> findById(LoanTypeId id) {
        return jpaRepository.findById(id.value()).map(mapper::map);
    }

    @Override
    public Boolean existsById(LoanTypeId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public Boolean existsByCode(LoanTypeCode code) {
        return jpaRepository.existsByCode_Value(code.value());
    }

    @Override
    public Optional<String> getLoanTypeCode(LoanTypeId id) {
        return jpaRepository.findCode(id.value());
    }
}
