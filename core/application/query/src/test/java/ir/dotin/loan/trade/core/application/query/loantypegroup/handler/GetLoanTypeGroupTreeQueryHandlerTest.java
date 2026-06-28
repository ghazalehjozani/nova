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
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupTreeQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class GetLoanTypeGroupTreeQueryHandlerTest {

    @Mock
    private LoanTypeGroupQueryRepository repository;

    private GetLoanTypeGroupTreeQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetLoanTypeGroupTreeQueryHandler(repository);
    }

    @Test
    void assemblesNestedTreeWithMemberships() {
        UUID rootId = UUID.randomUUID();
        UUID childAId = UUID.randomUUID();
        UUID childBId = UUID.randomUUID();
        UUID loanTypeInA = UUID.randomUUID();
        UUID loanTypeInB = UUID.randomUUID();

        when(repository.findAllGroups())
                .thenReturn(List.of(
                        new LoanTypeGroupNodeDto(rootId, "RT", "ریشه", null),
                        new LoanTypeGroupNodeDto(childAId, "A", "الف", rootId),
                        new LoanTypeGroupNodeDto(childBId, "B", "ب", rootId)));
        when(repository.findAllMemberships())
                .thenReturn(List.of(
                        new LoanTypeRefDto(loanTypeInA, "LT-A", "نوع الف", childAId),
                        new LoanTypeRefDto(loanTypeInB, "LT-B", "نوع ب", childBId)));

        LoanTypeGroupTreeDto forest = handler.handle(new GetLoanTypeGroupTreeQuery());

        assertThat(forest.title()).isEqualTo("ROOT");
        assertThat(forest.children()).hasSize(1);

        LoanTypeGroupTreeDto root = forest.children().get(0);
        assertThat(root.id()).isEqualTo(rootId);
        assertThat(root.code()).isEqualTo("RT");
        assertThat(root.children()).hasSize(2);

        LoanTypeGroupTreeDto childA = root.children().stream()
                .filter(node -> node.id().equals(childAId))
                .findFirst()
                .orElseThrow();
        assertThat(childA.loanTypes()).hasSize(1);
        assertThat(childA.loanTypes().get(0).loanTypeId()).isEqualTo(loanTypeInA);
    }
}
