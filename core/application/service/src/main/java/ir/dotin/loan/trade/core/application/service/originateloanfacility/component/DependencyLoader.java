package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.PartyDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DependencyLoader {

    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;
    private final CustomerServicePort customerServicePort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public Result<FacilityOriginationContext> loadDependencies(OriginateLoanFacilityCommand command) {
        log.debug("Loading dependencies for facility origination");

        var arrangementFuture = CompletableFuture.supplyAsync(
                () -> safeLoadArrangement(command.loanArrangementCode()), VIRTUAL_EXECUTOR);

        var loanTypeFuture =
                CompletableFuture.supplyAsync(() -> safeLoadLoanType(command.loanTypeCode()), VIRTUAL_EXECUTOR);

        var partyFutures = command.loanApplication().parties().stream()
                .map(partyDto -> CompletableFuture.supplyAsync(
                        () -> {
                            BigDecimal percentage = partyDto instanceof PartyDto.GuarantorDto guarantor
                                    ? guarantor.guaranteePercentage()
                                    : null;
                            return loadCustomerInfo(partyDto.customerNumber(), partyDto.role(), percentage);
                        },
                        VIRTUAL_EXECUTOR))
                .toList();

        CompletableFuture.allOf(Stream.concat(Stream.of(arrangementFuture, loanTypeFuture), partyFutures.stream())
                        .toArray(CompletableFuture[]::new))
                .join();

        var arrangementResult = arrangementFuture.join();
        var loanTypeResult = loanTypeFuture.join();
        var partiesResult = aggregatePartyResults(partyFutures);

        var notification = Notification.create()
                .merge(arrangementResult.notification())
                .merge(loanTypeResult.notification())
                .merge(partiesResult.notification());

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        log.debug("Successfully loaded all dependencies");
        return Result.success(new FacilityOriginationContext(
                arrangementResult.value(), loanTypeResult.value(), partiesResult.value()));
    }

    private Result<TradeLoanArrangement> safeLoadArrangement(String code) {
        try {
            return Result.fromOptional(
                    loanArrangementRepository.findByCode(
                            LoanArrangementCode.valueOf(code).getValue()),
                    Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_ARRANGEMENT, code));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.failure(Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_ARRANGEMENT, code));
        }
    }

    private Result<TradeLoanType> safeLoadLoanType(String code) {
        try {
            return Result.fromOptional(
                    tradeLoanTypeRepository.findByCode(LoanTypeCode.of(code).getValue()),
                    Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_TYPE, code));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Result.failure(Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_TYPE, code));
        }
    }

    private Result<PartyInfo> loadCustomerInfo(
            String customerNumber, @NotNull PartyRole role, @Nullable BigDecimal guaranteePercentage) {
        return customerServicePort.loadCustomerInfo(
                customerNumber, role, guaranteePercentage, CustomerInfoLoadOptions.baseInfoOnly());
    }

    private Result<List<PartyInfo>> aggregatePartyResults(List<CompletableFuture<Result<PartyInfo>>> futures) {
        var parties = new ArrayList<PartyInfo>(futures.size());
        var notification = Notification.create();

        for (var future : futures) {
            var result = future.join();
            notification.merge(result.notification());
            if (result.hasValue()) {
                parties.add(result.value());
            }
        }

        return notification.hasErrors() ? Result.failure(notification) : Result.success(parties);
    }
}
