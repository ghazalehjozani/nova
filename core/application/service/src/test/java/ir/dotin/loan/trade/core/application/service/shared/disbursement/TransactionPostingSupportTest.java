package ir.dotin.loan.trade.core.application.service.shared.disbursement;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway")
class TransactionPostingSupportTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private TransactionPostingPort transactionPostingPort;

    @Test
    void postTransactionsMapsTrackedNumbersToPostedTransactions() {
        TransactionPostingSupport support = new TransactionPostingSupport(transactionPostingPort, clock);
        TrackedTransactionNumber tracked =
                TrackedTransactionNumber.create("TX-1", "TRK-1", TransactionStatus.POSTED, clock);
        when(transactionPostingPort.postTransactions(any(), eq("doc"), anyList()))
                .thenReturn(Result.success(List.of(tracked)));

        Result<List<PostedTransaction>> result =
                support.postTransactions(LoanFacilityId.of(UUID.randomUUID()), "doc", List.of());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap()).containsExactly(new PostedTransaction("TX-1", "TRK-1", TransactionStatus.POSTED));
    }

    @Test
    void rebuildTrackedNumbersReturnsEmptyWhenNull() {
        TransactionPostingSupport support = new TransactionPostingSupport(transactionPostingPort, clock);

        assertThat(support.rebuildTrackedNumbers(null)).isEmpty();
    }

    @Test
    void rebuildTrackedNumbersRebuildsFromPosted() {
        TransactionPostingSupport support = new TransactionPostingSupport(transactionPostingPort, clock);

        List<TrackedTransactionNumber> tracked = support.rebuildTrackedNumbers(
                List.of(new PostedTransaction("TX-1", "TRK-1", TransactionStatus.POSTED)));

        assertThat(tracked).hasSize(1);
        assertThat(tracked.getFirst().value()).isEqualTo("TX-1");
    }

    @Test
    void reverseTransactionsDelegatesToPort() {
        TransactionPostingSupport support = new TransactionPostingSupport(transactionPostingPort, clock);
        when(transactionPostingPort.reverseTransactions(anyList())).thenReturn(Result.success(Unit.INSTANCE));

        Result<Unit> result = support.reverseTransactions(List.of());

        assertThat(result.isSuccess()).isTrue();
    }
}
