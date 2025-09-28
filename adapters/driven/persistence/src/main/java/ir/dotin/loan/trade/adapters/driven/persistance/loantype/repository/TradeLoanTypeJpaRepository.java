package ir.dotin.loan.trade.adapters.driven.persistance.loantype.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.trade.adapters.driven.persistance.loantype.entity.TradeLoanTypeEntity;

@Repository
public interface TradeLoanTypeJpaRepository extends JpaRepository<TradeLoanTypeEntity, UUID> {

    boolean existsByCode(String code);

    Optional<TradeLoanTypeEntity> findByCode(String code);

    List<TradeLoanTypeEntity> findByGroupId(UUID groupId);

    List<TradeLoanTypeEntity> findByActive(boolean active);

    List<TradeLoanTypeEntity> findBySegmentType(String segmentType);

    @Query("SELECT e FROM TradeLoanTypeEntity e WHERE e.title LIKE %:titleKeyword%")
    List<TradeLoanTypeEntity> findByTitleContaining(@Param("titleKeyword") String titleKeyword);

    List<TradeLoanTypeEntity> findByPreviousVersion(UUID previousVersionId);

    @Query("SELECT e FROM TradeLoanTypeEntity e WHERE e.previousVersion IS NULL")
    List<TradeLoanTypeEntity> findLatestVersions();

    @Query("SELECT e FROM TradeLoanTypeEntity e WHERE e.gatewayType = 'TRADE'")
    List<TradeLoanTypeEntity> findTradeSpecificLoanTypes();
}
