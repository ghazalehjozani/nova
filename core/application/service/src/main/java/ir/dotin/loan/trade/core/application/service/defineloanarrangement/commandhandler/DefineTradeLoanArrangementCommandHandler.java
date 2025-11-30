package ir.dotin.loan.trade.core.application.service.defineloanarrangement.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.service.defineloanarrangement.i18n.DefineLoanArrangementErrorCodes;
import ir.dotin.loan.trade.core.application.service.defineloanarrangement.mapper.DefineTradeLoanArrangementCommandMapper;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefineTradeLoanArrangementCommandHandler implements CommandHandler<DefineTradeLoanArrangementCommand> {

    private final DefineTradeLoanArrangementCommandMapper mapper;
    private final TradeLoanArrangementRepository repository;
    private final Clock clock;
    private final LoanServicePort loanServicePort;

    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public Result<List<DomainEvent<?>>> handle(DefineTradeLoanArrangementCommand command) {

        CompletableFuture<Result<EconomicSector>> economicSectorFuture = CompletableFuture.supplyAsync(
                () -> loadEconomicSector(mapper.map(command.economicSector())), VIRTUAL_EXECUTOR);

        return Result.requireFalse(
                        repository.existsByCode(LoanArrangementCode.valueOf(command.code().value()).getValue()),
                        Notification.ofError(
                                DefineLoanArrangementErrorCodes.DUPLICATE_CODE,
                                command.code().value()))
                .flatMap(ignored -> {
                    Result<EconomicSector> economicSectorResult = economicSectorFuture.join();
                    return economicSectorResult.flatMap(validatedSector -> Result.success(mapper.toBuilder(command))
                            .flatMap(builder -> TradeLoanArrangement.create(builder, clock)));
                })
                .peekValue(arrangement -> {
                    repository.save(arrangement);
                    log.info("Successfully established trade loan arrangement with ID: {}", arrangement.getId());
                })
                .mapNonNull(TradeLoanArrangement::domainEvents);
    }

    private Result<EconomicSector> loadEconomicSector(EconomicSector economicSector) {
        return loanServicePort.loadEconomicalSectorByCode(economicSector);
    }
}
