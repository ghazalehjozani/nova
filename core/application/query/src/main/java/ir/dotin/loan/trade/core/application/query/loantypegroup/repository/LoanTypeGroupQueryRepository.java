package ir.dotin.loan.trade.core.application.query.loantypegroup.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;

public interface LoanTypeGroupQueryRepository {

    List<LoanTypeGroupNodeDto> findAllGroups();

    List<LoanTypeRefDto> findAllMemberships();

    Optional<LoanTypeGroupNodeDto> findById(UUID id);
}
