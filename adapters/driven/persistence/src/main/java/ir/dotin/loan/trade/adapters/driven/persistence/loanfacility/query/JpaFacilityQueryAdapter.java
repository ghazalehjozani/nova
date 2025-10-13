package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query.mapper.FacilityQueryModelMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.dto.FacilityQueryDto;
import ir.dotin.loan.trade.core.application.ports.outbound.query.repository.TradeLoanFacilityQueryRepository;

import lombok.RequiredArgsConstructor;

/** JPA implementation of FacilityQueryRepository Maps existing entities to query models following CQRS pattern */
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaFacilityQueryAdapter implements TradeLoanFacilityQueryRepository {

    private final TradeLoanFacilityJpaRepository facilityRepository;
    private final FacilityQueryModelMapper queryModelMapper;

    @Override
    public Optional<FacilityQueryDto> findById(UUID facilityId) {
        return facilityRepository.findById(facilityId).stream().findFirst().map(queryModelMapper::toQueryModel);
    }
}
