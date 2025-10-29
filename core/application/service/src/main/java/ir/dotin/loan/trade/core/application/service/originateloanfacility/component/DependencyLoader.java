package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
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

        CompletableFuture<Result<TradeLoanArrangement>> arrangementFuture =
                CompletableFuture.supplyAsync(() -> loadArrangement(command.loanArrangementId()), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<TradeLoanType>> loanTypeFuture =
                CompletableFuture.supplyAsync(() -> loadLoanType(command.loanTypeId()), VIRTUAL_EXECUTOR);

        CompletableFuture<Result<PartyInfo>> mainCustomerFuture = CompletableFuture.supplyAsync(
                () -> loadCustomerInfo(command.loanApplication().customer().customerNumber()), VIRTUAL_EXECUTOR);

        List<CompletableFuture<Result<PartyInfo>>> guarantorFutures = command.loanApplication().guarantors().stream()
                .map(guarantor -> CompletableFuture.supplyAsync(
                        () -> loadCustomerInfo(guarantor.customerNumber()), VIRTUAL_EXECUTOR))
                .toList();

        CompletableFuture.allOf(Stream.concat(
                                Stream.of(arrangementFuture, loanTypeFuture, mainCustomerFuture),
                                guarantorFutures.stream())
                        .toArray(CompletableFuture[]::new))
                .join();

        return arrangementFuture.join().flatMap(arrangement -> loanTypeFuture
                .join()
                .flatMap(loanType -> mainCustomerFuture.join().flatMap(mainCustomer -> collectGuarantors(
                                guarantorFutures)
                        .map(guarantors -> {
                            log.debug("Successfully loaded all dependencies");
                            return new FacilityOriginationContext(arrangement, loanType, mainCustomer, guarantors);
                        }))));
    }

    private Result<TradeLoanArrangement> loadArrangement(UUID arrangementId) {
        return Result.fromOptional(
                loanArrangementRepository.findById(LoanArrangementId.of(arrangementId)),
                Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_ARRANGEMENT, arrangementId));
    }

    private Result<TradeLoanType> loadLoanType(UUID loanTypeId) {
        return Result.fromOptional(
                tradeLoanTypeRepository.findById(LoanTypeId.of(loanTypeId)),
                Notification.ofError(OriginateLoanFacilityErrorCodes.INVALID_LOAN_TYPE, loanTypeId));
    }

    private Result<PartyInfo> loadCustomerInfo(String customerNumber) {
        return customerServicePort.loadCustomerInfo(customerNumber, CustomerInfoLoadOptions.baseInfoOnly());
    }

    private Result<List<PartyInfo>> collectGuarantors(List<CompletableFuture<Result<PartyInfo>>> guarantorFutures) {
        List<PartyInfo> guarantors = new ArrayList<>();
        Notification aggregatedNotification = Notification.create();

        for (CompletableFuture<Result<PartyInfo>> future : guarantorFutures) {
            Result<PartyInfo> result = future.join();
            aggregatedNotification.merge(result.notification());
            if (result.hasValue()) {
                guarantors.add(result.value());
            }
        }

        return aggregatedNotification.hasErrors()
                ? Result.failure(aggregatedNotification)
                : Result.of(guarantors, aggregatedNotification);
    }
}
