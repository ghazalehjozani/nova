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
            select count(t) from TradeLoanFacilityEntity t
            where t.loanApplication.branch.code = :code
            and t.loanTypeId = :loanTypeId
            and t.loanApplication.customer.customerNumber = :customerNumber
            """)
    long countByBranchCodeAndLoanTypeIdAndCustomerNumber(
            @Param("code") String code,
            @Param("loanTypeId") UUID loanTypeId,
            @Param("customerNumber") String customerNumber);
}
