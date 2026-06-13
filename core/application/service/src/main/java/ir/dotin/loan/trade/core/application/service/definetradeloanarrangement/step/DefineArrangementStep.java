package ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.step;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.PublishingWriteActivity;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.trade.core.application.ports.inbound.command.DefineTradeLoanArrangementCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.service.definetradeloanarrangement.mapper.DefineTradeLoanArrangementCommandMapper;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefineArrangementStep implements PublishingWriteActivity<DefineArrangementData> {

    private final DefineTradeLoanArrangementCommandMapper mapper;
    private final TradeLoanArrangementRepository repository;
    private final Clock clock;

    @Override
    public StepResult<List<DomainEvent<?>>> execute(WorkflowContext<DefineArrangementData> ctx) {
        return StepResult.fromWriteResult(write(ctx.data().command()));
    }

    private Result<List<DomainEvent<?>>> write(DefineTradeLoanArrangementCommand command) {
        return Result.success(mapper.toBuilder(command))
                .flatMap(builder -> TradeLoanArrangement.create(builder, clock))
                .onSuccess(arrangement -> {
                    repository.save(arrangement);
                    log.info("Successfully established trade loan arrangement with ID: {}", arrangement.getId());
                })
                .map(TradeLoanArrangement::domainEvents);
    }
}
