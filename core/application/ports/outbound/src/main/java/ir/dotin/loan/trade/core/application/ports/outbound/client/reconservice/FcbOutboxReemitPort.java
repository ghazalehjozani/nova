package ir.dotin.loan.trade.core.application.ports.outbound.client.reconservice;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.servicelayer.api.port.RemoteIdempotentWritePort;

public interface FcbOutboxReemitPort extends RemoteIdempotentWritePort {

    Result<ReconReemitOutcome> reemitOutbox(String facilityId, @Nullable String outboxRef);
}
