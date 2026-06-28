package ir.dotin.loan.trade.core.application.query.loantypegroup.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;

import static java.util.Objects.requireNonNull;

final class LoanTypeGroupTreeAssembler {

    private LoanTypeGroupTreeAssembler() {}

    static Map<UUID, List<LoanTypeRefDto>> membersByGroup(List<LoanTypeRefDto> memberships) {
        return memberships.stream().collect(Collectors.groupingBy(LoanTypeRefDto::groupId));
    }

    static Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent(List<LoanTypeGroupNodeDto> nodes) {
        return nodes.stream()
                .filter(node -> node.parentGroupId() != null)
                .collect(Collectors.groupingBy(node -> requireNonNull(node.parentGroupId())));
    }

    static LoanTypeGroupTreeDto build(
            LoanTypeGroupNodeDto node,
            Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent,
            Map<UUID, List<LoanTypeRefDto>> membersByGroup) {
        return build(node, childrenByParent, membersByGroup, new HashSet<>());
    }

    private static LoanTypeGroupTreeDto build(
            LoanTypeGroupNodeDto node,
            Map<UUID, List<LoanTypeGroupNodeDto>> childrenByParent,
            Map<UUID, List<LoanTypeRefDto>> membersByGroup,
            Set<UUID> visited) {
        List<LoanTypeRefDto> members = membersByGroup.getOrDefault(node.id(), List.of());
        if (!visited.add(node.id())) {
            return new LoanTypeGroupTreeDto(node.id(), node.code(), node.title(), List.of(), members);
        }
        List<LoanTypeGroupTreeDto> children = childrenByParent.getOrDefault(node.id(), List.of()).stream()
                .map(child -> build(child, childrenByParent, membersByGroup, visited))
                .toList();
        return new LoanTypeGroupTreeDto(node.id(), node.code(), node.title(), children, members);
    }
}
