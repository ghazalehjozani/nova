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

    @Query(
            """
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
}
