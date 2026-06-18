package ir.dotin.loan.trade.core.application.service.shared.formula;

import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.formula.api.FormulaParameterSource;
import ir.dotin.platform.formula.api.spi.ProviderInstanceResolver;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeLoanProviderInstanceResolver implements ProviderInstanceResolver {

    private static final String LOAN_FACILITY = "LOAN_FACILITY";
    private static final String LOAN_ARRANGEMENT = "LOAN_ARRANGEMENT";

    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;

    @Override
    public Optional<FormulaParameterSource> resolve(String providerCode, String aggregateId) {
        UUID id = parseUuid(aggregateId);
        if (id == null) {
            return Optional.empty();
        }
        return switch (providerCode) {
            case LOAN_FACILITY -> facilityRepository.findById(LoanFacilityId.of(id)).map(f -> (FormulaParameterSource) f);
            case LOAN_ARRANGEMENT ->
                arrangementRepository.findById(LoanArrangementId.of(id)).map(a -> (FormulaParameterSource) a);
            default -> Optional.empty();
        };
    }

    @Nullable
    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
