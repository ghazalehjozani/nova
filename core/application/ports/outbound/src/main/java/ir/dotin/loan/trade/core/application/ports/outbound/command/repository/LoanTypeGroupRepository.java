package ir.dotin.loan.trade.core.application.ports.outbound.command.repository;

import java.util.List;
import java.util.Optional;

import ir.dotin.loan.baseloan.core.domain.loantypegroup.aggregate.LoanTypeGroup;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;

public interface LoanTypeGroupRepository {

    LoanTypeGroup save(LoanTypeGroup group);

    LoanTypeGroup save(LoanTypeGroup group, long expectedVersion);

    Optional<LoanTypeGroup> findById(LoanTypeGroupId id);

    Boolean existsById(LoanTypeGroupId id);

    List<LoanTypeGroupId> findAncestorChain(LoanTypeGroupId id);
}
