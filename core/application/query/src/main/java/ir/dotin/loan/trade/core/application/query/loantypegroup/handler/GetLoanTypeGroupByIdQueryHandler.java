package ir.dotin.loan.trade.core.application.query.loantypegroup.handler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.baseloan.core.domain.loantypegroup.error.LoanTypeGroupErrors;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupByIdQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanTypeGroupByIdQueryHandler implements QueryHandler<GetLoanTypeGroupByIdQuery, LoanTypeGroupTreeDto> {

    private final LoanTypeGroupQueryRepository repository;

    @Override
    public LoanTypeGroupTreeDto handle(GetLoanTypeGroupByIdQuery query) {
        LoanTypeGroupNodeDto root = repository.findById(query.groupId()).orElseThrow(() -> {
            var notification = Notification.ofError(LoanTypeGroupErrors.GROUP_NOT_FOUND, query.groupId());
            return new FailureCauseException(FailureCause.notFound(notification));
        });

        List<LoanTypeGroupNodeDto> nodes = repository.findAllGroups();
        Map<UUID, List<LoanTypeRefDto>> membersByGroup =
                LoanTypeGroupTreeAssembler.membersByGroup(repository.findAllMemberships());
        Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent = LoanTypeGroupTreeAssembler.childrenByParent(nodes);

        return LoanTypeGroupTreeAssembler.build(root, childrenByParent, membersByGroup);
    }
}
