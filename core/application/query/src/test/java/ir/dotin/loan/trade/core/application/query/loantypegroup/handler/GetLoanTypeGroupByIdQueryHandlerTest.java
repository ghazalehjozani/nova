package ir.dotin.loan.trade.core.application.query.loantypegroup.handler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupNodeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeGroupTreeDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.dto.LoanTypeRefDto;
import ir.dotin.loan.trade.core.application.query.loantypegroup.repository.LoanTypeGroupQueryRepository;
import ir.dotin.loan.trade.core.application.query.loantypegroup.request.GetLoanTypeGroupByIdQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class GetLoanTypeGroupByIdQueryHandlerTest {

    @Mock
    private LoanTypeGroupQueryRepository repository;

    private GetLoanTypeGroupByIdQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetLoanTypeGroupByIdQueryHandler(repository);
    }

    @Test
    void returnsSubtreeRootedAtGroup() {
        UUID rootId = UUID.randomUUID();
        UUID childAId = UUID.randomUUID();
        UUID loanTypeInA = UUID.randomUUID();

        when(repository.findById(childAId))
                .thenReturn(Optional.of(new LoanTypeGroupNodeDto(childAId, "A", "الف", rootId)));
        when(repository.findAllGroups())
                .thenReturn(List.of(
                        new LoanTypeGroupNodeDto(rootId, "RT", "ریشه", null),
                        new LoanTypeGroupNodeDto(childAId, "A", "الف", rootId)));
        when(repository.findAllMemberships())
                .thenReturn(List.of(new LoanTypeRefDto(loanTypeInA, "LT-A", "نوع الف", childAId)));

        LoanTypeGroupTreeDto subtree = handler.handle(new GetLoanTypeGroupByIdQuery(childAId));

        assertThat(subtree.id()).isEqualTo(childAId);
        assertThat(subtree.children()).isEmpty();
        assertThat(subtree.loanTypes()).hasSize(1);
        assertThat(subtree.loanTypes().get(0).loanTypeId()).isEqualTo(loanTypeInA);
    }

    @Test
    void throwsNotFoundWhenGroupAbsent() {
        UUID missing = UUID.randomUUID();
        when(repository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new GetLoanTypeGroupByIdQuery(missing)))
                .isInstanceOf(FailureCauseException.class);
    }
}
