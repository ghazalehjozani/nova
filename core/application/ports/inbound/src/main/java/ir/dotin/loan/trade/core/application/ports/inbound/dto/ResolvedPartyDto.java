package ir.dotin.loan.trade.core.application.ports.inbound.dto;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;

import lombok.Builder;

/**
 * Transport-neutral carrier for an FCB-resolved party, produced by the tx-free origination pre-flight query and
 * threaded back into {@link ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand} so
 * the lean transactional command can rebuild the enriched {@code PartyInfoResponse} without re-issuing FCB calls.
 *
 * <p>Carries every field needed to faithfully reconstruct the domain {@code Party} + {@code PartyInfoResponse} pair,
 * but uses only base-loan ENUMS and plain Java types — the inbound module may not import outbound
 * {@code PartyInfoResponse} or the domain {@code Party} sealed hierarchy.
 */
@Builder
public record ResolvedPartyDto(
        String customerNumber,
        PartyType partyType,
        PartyRole role,
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String companyName,
        @Nullable BigDecimal guaranteePercentage,
        String nationalCode,
        boolean isInBlackList,
        boolean isIncapable,
        boolean isInGrayList) {}
