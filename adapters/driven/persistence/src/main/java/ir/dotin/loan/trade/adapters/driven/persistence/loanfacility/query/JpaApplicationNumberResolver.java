package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.query.ApplicationNumberResolver;

import lombok.RequiredArgsConstructor;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaApplicationNumberResolver implements ApplicationNumberResolver {

    private final TradeLoanFacilityJpaRepository facilityRepository;

    @Override
    public Optional<LoanIdentifiers> resolveByApplicationNumber(String applicationNumber) {
        return facilityRepository
                .findByApplicationNumber(applicationNumber)
                .map(entity -> new LoanIdentifiers(entity.getId(), entity.getInstallmentScheduleId()));
    }

    @Override
    public Optional<LoanFacilityId> resolveLoanFacilityIdByApplicationNumber(String applicationNumber) {
        return facilityRepository
                .findByApplicationNumber(applicationNumber)
                .map(entity -> new LoanFacilityId(entity.getId()));
    }
}
