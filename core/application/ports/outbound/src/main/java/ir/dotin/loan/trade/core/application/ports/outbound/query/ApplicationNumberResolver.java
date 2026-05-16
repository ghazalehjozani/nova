package ir.dotin.loan.trade.core.application.ports.outbound.query;

import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;

/**
 * Resolves application numbers to domain aggregate identifiers.
 *
 * <p>This is a driven (outbound) port implemented by the persistence adapter. Used by application services (command
 * handlers) to resolve loan identifiers from application numbers.
 */
public interface ApplicationNumberResolver {

    record LoanIdentifiers(UUID loanFacilityId, UUID installmentScheduleId) {}

    Optional<LoanIdentifiers> resolveByApplicationNumber(String applicationNumber);

    Optional<LoanFacilityId> resolveLoanFacilityIdByApplicationNumber(String applicationNumber);
}
