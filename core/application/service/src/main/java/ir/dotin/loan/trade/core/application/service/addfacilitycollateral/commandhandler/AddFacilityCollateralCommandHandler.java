package ir.dotin.loan.trade.core.application.service.addfacilitycollateral.commandhandler;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.domain.event.DomainEvent;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.workflow.api.command.WorkflowCommandHandler;
import ir.dotin.platform.pangaea.workflow.api.context.WorkflowContext;
import ir.dotin.platform.pangaea.workflow.api.definition.Step;
import ir.dotin.platform.pangaea.workflow.api.definition.Steps;
import ir.dotin.platform.pangaea.workflow.api.definition.Workflow;
import ir.dotin.platform.pangaea.workflow.api.engine.WorkflowEngine;
import ir.dotin.platform.pangaea.workflow.api.model.RetryPolicy;
import ir.dotin.platform.pangaea.workflow.api.model.StepResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.service.validator.AbstractCollateralValidationService;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.AddFacilityCollateralCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.CollateralDetails;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.AddFacilityCollateralDependencyLoader;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.component.CollateralValidationContext;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.mapper.AddFacilityCollateralCommandMapper;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.AddFacilityCollateralStep;
import ir.dotin.loan.trade.core.application.service.addfacilitycollateral.workflow.CollateralData;
import ir.dotin.loan.trade.core.application.service.shared.authz.BranchAccessValidator;
import ir.dotin.loan.trade.core.application.service.shared.error.TradeLoanApplicationServiceErrors;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.TradeLoanFacilityService;

