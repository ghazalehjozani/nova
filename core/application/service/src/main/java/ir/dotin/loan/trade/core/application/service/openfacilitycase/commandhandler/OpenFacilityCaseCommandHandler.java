package ir.dotin.loan.trade.core.application.service.openfacilitycase.commandhandler;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.platform.commons.domain.entity.AbstractAggregateRoot;
import ir.dotin.platform.commons.domain.event.DomainEvent;
import ir.dotin.platform.dispatcher.api.command.CommandHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.BranchCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.PersonName;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OpenFacilityCaseCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.request.CustomerInfoLoadOptions;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;
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
    private final CustomerServicePort customerServicePort;
    private final Clock clock;

    @Override
    public Result<List<DomainEvent<?, ?>>> handle(@NonNull OpenFacilityCaseCommand command) {
        return loadDependencies(command).flatMap(context -> createFacility(command, context)
                .flatMap(facility -> {
                    Result<TradeLoanFacility> loanFacilityResult = validateFacility(facility, command, context);
                    loanFacilityResult.flatMap(this::saveFacility);
                    return loanFacilityResult;
                })
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

    private Result<TradeLoanFacility> createFacility(OpenFacilityCaseCommand command, FacilityCreationContext context) {
        try {
            TradeLoanApplication application = prepareTradeLoanApplication(command, context);

            TradeLoanFacility facility = TradeLoanFacility.create(
                    LoanFacilityId.of(UUID.randomUUID()),
                    application,
                    LoanTypeId.of(command.loanTypeId()),
                    LoanArrangementId.of(command.loanArrangementId()),
                    clock,
                    Objects.requireNonNull(command.installmentSchedule()
                                    .map(schedule -> InstallmentScheduleId.of(schedule.uid()))
                                    .orElse(null))
                            .getValue());

            log.debug("Facility created with ID: {}", facility.getId().value());
            return Result.success(facility);
        } catch (Exception e) {
            log.error("Error creating facility", e);
            return Result.failure(
                    Notification.ofError(OpenFacilityCaseErrorCodes.FACILITY_CREATION_FAILED, e.getMessage()));
        }
    }

    private TradeLoanApplication prepareTradeLoanApplication(
            OpenFacilityCaseCommand command, FacilityCreationContext context) {

        CompletableFuture<PartyInfo> mainCustomerFuture = CompletableFuture.supplyAsync(() -> customerServicePort
                .loadCustomerInfo(
                        command.loanApplication().customer().customerNumber(), CustomerInfoLoadOptions.baseInfoOnly())
                .getValue());

        List<CompletableFuture<PartyInfo>> guarantorFutures = command.loanApplication().guarantors().stream()
                .map(guarantor -> CompletableFuture.supplyAsync(() -> customerServicePort
                        .loadCustomerInfo(guarantor.customerNumber(), CustomerInfoLoadOptions.baseInfoOnly())
                        .getValue()))
                .toList();

        CompletableFuture<Void> allFutures =
                CompletableFuture.allOf(Stream.concat(Stream.of(mainCustomerFuture), guarantorFutures.stream())
                        .toArray(CompletableFuture[]::new));

        allFutures.join();

        PartyInfo customerInfo = mainCustomerFuture.join();
        Party mainCustomer = createPartyDtoFromPartyInfo(customerInfo);

        Branch branch = Branch.of(BranchCode.of(Objects.requireNonNull(
                                command.loanApplication().branch().code()))
                        .value())
                .value();

        String derivedValue = generateDerivedValue(
                command.loanApplication().branch().code(),
                context.loanType.getCode().value(),
                customerInfo.party().customerNumber());

        ApplicationNumber applicationNumber = new ApplicationNumber(
                Objects.requireNonNull(branch),
                Objects.requireNonNull(
                        LoanTypeCode.of(context.loanType.getCode().value()).value()),
                mainCustomer,
                Optional.empty(),
                derivedValue);

        Set<Party> enrichedGuarantors = guarantorFutures.stream()
                .map(future -> createPartyDtoFromPartyInfo(future.join()))
                .collect(Collectors.toSet());

        TradeLoanApplication.Builder builder = applicationMapper
                .map(command.loanApplication())
                .customer(mainCustomer)
                .applicationNumber(applicationNumber)
                .guarantors(enrichedGuarantors)
                .branch(branch);

        return TradeLoanApplication.create(builder).value();
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

    private Party createPartyDtoFromPartyInfo(PartyInfo partyInfo) {
        return new Party(
                partyInfo.party().customerNumber(),
                partyInfo.party().type(),
                new PersonName(
                        partyInfo.party().name().firstName(),
                        partyInfo.party().name().lastName()));
    }

    private String generateDerivedValue(String branchCode, String loanTypeCode, String customerNumber) {
        return branchCode + "-" + loanTypeCode + "-" + customerNumber;
    }

    private record FacilityCreationContext(TradeLoanArrangement arrangement, TradeLoanType loanType) {}
}
