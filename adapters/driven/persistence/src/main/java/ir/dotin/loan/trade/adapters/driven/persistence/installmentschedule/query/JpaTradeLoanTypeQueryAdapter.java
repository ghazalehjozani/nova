package ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.query.mapper.TradeLoanTypeQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository.TradeLoanTypeJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.TradeLoanTypeQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanTypeQueryPort;

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
