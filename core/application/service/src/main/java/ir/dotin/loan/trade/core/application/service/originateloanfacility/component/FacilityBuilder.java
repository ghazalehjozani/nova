package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
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
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.OriginateLoanFacilityApplicationMapper;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.ApplicationNumberStrategy;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.ApplicationNumberStrategySelector;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.FacilityOriginationContext;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanApplication;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityBuilder {

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final OriginateLoanFacilityApplicationMapper applicationMapper;
    private final LoanServicePort loanServicePort;
    private final ApplicationNumberStrategySelector applicationNumberStrategySelector;
    private final Clock clock;

    public Result<TradeLoanFacility> buildFacility(
            OriginateLoanFacilityCommand command,
            FacilityOriginationContext context,
            @Nullable InstallmentScheduleId scheduleId,
            @NonNull LoanFacilityId facilityId) {
        try {

            Result<TradeLoanApplication> applicationResult = buildApplicationWithStrategy(command, context);

            if (applicationResult.isFailure()) {
                return Result.failure(applicationResult.notification());
            }

            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId,
                    applicationResult.value(),
                    LoanTypeId.of(command.loanTypeId()),
                    LoanArrangementId.of(command.loanArrangementId()),
                    clock,
                    scheduleId);

            log.debug("Facility created with ID: {}", facility.getId().value());
            return Result.success(facility);
        } catch (Exception e) {
            log.error("Error creating facility", e);
            return Result.failure(
                    Notification.ofError(OriginateLoanFacilityErrorCodes.FACILITY_CREATION_FAILED, e.getMessage()));
        }
    }

    public Result<TradeLoanApplication> buildApplicationWithStrategy(
            OriginateLoanFacilityCommand command, FacilityOriginationContext context) {

        try {
            Party mainCustomer = createPartyFromPartyInfo(context.mainCustomer());

            Branch branch = Branch.of(
                            BranchCode.of(command.loanApplication().branch().code())
                                    .orElseThrow())
                    .orElseThrow();

            LoanTypeCode loanTypeCode =
                    LoanTypeCode.of(context.loanType().getCode().value()).orElseThrow();

            String derivedValue = String.valueOf(generateApplicationSequence(
                    branch.code(),
                    context.loanType().getId(),
                    context.mainCustomer().party().customerNumber()));

            ApplicationNumberStrategy strategy = applicationNumberStrategySelector.selectStrategy();

            Result<ApplicationNumber> applicationNumberResult =
                    strategy.generateOrValidateApplicationNumber(branch, loanTypeCode, mainCustomer, derivedValue);

            if (applicationNumberResult.isFailure()) {
                return Result.failure(applicationNumberResult.notification());
            }

            ApplicationNumber applicationNumber = applicationNumberResult.getValue();

            Set<Party> enrichedGuarantors = context.guarantors().stream()
                    .map(this::createPartyFromPartyInfo)
                    .collect(Collectors.toSet());

            TradeLoanApplication.Builder builder = applicationMapper
                    .map(command.loanApplication())
                    .customer(mainCustomer)
                    .applicationNumber(applicationNumber)
                    .guarantors(enrichedGuarantors)
                    .branch(branch);

            return TradeLoanApplication.create(builder);

        } catch (Exception e) {
            return Result.failure(
                    Notification.ofError(OriginateLoanFacilityErrorCodes.FACILITY_CREATION_FAILED, e.getMessage()));
        }
    }

    private Party createPartyFromPartyInfo(PartyInfo partyInfo) {
        return new Party(
                partyInfo.party().customerNumber(),
                partyInfo.party().type(),
                new PersonName(
                        partyInfo.party().name().firstName(),
                        partyInfo.party().name().lastName()));
    }

    private Long generateApplicationSequence(BranchCode branchCode, LoanTypeId loanTypeId, String customerNumber) {
        return loanFacilityRepository.countByBranchCodeAndLoanTypeIdAndCustomerNumber(
                branchCode, loanTypeId, customerNumber);
    }

    @Deprecated
    public TradeLoanApplication buildApplication(
            OriginateLoanFacilityCommand command, FacilityOriginationContext context) {
        return buildApplicationWithStrategy(command, context).value();
    }
}
