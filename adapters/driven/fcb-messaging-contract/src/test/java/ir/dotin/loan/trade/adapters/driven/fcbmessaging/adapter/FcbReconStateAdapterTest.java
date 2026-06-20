package ir.dotin.loan.trade.adapters.driven.fcbmessaging.adapter;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.client.FcbRequestReplyClient;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.config.FcbReconStateProperties;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconGuarantorReply;
import ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto.reply.ReconStateResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconGuarantor;
import ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice.ReconLoanFileState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Guarantor mapping in the FCB recon-state adapter (LN-59442 M1): the wire {@code guaranteePercent} string is parsed to
 * a {@link BigDecimal}, a guarantor with a blank customer number is dropped, and an unparseable percentage degrades to
 * an unknown ({@code null}) percentage rather than crashing the whole recon read.
 */
@ExtendWith(MockitoExtension.class)
class FcbReconStateAdapterTest {

    @Mock
    private FcbRequestReplyClient client;

    @Mock
    private FcbReconStateProperties properties;

    private FcbReconStateAdapter adapter;

    @BeforeEach
    void setUp() {
        when(properties.getReconStateTimeout()).thenReturn(Duration.ofSeconds(5));
        adapter = new FcbReconStateAdapter(client, properties);
    }

    @Test
    void mapsGuarantorsParsingPercentAndDroppingBlankCustomer() {
        ReconStateResponse response = new ReconStateResponse();
        response.setExists(true);
        response.setFileStatus("APPROVE_LOAN");
        response.setManualId("manual");
        response.setReachable(true);
        response.setGuarantors(List.of(
                guarantor("111", "60"),
                guarantor("222", "100.0000"),
                guarantor("  ", "40"), // blank customer → dropped
                guarantor("333", "not-a-number"))); // unparseable → null percentage
        when(client.sendAndReceive(any(), any())).thenReturn(Result.success(response));

        Result<ReconLoanFileState> result = adapter.loadReconState("fac-1", null);

        assertThat(result.isSuccess()).isTrue();
        List<ReconGuarantor> guarantors = result.unwrap().guarantors();
        assertThat(guarantors).hasSize(3); // the blank-customer one dropped
        assertThat(guarantors).contains(new ReconGuarantor("111", new BigDecimal("60")));
        assertThat(guarantors).contains(new ReconGuarantor("222", new BigDecimal("100.0000")));
        assertThat(guarantors).contains(new ReconGuarantor("333", null));
    }

    @Test
    void mapsEmptyGuarantorsWhenWireListNull() {
        ReconStateResponse response = new ReconStateResponse();
        response.setExists(true);
        response.setFileStatus("APPROVE_LOAN");
        response.setManualId("manual");
        response.setReachable(true);
        // guarantors left null (older FCB build)
        when(client.sendAndReceive(any(), any())).thenReturn(Result.success(response));

        Result<ReconLoanFileState> result = adapter.loadReconState("fac-1", null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.unwrap().guarantors()).isEmpty();
    }

    private static ReconGuarantorReply guarantor(String customerNumber, String percent) {
        ReconGuarantorReply reply = new ReconGuarantorReply();
        reply.setCustomerNumber(customerNumber);
        reply.setGuaranteePercent(percent);
        return reply;
    }
}
