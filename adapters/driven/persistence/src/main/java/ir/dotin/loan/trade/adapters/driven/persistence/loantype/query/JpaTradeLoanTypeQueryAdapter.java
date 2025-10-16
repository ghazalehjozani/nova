package ir.dotin.loan.trade.adapters.driven.persistence.loantype.query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.loan.trade.adapters.driven.persistence.loantype.query.mapper.TradeLoanTypeQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanTypeQueryPort;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanTypeQueryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JpaTradeLoanTypeQueryAdapter implements TradeLoanTypeQueryPort {

    private final TradeLoanTypeJpaRepository tradeLoanTypeJpaRepository;
    private final TradeLoanTypeQueryModelMapper queryModelMapper;

    @Override
    public Optional<TradeLoanTypeQueryDto> findById(UUID loanTypeId) {
        return tradeLoanTypeJpaRepository.findById(loanTypeId).map(queryModelMapper::toQueryModel);
    }
}
