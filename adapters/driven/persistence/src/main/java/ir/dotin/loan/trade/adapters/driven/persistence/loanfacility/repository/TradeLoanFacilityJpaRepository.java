package ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository;

import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.adapter.persistence.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;

@Repository
public interface TradeLoanFacilityJpaRepository extends PersistentRepository<TradeLoanFacilityEntity> {

    Window<TradeLoanFacilityEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);

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
            @Param("loanTypeId") UUID loanTypeId,
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
}
