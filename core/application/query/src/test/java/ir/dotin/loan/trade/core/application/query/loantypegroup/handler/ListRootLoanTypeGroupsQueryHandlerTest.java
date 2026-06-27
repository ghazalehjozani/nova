package ir.dotin.loan.trade.core.application.query.loantypegroup.handler;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeListResult;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.ListRootLoanTypeGroupsQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class ListRootLoanTypeGroupsQueryHandlerTest {

    @Mock
    private LoanTypeGroupQueryRepository repository;

    private ListRootLoanTypeGroupsQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListRootLoanTypeGroupsQueryHandler(repository);
    }

    @Test
    void returnsOnlyRootsEachWithItsSubtree() {
        UUID rootOneId = UUID.randomUUID();
        UUID childOfRootOneId = UUID.randomUUID();
        UUID rootTwoId = UUID.randomUUID();

        when(repository.findAllGroups())
                .thenReturn(List.of(
                        new LoanTypeGroupNodeDto(rootOneId, "ریشه ۱", null),
                        new LoanTypeGroupNodeDto(childOfRootOneId, "فرزند", rootOneId),
                        new LoanTypeGroupNodeDto(rootTwoId, "ریشه ۲", null)));
        when(repository.findAllMemberships()).thenReturn(List.of());

        LoanTypeGroupTreeListResult result = handler.handle(new ListRootLoanTypeGroupsQuery());

        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).extracting(LoanTypeGroupTreeDto::id).containsExactlyInAnyOrder(rootOneId, rootTwoId);

        LoanTypeGroupTreeDto rootOne = result.items().stream()
                .filter(node -> node.id().equals(rootOneId))
                .findFirst()
                .orElseThrow();
        assertThat(rootOne.children()).hasSize(1);
        assertThat(rootOne.children().get(0).id()).isEqualTo(childOfRootOneId);
    }
}
