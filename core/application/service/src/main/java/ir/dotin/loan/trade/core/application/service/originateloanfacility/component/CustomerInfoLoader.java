package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.PartyDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads FCB customer-info for the command's parties via {@link CustomerServicePort}. Extracted from
 * {@code DependencyLoader} so the tx-free origination pre-flight can reuse the exact same loading logic — same party
 * order, same {@link CustomerInfoLoadOptions#baseInfoOnly()} option, same per-party error handling and parallel fanout
 * — without dragging in the DB-only arrangement/loan-type lookups.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerInfoLoader {

    private final CustomerServicePort customerServicePort;

    @WithSpan("facility.customerinfo.fanout")
    public Result<List<PartyInfoResponse>> loadPartyInfos(OriginateLoanFacilityCommand command) {
        log.debug("Loading customer-info for facility origination parties");

        Set<PartyDto> partySet = command.loanApplication().parties();
        List<PartyDto> parties = new ArrayList<>(partySet);
        int partyCount = parties.size();
        AtomicReferenceArray<@Nullable PartyInfoResponse> partyRefs = new AtomicReferenceArray<>(partyCount);

        List<Supplier<Result<Unit>>> tasks = getTasks(partyCount, parties, partyRefs);

        Result<Unit> fanout = ParallelFanout.allVoid(tasks);
        if (fanout.isFailure()) {
            return Result.failure(fanout.err().orElseThrow());
        }

        List<PartyInfoResponse> partyInfos = new ArrayList<>(partyCount);
        for (int i = 0; i < partyCount; i++) {
            PartyInfoResponse p = partyRefs.get(i);
            if (p != null) {
                partyInfos.add(p);
            }
        }

        log.debug("Successfully loaded customer-info for {} parties", partyInfos.size());
        return Result.success(partyInfos);
    }

    private List<Supplier<Result<Unit>>> getTasks(
            int partyCount, List<PartyDto> parties, AtomicReferenceArray<@Nullable PartyInfoResponse> partyRefs) {
        List<Supplier<Result<Unit>>> tasks = new ArrayList<>(partyCount);
        for (int i = 0; i < partyCount; i++) {
            final int idx = i;
            final PartyDto partyDto = parties.get(i);
            tasks.add(() -> {
                BigDecimal percentage =
                        partyDto instanceof PartyDto.GuarantorDto guarantor ? guarantor.guaranteePercentage() : null;
                Result<PartyInfoResponse> r = loadCustomerInfo(partyDto.customerNumber(), partyDto.role(), percentage);
                if (r.isFailure()) {
                    return Result.failure(r.err().orElseThrow());
                }
                partyRefs.set(idx, r.unwrap());
                return Result.success();
            });
        }
        return tasks;
    }

    private Result<PartyInfoResponse> loadCustomerInfo(
            String customerNumber, @NotNull PartyRole role, @Nullable BigDecimal guaranteePercentage) {
        return customerServicePort.loadCustomerInfo(
                customerNumber, role, guaranteePercentage, CustomerInfoLoadOptions.baseInfoOnly());
    }
}