@Service
public class AddFacilityCollateralCommandHandler
        extends WorkflowCommandHandler<AddFacilityCollateralCommand, CollateralData> {

    private static final Logger log = LoggerFactory.getLogger(AddFacilityCollateralCommandHandler.class);

    private static final Integer RESERVE_DURATION_MINUTES = 1440;

    private final AddFacilityCollateralCommandMapper mapper;
    private final AddFacilityCollateralDependencyLoader dependencyLoader;
    private final BranchAccessValidator branchAccessValidator;
    private final TradeLoanFacilityRepository facilityRepository;
    private final TradeLoanArrangementRepository arrangementRepository;
    private final TradeLoanFacilityService domainService;
    private final AbstractCollateralValidationService collateralValidationService;
    private final CollateralServicePort collateralServicePort;
    private final Clock clock;

    private final Workflow<CollateralData> workflow;

    public AddFacilityCollateralCommandHandler(
            WorkflowEngine engine,
            AddFacilityCollateralCommandMapper mapper,
            AddFacilityCollateralDependencyLoader dependencyLoader,
            BranchAccessValidator branchAccessValidator,
            TradeLoanFacilityRepository facilityRepository,
            TradeLoanArrangementRepository arrangementRepository,
            TradeLoanFacilityService domainService,
            AbstractCollateralValidationService collateralValidationService,
            CollateralServicePort collateralServicePort,
            Clock clock) {
        super(engine);
        this.mapper = mapper;
        this.dependencyLoader = dependencyLoader;
        this.branchAccessValidator = branchAccessValidator;
        this.facilityRepository = facilityRepository;
        this.arrangementRepository = arrangementRepository;
        this.domainService = domainService;
        this.collateralValidationService = collateralValidationService;
        this.collateralServicePort = collateralServicePort;
        this.clock = clock;
        this.workflow = buildWorkflow();
    }

    @Override
    protected Workflow<CollateralData> workflow() {
        return workflow;
    }

    @Override
    protected Result<CollateralData> seed(AddFacilityCollateralCommand command) {
        LoanFacilityId loanFacilityId = LoanFacilityId.of(command.loanFacilityId());

        List<Collateral> collaterals = mapper.toCollaterals(command.collaterals());

        Result<CollateralValidationContext> contextResult =
                dependencyLoader.loadAndCalculate(loanFacilityId, collaterals);
        if (contextResult.isFailure()) {
            return Result.failure(contextResult.err().orElseThrow());
        }
        CollateralValidationContext context = contextResult.unwrap();

        Result<Unit> branchResult =
                branchAccessValidator.verifyCallerCoversFacility(command.branchCode(), context.facility());
        if (branchResult.isFailure()) {
            return Result.failure(branchResult.err().orElseThrow());
        }

        Money requiredAmount = Objects.requireNonNull(context).requiredCollateralAmount();

        Result<Unit> adequacyResult = validateCollateralAdequacy(collaterals, context);
        if (adequacyResult.isFailure()) {
            return Result.failure(adequacyResult.err().orElseThrow());
        }

        Money totalNewCollateralAmount = collaterals.stream()
                .map(Collateral::usedAmount)
                .reduce(Money.zero(context.arrangement().getCurrencyType()).unwrap(), (a, b) -> a.add(b)
                        .unwrap());

        Result<Unit> valueValidationResult = validateTotalCollateralValue(totalNewCollateralAmount, requiredAmount);
        if (valueValidationResult.isFailure()) {
            return Result.failure(valueValidationResult.err().orElseThrow());
        }

        return Result.success(CollateralData.initial(
                command.loanFacilityId(), command.uid(), command.collaterals(), command.version()));
    }

    private Workflow<CollateralData> buildWorkflow() {
        return new Workflow<>() {
            @Override
            public String workflowType() {
                return "add-facility-collateral";
            }

            @Override
            public List<Step<CollateralData>> steps() {
                return List.of(
                        Steps.<CollateralData>remote(
                                        AddFacilityCollateralStep.RESERVE_COLLATERALS,
                                        AddFacilityCollateralCommandHandler.this::reserveCollaterals)
                                .retry(RetryPolicy.CONSERVATIVE)
                                .timeout(Duration.ofSeconds(30))
                                .compensatedBy(AddFacilityCollateralCommandHandler.this::unReserveCollaterals)
                                .build(),
                        Steps.<CollateralData>write(
                                        AddFacilityCollateralStep.ADD_COLLATERAL,
                                        AddFacilityCollateralCommandHandler.this::addCollateral)
                                .compensatedBy(AddFacilityCollateralCommandHandler.this::revertAddCollateral)
                                .build());
            }
        };
    }

    private StepResult<Void> reserveCollaterals(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.failure(facilityResult.err().orElseThrow());
        }
        var facility = facilityResult.unwrap();

        if (facility.getLoanApplication().getApplicationNumber().isEmpty()) {
            return StepResult.failure(FailureCause.businessRule(
                    Notification.ofError(TradeLoanApplicationServiceErrors.APPLICATION_NUMBER_MISSING)));
        }
        ApplicationNumber appNumber =
                facility.getLoanApplication().getApplicationNumber().get();

        List<Collateral> collaterals = mapper.toCollaterals(data.collaterals());
        List<String> reserved = new ArrayList<>();

        for (Collateral collateral : collaterals) {
            Result<List<CollateralSerial>> result = collateralServicePort.reserveCollateral(
                    collateral.collateralSerial(),
                    appNumber,
                    data.requestId(),
                    RESERVE_DURATION_MINUTES,
                    collateral.usedAmount());

            if (result.isFailure()) {
                unReserve(appNumber, reserved, data.requestId());
                return StepResult.failure(result.err().orElseThrow());
            }
            reserved.add(collateral.collateralSerial().value());
        }

        ctx.updateData(d -> d.withReservedSerials(reserved));
        log.info("Reserved {} collaterals for facility: {}", reserved.size(), data.facilityId());
        return new StepResult.Success<>(null);
    }

    private StepResult<Void> unReserveCollaterals(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();
        List<String> reservedSerials = data.reservedSerials() == null ? List.of() : data.reservedSerials();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }
        var facility = facilityResult.unwrap();

        if (facility.getLoanApplication().getApplicationNumber().isEmpty()) {
            log.warn("No application number for facility {}; skipping un-reserve", data.facilityId());
            return new StepResult.Success<>(null);
        }
        ApplicationNumber appNumber =
                facility.getLoanApplication().getApplicationNumber().get();

        unReserve(appNumber, reservedSerials, data.requestId());
        log.warn("Un-reserved {} collaterals for facility: {}", reservedSerials.size(), data.facilityId());
        return new StepResult.Success<>(null);
    }

    private StepResult<List<DomainEvent<?>>> addCollateral(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();

        var result = loadFacility(LoanFacilityId.of(data.facilityId())).flatMap(facility -> loadArrangement(facility)
                .flatMap(arrangement -> {
                    List<Collateral> collaterals = mapper.toCollaterals(data.collaterals());
                    return domainService
                            .addCollateral(facility, collaterals)
                            .flatMap(ignored ->
                                    collateralValidationService.validateFacilityCollaterals(facility, arrangement))
                            .map(ignored -> {
                                List<DomainEvent<?>> events = List.copyOf(facility.domainEvents());
                                facilityRepository.save(facility, data.expectedVersion());
                                log.info(
                                        "{} collaterals added for facility: {}",
                                        collaterals.size(),
                                        data.facilityId());
                                return events;
                            });
                }));

        return StepResult.fromWriteResult(result);
    }

    private StepResult<Void> revertAddCollateral(WorkflowContext<CollateralData> ctx) {
        var data = ctx.data();

        var facilityResult = loadFacility(LoanFacilityId.of(data.facilityId()));
        if (facilityResult.isFailure()) {
            return StepResult.fromResult(facilityResult);
        }
        var facility = facilityResult.unwrap();

        List<String> serials = data.collaterals().stream()
                .map(AddFacilityCollateralCommand.CollateralDto::collateralSerial)
                .toList();

        var revertResult = facility.revertAddCollateral(serials, clock);
        if (revertResult.isFailure()) {
            return StepResult.fromResult(revertResult);
        }

        facilityRepository.save(facility);
        log.warn("Reverted add-collateral for facility: {}", data.facilityId());
        return new StepResult.Success<>(null);
    }

    private void unReserve(ApplicationNumber appNumber, List<String> serials, UUID requestId) {
        for (String serial : serials) {
            collateralServicePort.unReserveCollateral(
                    CollateralSerial.of(serial).unwrap(), appNumber, UUID.randomUUID(), requestId);
        }
    }

    private Result<TradeLoanFacility> loadFacility(LoanFacilityId loanFacilityId) {
        return Result.fromOptional(
                facilityRepository.findById(loanFacilityId),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.FACILITY_NOT_FOUND, loanFacilityId.value())));
    }

    private Result<TradeLoanArrangement> loadArrangement(TradeLoanFacility facility) {
        return Result.fromOptional(
                arrangementRepository.findById(facility.getLoanArrangementId()),
                () -> FailureCause.notFound(Notification.ofError(
                        TradeLoanApplicationServiceErrors.LOAN_ARRANGEMENT_NOT_FOUND,
                        facility.getLoanArrangementId(),
                        facility.getId().value())));
    }

    private Result<Unit> validateCollateralAdequacy(List<Collateral> collaterals, CollateralValidationContext context) {
        for (Collateral collateral : collaterals) {
            CollateralDetails details = context.collateralDetailsMap().get(collateral.collateralSerial());
            if (details == null) {
                return Result.failure(Notification.ofError(
                        TradeLoanApplicationServiceErrors.COLLATERAL_DETAILS_NOT_FOUND,
                        collateral.collateralSerial().value()));
            }

            Money realCollateralPrice = Money.valueOf(
                            details.price(), context.arrangement().getCurrencyType())
                    .unwrap();

            if (collateral.usedAmount().isGreaterThan(realCollateralPrice).unwrap()) {
                log.warn(
                        "Collateral adequacy validation failed for serial {}",
                        collateral.collateralSerial().value());
                return Result.failure(
                        TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE,
                        realCollateralPrice,
                        collateral.usedAmount());
            }
        }
        return Result.success();
    }

    private Result<Unit> validateTotalCollateralValue(Money totalValue, Money requiredAmount) {
        if (totalValue.isLessThan(requiredAmount).unwrap()) {
            log.warn("Total new collateral value {} is less than required amount {}", totalValue, requiredAmount);
            return Result.failure(
                    TradeLoanApplicationServiceErrors.INSUFFICIENT_COLLATERAL_VALUE, totalValue, requiredAmount);
        }
        return Result.success();
    }
}
