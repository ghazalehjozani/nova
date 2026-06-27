package ir.dotin.loan.trade.core.application.query.loantypegroup.handler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeListResult;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.ListRootLoanTypeGroupsQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListRootLoanTypeGroupsQueryHandler
        implements QueryHandler<ListRootLoanTypeGroupsQuery, LoanTypeGroupTreeListResult> {

    private final LoanTypeGroupQueryRepository repository;

    @Override
    public LoanTypeGroupTreeListResult handle(ListRootLoanTypeGroupsQuery query) {
        List<LoanTypeGroupNodeDto> nodes = repository.findAllGroups();
        Map<UUID, List<LoanTypeRefDto>> membersByGroup =
                LoanTypeGroupTreeAssembler.membersByGroup(repository.findAllMemberships());
        Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent = LoanTypeGroupTreeAssembler.childrenByParent(nodes);

        List<LoanTypeGroupTreeDto> roots = nodes.stream()
                .filter(node -> node.parentGroupId() == null)
                .map(node -> LoanTypeGroupTreeAssembler.build(node, childrenByParent, membersByGroup))
                .toList();

        return new LoanTypeGroupTreeListResult(roots);
    }
}
