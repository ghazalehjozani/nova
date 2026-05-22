package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.concurrent.ParallelFanout;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.PartyDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DependencyLoader {

    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;
    private final CustomerServicePort customerServicePort;

    @WithSpan("facility.dependencies.fanout")
    public Result<FacilityOriginationContext> loadDependencies(OriginateLoanFacilityCommand command) {
        log.debug("Loading dependencies for facility origination");

        AtomicReference<TradeLoanArrangement> arrangementRef = new AtomicReference<>();
        AtomicReference<TradeLoanType> loanTypeRef = new AtomicReference<>();

        Set<PartyDto> partySet = command.loanApplication().parties();
        List<PartyDto> parties = new ArrayList<>(partySet);
        int partyCount = parties.size();
        AtomicReferenceArray<PartyInfoResponse> partyRefs = new AtomicReferenceArray<>(partyCount);

        List<Supplier<Result<Unit>>> tasks = new ArrayList<>(partyCount + 2);

        tasks.add(() -> {
            Result<TradeLoanArrangement> r = safeLoadArrangement(command.loanArrangementCode());
            if (r.isFailure()) {
                return Result.failure(r.err().orElseThrow());
            }
            arrangementRef.set(r.unwrap());
            return Result.success();
        });

        tasks.add(() -> {
            Result<TradeLoanType> r = safeLoadLoanType(command.loanTypeCode());
            if (r.isFailure()) {
                return Result.failure(r.err().orElseThrow());
            }
            loanTypeRef.set(r.unwrap());
            return Result.success();
        });

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

        Result<Unit> fanout = ParallelFanout.allVoid(tasks);
        if (fanout.isFailure()) {
            return Result.failure(fanout.err().orElseThrow());
        }

        List<PartyInfoResponse> partiesValue = new ArrayList<>(partyCount);
        for (int i = 0; i < partyCount; i++) {
            PartyInfoResponse p = partyRefs.get(i);
            if (p != null) {
                partiesValue.add(p);
            }
        }

        log.debug("Successfully loaded all dependencies");
        return Result.success(new FacilityOriginationContext(arrangementRef.get(), loanTypeRef.get(), partiesValue));
    }

    private Result<TradeLoanArrangement> safeLoadArrangement(String code) {
        try {
            return Result.fromOptional(
                    loanArrangementRepository.findByCode(
                            LoanArrangementCode.valueOf(code).unwrap()),
                    () -> FailureCause.businessRule(
                            Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_ARRANGEMENT, code)));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.failure(OriginateLoanFacilityErrorCodes.INVALID_LOAN_ARRANGEMENT, code);
        }
    }

    private Result<TradeLoanType> safeLoadLoanType(String code) {
        try {
            return Result.fromOptional(
                    tradeLoanTypeRepository.findByCode(LoanTypeCode.of(code).unwrap()),
                    () -> FailureCause.businessRule(
                            Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_TYPE, code)));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.failure(OriginateLoanFacilityErrorCodes.INVALID_LOAN_TYPE, code);
        }
    }

    private Result<PartyInfoResponse> loadCustomerInfo(
            String customerNumber, @NotNull PartyRole role, @Nullable BigDecimal guaranteePercentage) {
        return customerServicePort.loadCustomerInfo(
                customerNumber, role, guaranteePercentage, CustomerInfoLoadOptions.baseInfoOnly());
    }
}
