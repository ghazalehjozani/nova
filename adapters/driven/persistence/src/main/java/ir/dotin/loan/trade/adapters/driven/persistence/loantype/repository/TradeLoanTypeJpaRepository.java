package ir.dotin.loan.trade.adapters.driven.persistence.loantype.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ir.dotin.platform.pangaea.persistence.jpa.repository.PersistentRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.entity.TradeLoanTypeEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loantype.projection.TradeLoanTypeIdProjection;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;

@Repository
public interface TradeLoanTypeJpaRepository extends PersistentRepository<TradeLoanTypeEntity> {
    boolean existsByCode_Value(String value);

    @Query("select new ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto("
            + "t.id, t.code.value, t.title.value, t.groupId) "
            + "from TradeLoanTypeEntity t where t.groupId is not null")
    List<LoanTypeRefDto> findGroupMemberships();

    Window<TradeLoanTypeEntity> findAllBy(ScrollPosition position, Limit limit, Sort sort);

    @Query("select t.code.value from TradeLoanTypeEntity t where t.id = :id")
    Optional<String> findCode(@Param("id") UUID id);

    Optional<TradeLoanTypeEntity> getByCode_Value(@NonNull String value);

    Optional<TradeLoanTypeIdProjection> findByCode_Value(String value);
}
