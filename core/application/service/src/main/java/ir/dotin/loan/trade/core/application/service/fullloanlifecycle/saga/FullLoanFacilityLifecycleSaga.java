package ir.dotin.loan.trade.core.application.service.fullloanlifecycle.saga;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.Command;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.platform.dispatcher.api.execution.ExecutionResult;
import ir.dotin.platform.saga.api.annotation.SagaHandler;
import ir.dotin.platform.saga.api.context.SagaContext;
import ir.dotin.platform.saga.api.definition.SagaDefinition;
import ir.dotin.platform.saga.api.definition.SagaInput;
import ir.dotin.platform.saga.api.definition.SagaStep;
import ir.dotin.platform.saga.api.definition.SagaSteps;
import ir.dotin.platform.saga.api.model.StepError;
import ir.dotin.platform.saga.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.TransactionConfig;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateApprovalSubmissionCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateContractIssuanceCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateIrregularDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateLumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.CompensateOriginationCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.FullLoanFacilityLifecycleCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IrregularProgressiveDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.IssueFacilityContractCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.LumpSumDisbursementCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.SubmitFacilityForApprovalCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.fullloanlifecycle.i18n.FullLoanFacilityLifecycleErrorCodes;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityApproved;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCollateralAdded;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityCreated;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityIrregularTrancheDisbursed;
import ir.dotin.loan.trade.core.domain.loanfacility.event.TradeLoanFacilityLumpSumDisbursed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;

@Slf4j
@SagaHandler
@Component
@RequiredArgsConstructor
public class FullLoanFacilityLifecycleSaga implements SagaDefinition<FullLoanFacilityLifecycleSagaData> {

    private final CommandDispatcher dispatcher;
    private final TradeLoanTypeRepository loanTypeRepository;
    private final TradeLoanArrangementRepository arrangementRepository;

    @Override
    public String sagaType() {
        return "full-loan-facility-lifecycle";
    }

