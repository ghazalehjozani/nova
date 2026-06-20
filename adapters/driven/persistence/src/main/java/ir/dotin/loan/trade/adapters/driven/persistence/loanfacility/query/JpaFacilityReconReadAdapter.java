package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.query;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection.FacilityReconStateProjection;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.FacilityReconRow;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconGuarantor;

import lombok.RequiredArgsConstructor;

/**
 * JPA implementation of {@link FacilityReconReadPort}. Pages non-terminal facilities by a {@code (modifiedAt, id)}
 * keyset cursor and resolves single facilities by id, returning the narrow {@link FacilityReconRow} read model.
 *
 * <p>The opaque cursor string is {@code <modifiedAtEpochMs>|<id>}; pangaea never interprets it, only this adapter does.
 */
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JpaFacilityReconReadAdapter implements FacilityReconReadPort {

    private static final Set<FacilityStatus> TERMINAL_STATES = Set.of(
            FacilityStatus.REJECTED,
            FacilityStatus.CLOSED_PAID_OFF,
            FacilityStatus.CLOSED_DEFAULTED,
            FacilityStatus.CANCELLED);

    private final TradeLoanFacilityJpaRepository repository;

    @Override
    public List<FacilityReconRow> pageNonTerminal(@Nullable String cursor, int size) {
        Limit limit = Limit.of(Math.max(1, size));
        List<FacilityReconStateProjection> page;
        Cursor decoded = decodeCursor(cursor);
        if (decoded == null) {
            page = repository.pageNonTerminalFirst(TERMINAL_STATES, limit);
        } else {
            page = repository.pageNonTerminalAfter(TERMINAL_STATES, decoded.modifiedAt(), decoded.id(), limit);
        }
        return page.stream().map(p -> toRow(p)).toList();
    }

    @Override
    public Optional<FacilityReconRow> findById(String facilityId, boolean includeGuarantors) {
        UUID id;
        try {
            id = UUID.fromString(facilityId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        // Guarantor set is projected ONLY when asked (the guarantor-drift probe path). When the feature is dark the
        // probe passes includeGuarantors=false, so the extra findGuarantorsById query is never issued — zero cost for a
        // disabled feature. Reads the persisted guarantor parties without loading the full graph or command aggregate.
        return repository.findReconStateById(id).map(p -> toRow(p, includeGuarantors ? readGuarantors(id) : List.of()));
    }

    private List<ReconGuarantor> readGuarantors(UUID id) {
        return repository.findGuarantorsById(id).stream()
                .map(g -> new ReconGuarantor(
                        Objects.requireNonNull(g.getCustomerNumber(), "guarantor customerNumber"),
                        g.getGuaranteePercentage()))
                .toList();
    }

    private static FacilityReconRow toRow(FacilityReconStateProjection p) {
        return toRow(p, List.of());
    }

    private static FacilityReconRow toRow(FacilityReconStateProjection p, List<ReconGuarantor> guarantors) {
        UUID id = Objects.requireNonNull(p.getId(), "facility id");
        FacilityStatus status = Objects.requireNonNull(p.getCurrentState(), "facility currentState");
        long epochMs = toEpochMs(p.getModifiedAt());
        String applicationNumber = formatApplicationNumber(
                p.getApplicationBranchCode(),
                p.getApplicationLoanTypeCode(),
                p.getApplicationCustomerNumber(),
                p.getApplicationDerivedValue());
        return new FacilityReconRow(id.toString(), status, epochMs, applicationNumber, guarantors);
    }

    private static @Nullable String formatApplicationNumber(
            @Nullable String branchCode,
            @Nullable String loanTypeCode,
            @Nullable String customerNumber,
            @Nullable String derivedValue) {
        if (branchCode == null || loanTypeCode == null || customerNumber == null || derivedValue == null) {
            return null;
        }
        return MessageFormat.format("{0}-{1}-{2}-{3}", branchCode, loanTypeCode, customerNumber, derivedValue);
    }

    private static long toEpochMs(@Nullable LocalDateTime modifiedAt) {
        return modifiedAt == null ? 0L : modifiedAt.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private static @Nullable Cursor decodeCursor(@Nullable String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        int sep = cursor.indexOf('|');
        if (sep <= 0 || sep == cursor.length() - 1) {
            return null;
        }
        try {
            long epochMs = Long.parseLong(cursor.substring(0, sep));
            UUID id = UUID.fromString(cursor.substring(sep + 1));
            LocalDateTime modifiedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneOffset.UTC);
            return new Cursor(modifiedAt, id);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private record Cursor(LocalDateTime modifiedAt, UUID id) {}
}
