package ir.dotin.loan.trade.core.application.service.openfacilitycase.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
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

@Service
@RequiredArgsConstructor
public class OpenFacilityCaseCommandHandler implements CommandHandler<OpenFacilityCaseCommand> {

    private static final Logger log = LoggerFactory.getLogger(OpenFacilityCaseCommandHandler.class);

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final TradeLoanArrangementRepository loanArrangementRepository;
    private final TradeLoanTypeRepository tradeLoanTypeRepository;
    private final OpenFacilityCaseLoanApplicationMapper applicationMapper;
    private final TradeLoanFacilityValidationService validationService;

    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(@NonNull OpenFacilityCaseCommand command) {
        UUID uuid = Optional.ofNullable(command.loanFacilityId()).orElseGet(UUID::randomUUID);
        LoanFacilityId loanFacilityId = LoanFacilityId.of(Objects.requireNonNull(uuid));

        return loadDependencies(command).flatMap(context -> ensureFacilityDoesNotExist(loanFacilityId)
                .flatMap(ignored -> createFacility(command, loanFacilityId))
                .flatMap(facility -> validateFacility(facility, context))
                .peekValue(loanFacilityRepository::save)
                .peekValue(facility -> log.debug(
                        "Facility case opened successfully: {}",
                        facility.getId().value()))
                .mapNonNull(TradeLoanFacility::domainEvents));
    }

    private Result<FacilityCreationContext> loadDependencies(OpenFacilityCaseCommand command) {
        Result<TradeLoanArrangement> arrangement = Result.fromOptional(
                loanArrangementRepository.findById(LoanArrangementId.of(command.loanArrangementId())),
                Notification.ofError(OpenFacilityCaseErrorCodes.INVALID_LOAN_ARRANGEMENT, command.loanArrangementId()));

        Result<TradeLoanType> loanType = Result.fromOptional(
                tradeLoanTypeRepository.findById(LoanTypeId.of(command.loanTypeId())),
                Notification.ofError(OpenFacilityCaseErrorCodes.INVALID_LOAN_TYPE, command.loanTypeId()));

        return Result.combine(arrangement, loanType, FacilityCreationContext::new);
    }

    private Result<Void> ensureFacilityDoesNotExist(LoanFacilityId facilityId) {
        return loanFacilityRepository
                .existsById(facilityId)
                .flatMap(exists -> Result.requireFalse(
                        exists,
                        Notification.ofError(OpenFacilityCaseErrorCodes.FACILITY_ALREADY_EXISTS, facilityId.value())));
    }

    private Result<TradeLoanFacility> createFacility(OpenFacilityCaseCommand command, LoanFacilityId facilityId) {
        TradeLoanApplication application = applicationMapper.map(command.loanApplication());
        TradeLoanFacility facility = TradeLoanFacility.create(
                facilityId,
                application,
                LoanTypeId.of(command.loanTypeId()),
                LoanArrangementId.of(command.loanArrangementId()),
                clock);

        return Result.success(facility);
    }

    private Result<TradeLoanFacility> validateFacility(TradeLoanFacility facility, FacilityCreationContext context) {

        return validationService
                .validateForCreation(facility, context.arrangement(), context.loanType())
                .mapNonNull(ignored -> facility);
    }

    private record FacilityCreationContext(TradeLoanArrangement arrangement, TradeLoanType loanType) {}
}
