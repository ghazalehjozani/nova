package ir.dotin.loan.trade.adapters.driven.persistence.loantype.query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.loan.trade.adapters.driven.persistence.loantype.query.mapper.TradeLoanTypeQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.core.application.query.loantype.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.query.loantype.repository.TradeLoanTypeQueryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JpaTradeLoanTypeQueryAdapter implements TradeLoanTypeQueryRepository {

    private final TradeLoanTypeJpaRepository tradeLoanTypeJpaRepository;
    private final TradeLoanTypeQueryModelMapper queryModelMapper;

    @Override
    public Optional<TradeLoanTypeQueryDto> findById(UUID loanTypeId) {
        return tradeLoanTypeJpaRepository.findById(loanTypeId).map(queryModelMapper::toQueryModel);
    }
}
