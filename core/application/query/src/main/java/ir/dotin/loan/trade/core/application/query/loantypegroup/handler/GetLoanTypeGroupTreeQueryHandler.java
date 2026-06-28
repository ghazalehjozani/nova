package ir.dotin.loan.trade.core.application.query.loantypegroup.handler;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.servicelayer.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupTreeQuery;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLoanTypeGroupTreeQueryHandler implements QueryHandler<GetLoanTypeGroupTreeQuery, LoanTypeGroupTreeDto> {

    private static final UUID FOREST_ROOT_ID = new UUID(0L, 0L);
    private static final String FOREST_ROOT_CODE = "ROOT";

    private final LoanTypeGroupQueryRepository repository;

    @Override
    public LoanTypeGroupTreeDto handle(GetLoanTypeGroupTreeQuery query) {
        List<LoanTypeGroupNodeDto> nodes = repository.findAllGroups();
        Map<UUID, List<LoanTypeRefDto>> membersByGroup =
                LoanTypeGroupTreeAssembler.membersByGroup(repository.findAllMemberships());
        Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent = LoanTypeGroupTreeAssembler.childrenByParent(nodes);

        List<LoanTypeGroupTreeDto> roots = nodes.stream()
                .filter(node -> node.parentGroupId() == null)
                .map(node -> LoanTypeGroupTreeAssembler.build(node, childrenByParent, membersByGroup))
                .toList();

        return new LoanTypeGroupTreeDto(FOREST_ROOT_ID, FOREST_ROOT_CODE, "ROOT", roots, List.of());
    }
}
