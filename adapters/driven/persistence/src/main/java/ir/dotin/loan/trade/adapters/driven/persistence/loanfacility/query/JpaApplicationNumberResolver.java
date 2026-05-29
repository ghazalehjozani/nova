package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

import java.util.Objects;
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
    @SuppressWarnings(
            "NullAway") // LoanIdentifiers.installmentScheduleId is @NonNull in the port record but the column is
    // nullable; port type is out of scope for this module
    public Optional<LoanIdentifiers> resolveByApplicationNumber(String applicationNumber) {
        return facilityRepository
                .findByApplicationNumber(applicationNumber)
                .map(entity -> new LoanIdentifiers(
                        Objects.requireNonNull(entity.getId(), "entity.id"), entity.getInstallmentScheduleId()));
    }

    @Override
    public Optional<LoanFacilityId> resolveLoanFacilityIdByApplicationNumber(String applicationNumber) {
        return facilityRepository
                .findByApplicationNumber(applicationNumber)
                .map(entity -> new LoanFacilityId(Objects.requireNonNull(entity.getId(), "entity.id")));
    }
}