    @Override
    public List<SagaStep<FullLoanFacilityLifecycleSagaData, ?>> steps() {
        return List.of(
                SagaSteps.readOnlyStep(FullLoanFacilityLifecycleStep.VALIDATE_INPUT, this::validateInput)
                        .withNoRetry(),
                SagaSteps.step(
                                FullLoanFacilityLifecycleStep.ORIGINATE_FACILITY,
                                this::originateFacility,
                                this::compensateOrigination)
                        .withTimeout(Duration.ofSeconds(600))
                        .withNoRetry(),
                SagaSteps.step(
                                FullLoanFacilityLifecycleStep.SUBMIT_FOR_APPROVAL,
                                this::submitForApproval,
                                this::compensateApprovalSubmission)
                        .withNoRetry(),
                SagaSteps.step(
                                FullLoanFacilityLifecycleStep.APPROVE_FACILITY,
                                this::approveFacility,
                                this::compensateApproval)
                        .withNoRetry(),
                SagaSteps.step(
                                FullLoanFacilityLifecycleStep.ADD_COLLATERALS,
                                this::addCollaterals,
                                this::compensateCollaterals)
                        .withNoRetry(),
                SagaSteps.step(
                                FullLoanFacilityLifecycleStep.ISSUE_CONTRACT,
                                this::issueContract,
                                this::compensateContractIssuance)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(600)),
                SagaSteps.step(
                                FullLoanFacilityLifecycleStep.EXECUTE_DISBURSEMENT,
                                this::executeDisbursement,
                                this::compensateDisbursement)
                        .withConservativeRetry()
                        .withTimeout(Duration.ofSeconds(600)));
    }

    @Override
    public FullLoanFacilityLifecycleSagaData createInitialData(SagaInput input) {
        if (!(input
                instanceof
                FullLoanFacilityLifecycleInput(
                        OriginateLoanFacilityCommand originationCommand,
                        List<FullLoanFacilityLifecycleCommand.CollateralDto> collaterals,
                        BigDecimal trancheAmount,
                        String branchCode,
                        TransactionConfig transactionConfig,
                        DisbursementMethod disbursementMethod,
                        LocalDate disbursementDate,
                        UUID correlationId,
                        String confirmType))) {
            throw new IllegalArgumentException("Expected FullLoanFacilityLifecycleInput but got: " + input.getClass());
        }
        return FullLoanFacilityLifecycleSagaData.initial(
                originationCommand,
                collaterals,
                trancheAmount,
                branchCode,
                transactionConfig,
                disbursementMethod,
                disbursementDate,
                correlationId,
                confirmType);
    }

    private StepResult<Void> validateInput(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();

        if (data.disbursementMethod() == DisbursementMethod.REGULAR_PROGRESSIVE) {
            return new StepResult.Failure<>(new StepError.BusinessError(
                    "UNSUPPORTED_DISBURSEMENT_METHOD", "REGULAR_PROGRESSIVE not supported"));
        }

        var command = data.originationCommand();
        if (loanTypeRepository
                .findByCode(LoanTypeCode.of(command.loanTypeCode()).getValue())
                .isEmpty()) {
            return new StepResult.Failure<>(new StepError.BusinessRuleError(Notification.ofError(
                    FullLoanFacilityLifecycleErrorCodes.LOAN_TYPE_NOT_FOUND, command.loanTypeCode())));
        }

        if (arrangementRepository
                .findByCode(LoanArrangementCode.valueOf(command.loanArrangementCode())
                        .getValue())
                .isEmpty()) {
            return new StepResult.Failure<>(new StepError.BusinessRuleError(Notification.ofError(
                    FullLoanFacilityLifecycleErrorCodes.LOAN_ARRANGEMENT_NOT_FOUND, command.loanArrangementCode())));
        }

        return new StepResult.Success<>(null);
    }

    // ========== ORIGINATE ==========

    private StepResult<Void> originateFacility(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        log.info("Originating facility for correlation: {}", data.correlationId());

        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(data.originationCommand());

        return switch (result) {
            case ExecutionResult.Fresh<List<DomainEvent<?>>> fresh -> {
                var events = fresh.payload();
                extractFacilityId(events)
                        .ifPresent(fid ->
                                ctx.updateSagaData(d -> d.withFacilityId(fid).addDomainEvents(events)));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.Replayed<List<DomainEvent<?>>> replayed -> {
                ctx.updateSagaData(d -> d.addDomainEvents(replayed.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.BusinessFailure<List<DomainEvent<?>>> failure ->
                new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
        };
    }

    private StepResult<Void> compensateOrigination(SagaContext<FullLoanFacilityLifecycleSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();
        if (data.facilityId() == null) return new StepResult.Success<>(null);

        log.warn("Compensating origination for facility: {}", data.facilityId());
        var command = CompensateOriginationCommand.builder()
                .uid(data.correlationId())
                .loanFacilityId(data.facilityId())
                .build();

        return dispatchCompensation(command);
    }

    private StepResult<Void> submitForApproval(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        log.info("Submitting facility {} for approval", data.facilityId());

        var command = new SubmitFacilityForApprovalCommand(data.correlationId(), 1L, data.facilityId());
        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(command);

        return switch (result) {
            case ExecutionResult.Fresh<List<DomainEvent<?>>> fresh -> {
                ctx.updateSagaData(d -> d.addDomainEvents(fresh.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.Replayed<List<DomainEvent<?>>> replayed -> {
                ctx.updateSagaData(d -> d.addDomainEvents(replayed.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.BusinessFailure<List<DomainEvent<?>>> failure ->
                new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
        };
    }

    private StepResult<Void> compensateApprovalSubmission(
            SagaContext<FullLoanFacilityLifecycleSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();
        log.warn("Compensating approval submission for facility: {}", data.facilityId());

        var command = CompensateApprovalSubmissionCommand.builder()
                .uid(data.correlationId())
                .loanFacilityId(data.facilityId())
                .build();

        return dispatchCompensation(command);
    }

    private StepResult<Void> approveFacility(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        log.info("Approving facility: {}", data.facilityId());

        var command = ApproveFacilityCommand.builder()
                .uid(data.correlationId())
                .version(3L)
                .loanFacilityId(data.facilityId())
                .confirmType(data.confirmType())
                .build();

        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(command);

        return switch (result) {
            case ExecutionResult.Fresh<List<DomainEvent<?>>> fresh -> {
                var events = fresh.payload();
                extractSanctionedLoanId(events)
                        .ifPresent(slId -> ctx.updateSagaData(
                                d -> d.withSanctionedLoanId(slId).addDomainEvents(events)));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.Replayed<List<DomainEvent<?>>> replayed -> {
                ctx.updateSagaData(d -> d.addDomainEvents(replayed.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.BusinessFailure<List<DomainEvent<?>>> failure ->
                new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
        };
    }

    private StepResult<Void> compensateApproval(SagaContext<FullLoanFacilityLifecycleSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();
        log.warn("Compensating approval for facility: {}", data.facilityId());

        var command = CompensateApprovalCommand.builder()
                .uid(data.correlationId())
                .version(4L)
                .loanFacilityId(data.facilityId())
                .build();

        return dispatchCompensation(command);
    }

    private StepResult<Void> issueContract(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        var txConfig = data.transactionConfig();
        log.info("Issuing contract for facility: {}", data.facilityId());

        var command = IssueFacilityContractCommand.builder()
                .uid(data.correlationId())
                .version(5L)
                .loanFacilityId(data.facilityId())
                .branchCode(data.branchCode())
                .terminalType(txConfig.terminalType())
                .terminalIp(txConfig.terminalIp())
                .terminalId(txConfig.terminalId())
                .productCode(txConfig.productCode())
                .userId(txConfig.userId())
                .toolSource(txConfig.toolSource())
                .networkType(txConfig.networkType())
                .channel(txConfig.channel())
                .build();

        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(command);

        return switch (result) {
            case ExecutionResult.Fresh<List<DomainEvent<?>>> fresh -> {
                ctx.updateSagaData(d -> d.addDomainEvents(fresh.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.Replayed<List<DomainEvent<?>>> replayed -> {
                ctx.updateSagaData(d -> d.addDomainEvents(replayed.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.BusinessFailure<List<DomainEvent<?>>> failure ->
                new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
        };
    }

    private StepResult<Void> compensateContractIssuance(
            SagaContext<FullLoanFacilityLifecycleSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();
        log.warn("Compensating contract issuance for facility: {}", data.facilityId());

        var command = CompensateContractIssuanceCommand.builder()
                .uid(data.correlationId())
                .version(6L)
                .loanFacilityId(data.facilityId())
                .build();

        return dispatchCompensation(command);
    }

    private StepResult<Void> executeDisbursement(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        log.info("Executing {} disbursement for facility: {}", data.disbursementMethod(), data.facilityId());

        return switch (data.disbursementMethod()) {
            case LUMP_SUM -> executeLumpSumDisbursement(ctx);
            case IRREGULAR_PROGRESSIVE -> executeIrregularDisbursement(ctx);
            case REGULAR_PROGRESSIVE ->
                new StepResult.Failure<>(
                        new StepError.BusinessError("UNSUPPORTED", "REGULAR_PROGRESSIVE not supported"));
        };
    }

    private StepResult<Void> executeLumpSumDisbursement(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        var txConfig = data.transactionConfig();

        var command = LumpSumDisbursementCommand.builder()
                .uid(data.correlationId())
                .version(7L)
                .loanFacilityId(data.facilityId())
                .branchCode(data.branchCode())
                .terminalType(txConfig.terminalType())
                .terminalIp(txConfig.terminalIp())
                .terminalId(txConfig.terminalId())
                .productCode(txConfig.productCode())
                .userId(txConfig.userId())
                .toolSource(txConfig.toolSource())
                .networkType(txConfig.networkType())
                .channel(txConfig.channel())
                .disbursementDate(data.disbursementDate())
                .build();

        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(command);

        return switch (result) {
            case ExecutionResult.Fresh<List<DomainEvent<?>>> fresh -> {
                var events = fresh.payload();
                extractLumpSumScheduleId(events)
                        .ifPresent(scheduleId -> ctx.updateSagaData(
                                d -> d.withInstallmentScheduleId(scheduleId).addDomainEvents(events)));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.Replayed<List<DomainEvent<?>>> replayed -> {
                ctx.updateSagaData(d -> d.addDomainEvents(replayed.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.BusinessFailure<List<DomainEvent<?>>> failure ->
                new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
        };
    }

    private StepResult<Void> executeIrregularDisbursement(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        var txConfig = data.transactionConfig();
        var origCmd = data.originationCommand();

        var command = IrregularProgressiveDisbursementCommand.builder()
                .uid(data.correlationId())
                .loanFacilityId(data.facilityId())
                .trancheAmount(data.trancheAmount())
                .version(7L)
                .branchCode(data.branchCode())
                .terminalType(txConfig.terminalType())
                .terminalIp(txConfig.terminalIp())
                .terminalId(txConfig.terminalId())
                .productCode(txConfig.productCode())
                .userId(txConfig.userId())
                .toolSource(txConfig.toolSource())
                .networkType(txConfig.networkType())
                .channel(txConfig.channel())
                .installmentSchedulePlan(mapInstallmentPlan(origCmd))
                .disbursementDate(data.disbursementDate())
                .build();

        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(command);

        return switch (result) {
            case ExecutionResult.Fresh<List<DomainEvent<?>>> fresh -> {
                var events = fresh.payload();
                extractIrregularScheduleIds(events, ctx);
                ctx.updateSagaData(d -> d.addDomainEvents(events));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.Replayed<List<DomainEvent<?>>> replayed -> {
                ctx.updateSagaData(d -> d.addDomainEvents(replayed.payload()));
                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.BusinessFailure<List<DomainEvent<?>>> failure ->
                new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
        };
    }

    private IrregularProgressiveDisbursementCommand.InstallmentSchedulePlanDto mapInstallmentPlan(
            OriginateLoanFacilityCommand origCmd) {
        if (origCmd.installmentSchedulePlan() == null) return null;

        var specs = origCmd.installmentSchedulePlan().installments().stream()
                .map(spec -> IrregularProgressiveDisbursementCommand.InstallmentSpecDto.builder()
                        .sequenceNumber(spec.sequenceNumber())
                        .dueDate(spec.dueDate())
                        .principalAmount(spec.principalAmount())
                        .interestAmount(spec.interestAmount())
                        .build())
                .toList();

        return IrregularProgressiveDisbursementCommand.InstallmentSchedulePlanDto.builder()
                .installments(specs)
                .build();
    }

    private StepResult<Void> compensateDisbursement(SagaContext<FullLoanFacilityLifecycleSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();
        log.warn("Compensating disbursement for facility: {}", data.facilityId());

        return switch (data.disbursementMethod()) {
            case LUMP_SUM -> compensateLumpSum(ctx);
            case IRREGULAR_PROGRESSIVE -> compensateIrregular(ctx);
            case REGULAR_PROGRESSIVE -> new StepResult.Success<>(null);
        };
    }

    private StepResult<Void> compensateLumpSum(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        var command = CompensateLumpSumDisbursementCommand.builder()
                .uid(data.correlationId())
                .version(7L)
                .loanFacilityId(data.facilityId())
                .build();

        return dispatchCompensation(command);
    }

    private StepResult<Void> compensateIrregular(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();
        if (data.installmentScheduleId() == null || data.previousInstallmentScheduleId() == null) {
            return new StepResult.Success<>(null);
        }

        var command = CompensateIrregularDisbursementCommand.builder()
                .uid(data.correlationId())
                .version(8L)
                .loanFacilityId(requireNonNull(data.facilityId()))
                .installmentScheduleId(data.installmentScheduleId())
                .previousInstallmentScheduleId(data.previousInstallmentScheduleId())
                .build();

        return dispatchCompensation(command);
    }

    private <C extends Record & Command> StepResult<Void> dispatchCompensation(C command) {
        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(command);
        return switch (result) {
            case ExecutionResult.Fresh<?> ignored -> new StepResult.Success<>(null);
            case ExecutionResult.Replayed<?> ignored -> new StepResult.Success<>(null);
            case ExecutionResult.BusinessFailure<?> failure ->
                new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
        };
    }

    private Optional<UUID> extractFacilityId(List<DomainEvent<?>> events) {
        return events.stream()
                .filter(TradeLoanFacilityCreated.class::isInstance)
                .map(e -> ((TradeLoanFacilityCreated) e).aggregateId())
                .findFirst();
    }

    private Optional<UUID> extractSanctionedLoanId(List<DomainEvent<?>> events) {
        return events.stream()
                .filter(TradeLoanFacilityApproved.class::isInstance)
                .map(e -> ((TradeLoanFacilityApproved) e).sanctionedLoanId())
                .findFirst();
    }

    private Optional<UUID> extractLumpSumScheduleId(List<DomainEvent<?>> events) {
        return events.stream()
                .filter(TradeLoanFacilityLumpSumDisbursed.class::isInstance)
                .map(e -> ((TradeLoanFacilityLumpSumDisbursed) e).installmentScheduleId())
                .findFirst();
    }

    private void extractIrregularScheduleIds(
            List<DomainEvent<?>> events, SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        events.stream()
                .filter(TradeLoanFacilityIrregularTrancheDisbursed.class::isInstance)
                .map(e -> (TradeLoanFacilityIrregularTrancheDisbursed) e)
                .findFirst()
                .ifPresent(e -> ctx.updateSagaData(d -> d.withPreviousInstallmentScheduleId(d.installmentScheduleId())
                        .withInstallmentScheduleId(e.installmentScheduleId())));
    }

    private StepResult<Void> addCollaterals(SagaContext<FullLoanFacilityLifecycleSagaData> ctx) {
        var data = ctx.getSagaData();

        if (!data.hasCollaterals()) {
            return new StepResult.Success<>(null);
        }

        log.info("Adding {} collaterals to facility: {}", data.collaterals().size(), data.facilityId());

        List<AddFacilityCollateralCommand.CollateralDto> collateralDtos = data.collaterals().stream()
                .map(c -> AddFacilityCollateralCommand.CollateralDto.builder()
                        .collateralTypeCode(c.collateralTypeCode())
                        .percent(c.percent())
                        .description(c.description())
                        .collateralSerial(c.collateralSerial())
                        .usedAmount(new AddFacilityCollateralCommand.MoneyDto(
                                c.usedAmount().value(), c.usedAmount().currency()))
                        .build())
                .toList();

        var command = AddFacilityCollateralCommand.builder()
                .uid(data.correlationId())
                .version(4L)
                .loanFacilityId(data.facilityId())
                .collaterals(collateralDtos)
                .build();

        ExecutionResult<List<DomainEvent<?>>> result = dispatcher.dispatch(command);

        return switch (result) {
            case ExecutionResult.Fresh<List<DomainEvent<?>>> fresh -> {
                var events = fresh.payload();
                extractAddedCollateralSerials(events).ifPresent(serials -> {
                    ctx.updateSagaData(
                            d -> d.withAddedCollateralSerials(serials).addDomainEvents(events));
                    log.info("Successfully added {} collaterals to facility: {}", serials.size(), data.facilityId());
                });

                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.Replayed<List<DomainEvent<?>>> replayed -> {
                var events = replayed.payload();

                extractAddedCollateralSerials(events).ifPresent(serials -> {
                    ctx.updateSagaData(
                            d -> d.withAddedCollateralSerials(serials).addDomainEvents(events));
                });

                yield new StepResult.Success<>(null);
            }
            case ExecutionResult.BusinessFailure<List<DomainEvent<?>>> failure -> {
                log.warn("Failed to add collaterals: {}", failure.notification());
                yield new StepResult.Failure<>(new StepError.BusinessRuleError(failure.notification()));
            }
        };
    }

    private Optional<List<String>> extractAddedCollateralSerials(List<DomainEvent<?>> events) {
        return events.stream()
                .filter(TradeLoanFacilityCollateralAdded.class::isInstance)
                .map(e -> ((TradeLoanFacilityCollateralAdded) e).collateralSerials())
                .findFirst();
    }

    private StepResult<Void> compensateCollaterals(SagaContext<FullLoanFacilityLifecycleSagaData> ctx, Void ignored) {
        var data = ctx.getSagaData();

        if (!data.hasAddedCollaterals()) {
            return new StepResult.Success<>(null);
        }

        log.warn(
                "Compensating {} collateral additions for facility: {}",
                data.getAddedCollateralCount(),
                data.facilityId());

        var command = CompensateCollateralCommand.builder()
                .uid(data.correlationId())
                .version(5L)
                .loanFacilityId(data.facilityId())
                .collateralSerials(data.addedCollateralSerials())
                .build();

        return dispatchCompensation(command);
    }
}
