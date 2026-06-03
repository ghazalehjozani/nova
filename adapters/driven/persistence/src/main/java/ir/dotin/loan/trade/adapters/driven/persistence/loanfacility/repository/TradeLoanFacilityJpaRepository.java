package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.pangaea.persistence.jpa.repository.PersistentRepository;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.projection.FacilityReconStateProjection;

@Repository
public interface TradeLoanFacilityJpaRepository extends PersistentRepository<TradeLoanFacilityEntity> {

    Window<TradeLoanFacilityEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);

    /**
     * First keyset page of non-terminal facilities for reconciliation, ordered by {@code (modifiedAt, id)} so a sweep
     * is resumable after a lost lease. {@code modifiedAt} is coalesced to {@code createdAt} for never-modified rows.
     */
    @Query("""
            SELECT t.id AS id, t.currentState AS currentState, COALESCE(t.modifiedAt, t.createdAt) AS modifiedAt
            FROM TradeLoanFacilityEntity t
            WHERE t.currentState NOT IN :terminalStates
            AND COALESCE(t.modifiedAt, t.createdAt) <= :modifiedBefore
            ORDER BY COALESCE(t.modifiedAt, t.createdAt) ASC, t.id ASC
            """)
    List<FacilityReconStateProjection> pageNonTerminalFirst(
            @Param("terminalStates") Collection<FacilityStatus> terminalStates,
            @Param("modifiedBefore") LocalDateTime modifiedBefore,
            Limit limit);

    /**
     * Subsequent keyset page of non-terminal facilities for reconciliation, continuing strictly after the supplied
     * {@code (modifiedAt, id)} cursor.
     */
    @Query("""
            SELECT t.id AS id, t.currentState AS currentState, COALESCE(t.modifiedAt, t.createdAt) AS modifiedAt
            FROM TradeLoanFacilityEntity t
            WHERE t.currentState NOT IN :terminalStates
            AND COALESCE(t.modifiedAt, t.createdAt) <= :modifiedBefore
            AND (COALESCE(t.modifiedAt, t.createdAt) > :afterModifiedAt
                 OR (COALESCE(t.modifiedAt, t.createdAt) = :afterModifiedAt AND t.id > :afterId))
            ORDER BY COALESCE(t.modifiedAt, t.createdAt) ASC, t.id ASC
            """)
    List<FacilityReconStateProjection> pageNonTerminalAfter(
            @Param("terminalStates") Collection<FacilityStatus> terminalStates,
            @Param("afterModifiedAt") LocalDateTime afterModifiedAt,
            @Param("afterId") UUID afterId,
            @Param("modifiedBefore") LocalDateTime modifiedBefore,
            Limit limit);

    /** Narrow projection lookup of a single facility's reconciliation row by id. */
    @Query("""
            SELECT t.id AS id, t.currentState AS currentState, COALESCE(t.modifiedAt, t.createdAt) AS modifiedAt
            FROM TradeLoanFacilityEntity t
            WHERE t.id = :id
            """)
    Optional<FacilityReconStateProjection> findReconStateById(@Param("id") UUID id);

    @Query("""
            SELECT count(t)
            FROM TradeLoanFacilityEntity t
            JOIN t.loanApplication.parties p
            WHERE t.loanApplication.branch.code = :code
            AND t.loanTypeId = :loanTypeId
            AND p.customerNumber = :customerNumber
            AND p.partyRole = 'PRIMARY_APPLICANT'
            """)
    long countByBranchCodeAndLoanTypeIdAndCustomerNumber(
            @Param("code") String code,
            @Param("loanTypeId") @Nullable UUID loanTypeId,
            @Param("customerNumber") String customerNumber);

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TradeLoanFacilityEntity t
        WHERE t.loanApplication.applicationNumber.branch.code = :branchCode
        AND t.loanApplication.applicationNumber.loanTypeCode.value = :loanTypeCode
        AND t.loanApplication.applicationNumber.party.customerNumber = :customerNumber
        AND t.loanApplication.applicationNumber.derivedValue = :derivedValue
        """)
    boolean existsByApplicationNumber(
            @Param("branchCode") String branchCode,
            @Param("loanTypeCode") String loanTypeCode,
            @Param("customerNumber") String customerNumber,
            @Param("derivedValue") String derivedValue);

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TradeLoanFacilityEntity t
        WHERE t.loanApplication.applicationNumber.branch.code = :branchCode
        AND t.loanApplication.applicationNumber.loanTypeCode.value = :loanTypeCode
        AND t.loanApplication.applicationNumber.party.customerNumber = :customerNumber
        AND t.loanApplication.applicationNumber.derivedValue = :derivedValue
        AND t.currentState NOT IN :terminalStates
        """)
    boolean existsActiveByApplicationNumber(
            @Param("branchCode") String branchCode,
            @Param("loanTypeCode") String loanTypeCode,
            @Param("customerNumber") String customerNumber,
            @Param("derivedValue") String derivedValue,
            @Param("terminalStates") Collection<FacilityStatus> terminalStates);

    @Query("""
        SELECT t
        FROM TradeLoanFacilityEntity t
        WHERE t.loanApplication.applicationNumber.branch.code = :branchCode
        AND t.loanApplication.applicationNumber.loanTypeCode.value = :loanTypeCode
        AND t.loanApplication.applicationNumber.party.customerNumber = :customerNumber
        AND t.loanApplication.applicationNumber.derivedValue = :derivedValue
        """)
    Optional<TradeLoanFacilityEntity> findByApplicationNumberComponents(
            @Param("branchCode") String branchCode,
            @Param("loanTypeCode") String loanTypeCode,
            @Param("customerNumber") String customerNumber,
            @Param("derivedValue") String derivedValue);

    default Optional<TradeLoanFacilityEntity> findByApplicationNumber(String applicationNumber) {
        if (applicationNumber == null || applicationNumber.isBlank()) {
            return Optional.empty();
        }

        String[] parts = applicationNumber.split("-");

        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Invalid Application Number format. Expected 4 parts (Branch-Type-Customer-Derived), got: "
                            + applicationNumber);
        }

        return findByApplicationNumberComponents(
                parts[0], // branchCode
                parts[1], // loanTypeCode
                parts[2], // customerNumber
                parts[3] // derivedValue
                );
    }
}
