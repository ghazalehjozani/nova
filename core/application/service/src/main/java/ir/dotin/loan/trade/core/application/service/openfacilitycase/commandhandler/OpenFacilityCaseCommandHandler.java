package ir.dotin.loan.trade.core.application.service.openfacilitycase.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.platform.dispatcher.api.dispatcher.CommandDispatcher;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OpenFacilityCaseCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanArrangementRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanTypeRepository;
import ir.dotin.loan.trade.core.application.service.openfacilitycase.i18n.OpenFacilityCaseErrorCodes;
import ir.dotin.loan.trade.core.application.service.openfacilitycase.mapper.OpenFacilityCaseLoanApplicationMapper;
import ir.dotin.loan.trade.core.domain.loanarrangement.entity.TradeLoanArrangement;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;
import ir.dotin.loan.trade.core.domain.loanfacility.service.validator.TradeLoanFacilityValidationService;
import ir.dotin.loan.trade.core.domain.loantype.entity.TradeLoanType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenFacilityCaseCommandHandler implements CommandHandler<OpenFacilityCaseCommand> {

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;
    private final OpenFacilityCaseLoanApplicationMapper applicationMapper;
    private final TradeLoanFacilityValidationService validationService;
    private final CommandDispatcher commandDispatcher;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(@NonNull OpenFacilityCaseCommand command) {
        return loadDependencies(command).flatMap(context -> createFacility(command)
                .flatMap(facility -> validateFacility(facility, command, context))
                .flatMap(this::saveFacility)
                .flatMap(savedFacility -> handleGradualScheduleIfRequired(savedFacility, command))
                .map(AbstractAggregateRoot::domainEvents));
    }

    private Result<FacilityCreationContext> loadDependencies(OpenFacilityCaseCommand command) {
        var arrangementResult = loadArrangement(command.loanArrangementId());
        var loanTypeResult = loadLoanType(command.loanTypeId());
        return Result.combine(arrangementResult, loanTypeResult, FacilityCreationContext::new);
    }

    private Result<TradeLoanArrangement> loadArrangement(UUID arrangementId) {
        return Result.fromOptional(
                loanArrangementRepository.findById(LoanArrangementId.of(arrangementId)),
                Notification.ofError(OpenFacilityCaseErrorCodes.INVALID_LOAN_ARRANGEMENT, arrangementId));
    }

    private Result<TradeLoanType> loadLoanType(UUID loanTypeId) {
        return Result.fromOptional(
                tradeLoanTypeRepository.findById(LoanTypeId.of(loanTypeId)),
                Notification.ofError(OpenFacilityCaseErrorCodes.INVALID_LOAN_TYPE, loanTypeId));
    }

    private Result<TradeLoanFacility> createFacility(OpenFacilityCaseCommand command) {
        try {
            TradeLoanApplication application = applicationMapper.map(command.loanApplication());
            TradeLoanFacility facility = TradeLoanFacility.create(
                    LoanFacilityId.of(UUID.randomUUID()),
                    application,
                    LoanTypeId.of(command.loanTypeId()),
                    LoanArrangementId.of(command.loanArrangementId()),
                    clock);

            log.debug("Facility created with ID: {}", facility.getId().value());
            return Result.success(facility);
        } catch (Exception e) {
            log.error("Error creating facility", e);
            return Result.failure(
                    Notification.ofError(OpenFacilityCaseErrorCodes.FACILITY_CREATION_FAILED, e.getMessage()));
        }
    }

    private Result<TradeLoanFacility> validateFacility(
            TradeLoanFacility facility, OpenFacilityCaseCommand command, FacilityCreationContext context) {
        return validationService
                .validateForCreation(facility, context.arrangement(), context.loanType())
                .mapNonNull(ignored -> facility);
    }

    private Result<TradeLoanFacility> saveFacility(TradeLoanFacility facility) {
        try {
            TradeLoanFacility savedFacility = loanFacilityRepository.save(facility);
            return Result.success(savedFacility);
        } catch (Exception e) {
            log.error("Error saving facility: {}", facility.getId().value(), e);
            return Result.failure(
                    Notification.ofError(OpenFacilityCaseErrorCodes.FACILITY_SAVE_FAILED, e.getMessage()));
        }
    }

    private Result<TradeLoanFacility> handleGradualScheduleIfRequired(
            TradeLoanFacility facility, OpenFacilityCaseCommand command) {
        return loadArrangement(command.loanArrangementId()).flatMap(arrangement -> {
            if (arrangement.getInstallmentPolicy().installmentPaymentType() == InstallmentPaymentType.GRADUAL) {
                return dispatchGradualScheduleCommand(facility, command);
            }
            return Result.success(facility);
        });
    }

    private Result<TradeLoanFacility> dispatchGradualScheduleCommand(
            TradeLoanFacility facility, OpenFacilityCaseCommand command) {
        return Result.fromOptional(
                        command.installmentSchedule(),
                        () -> Notification.ofError(
                                OpenFacilityCaseErrorCodes.INSTALLMENT_SCHEDULE_IS_MANDATORY_IN_GRADUAL))
                .flatMap(scheduleCommand -> {
                    var planCommand = scheduleCommand.toBuilder()
                            .loanFacilityId(facility.getId().value())
                            .uid(command.uid())
                            .version(null)
                            .build();

                    commandDispatcher.dispatch(planCommand);
                    log.debug(
                            "Gradual installment schedule command dispatched for facility: {}",
                            facility.getId().value());
                    return Result.success(facility);
                });
    }

    private record FacilityCreationContext(TradeLoanArrangement arrangement, TradeLoanType loanType) {}
}
