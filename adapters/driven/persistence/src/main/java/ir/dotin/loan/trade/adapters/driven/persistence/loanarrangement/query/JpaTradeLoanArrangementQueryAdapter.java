package ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.query.mapper.LoanArrangementQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanarrangement.repository.TradeLoanArrangementJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanArrangementQueryPort;
import ir.dotin.loan.trade.core.application.ports.outbound.query.request.TradeLoanArrangementQueryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JpaTradeLoanArrangementQueryAdapter implements TradeLoanArrangementQueryPort {
    private final TradeLoanArrangementJpaRepository jpaRepository;
    private final LoanArrangementQueryModelMapper queryModelMapper;

    @Override
    public Optional<TradeLoanArrangementQueryDto> findById(UUID loanArrangementId) {
        return jpaRepository.findById(loanArrangementId).map(queryModelMapper::toQueryModel);
    }
}
