package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.time.Clock;
import java.util.Optional;
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
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfo;
import ir.dotin.loan.trade.core.application.ports.outbound.command.repository.TradeLoanFacilityRepository;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.OriginateLoanFacilityApplicationMapper;
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
    private final Clock clock;

    public Result<TradeLoanFacility> buildFacility(
            OriginateLoanFacilityCommand command,
            FacilityOriginationContext context,
            @Nullable InstallmentScheduleId scheduleId,
            @NonNull LoanFacilityId facilityId) {
        try {
            TradeLoanApplication application = buildApplication(command, context, scheduleId);

            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId,
                    application,
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

    public TradeLoanApplication buildApplication(
            OriginateLoanFacilityCommand command,
            FacilityOriginationContext context,
            @Nullable InstallmentScheduleId scheduleId) {

        Party mainCustomer = createPartyFromPartyInfo(context.mainCustomer());

        Branch branch = Branch.of(
                        BranchCode.of(command.loanApplication().branch().code()).orElseThrow())
                .orElseThrow();

        String derivedValue = String.valueOf(generateApplicationSequence(
                branch.code(),
                context.loanType().getId(),
                context.mainCustomer().party().customerNumber()));

        ApplicationNumber applicationNumber = new ApplicationNumber(
                branch,
                LoanTypeCode.of(context.loanType().getCode().value()).orElseThrow(),
                mainCustomer,
                Optional.empty(),
                derivedValue);

        Set<Party> enrichedGuarantors = context.guarantors().stream()
                .map(this::createPartyFromPartyInfo)
                .collect(Collectors.toSet());

        TradeLoanApplication.Builder builder = applicationMapper
                .map(command.loanApplication())
                .customer(mainCustomer)
                .applicationNumber(applicationNumber)
                .guarantors(enrichedGuarantors)
                .branch(branch);

        if (scheduleId != null) {
            builder.installmentScheduleId(scheduleId);
        }

        return TradeLoanApplication.create(builder).value();
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
}
