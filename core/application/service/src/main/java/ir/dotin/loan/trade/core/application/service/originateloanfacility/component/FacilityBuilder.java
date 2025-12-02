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
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
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

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityBuilder {

    private final TradeLoanFacilityRepository loanFacilityRepository;
    private final OriginateLoanFacilityApplicationMapper applicationMapper;
    private final ApplicationNumberStrategySelector applicationNumberStrategySelector;
    private final Clock clock;

    public Result<TradeLoanFacility> buildFacility(
            OriginateLoanFacilityCommand command,
            FacilityOriginationContext context,
            @Nullable InstallmentScheduleId scheduleId,
            @NonNull LoanFacilityId facilityId) {

        return buildApplication(command, context).map(application -> {
            TradeLoanFacility facility = TradeLoanFacility.create(
                    facilityId,
                    application,
                    context.loanType().getId(),
                    context.arrangement().getId(),
                    clock,
                    scheduleId);

            log.debug("Facility created with ID: {}", facility.getId().value());
            return facility;
        });
    }

    public Result<TradeLoanApplication> buildApplication(
            OriginateLoanFacilityCommand command, FacilityOriginationContext context) {

        // 1. Validate Branch Code
        String rawBranchCode = command.loanApplication().branch().code();
        if (rawBranchCode == null) {
            return Result.failure(Notification.ofError(OriginateLoanFacilityErrorCodes.BRANCH_CODE_REQUIRED));
        }

        Result<Branch> branchResult = BranchCode.of(rawBranchCode).flatMap(Branch::of);
        if (branchResult.isFailure()) {
            return Result.failure(branchResult.notification());
        }
        Branch branch = branchResult.getValue();

        // 2. Validate Loan Type Code
        Result<LoanTypeCode> loanTypeCodeResult =
                LoanTypeCode.of(requireNonNull(context.loanType().getCode()).value());
        if (loanTypeCodeResult.isFailure()) {
            return Result.failure(loanTypeCodeResult.notification());
        }
        LoanTypeCode loanTypeCode = loanTypeCodeResult.getValue();

        // 3. Prepare Parties
        Party primaryApplicant = context.primaryApplicant().party();

        Set<Party> enrichedParties =
                context.partyInfos().stream().map(PartyInfo::party).collect(Collectors.toSet());

        ApplicationNumberStrategy strategy = applicationNumberStrategySelector.selectStrategy();

        Result<ApplicationNumber> appNumberResult =
                strategy.generateOrValidateApplicationNumber(branch, loanTypeCode, primaryApplicant);

        if (appNumberResult.isFailure()) {
            return Result.failure(appNumberResult.notification());
        }

        // 4. Build Application
        TradeLoanApplication.Builder builder = applicationMapper
                .map(command.loanApplication())
                .parties(enrichedParties)
                .applicationNumber(appNumberResult.getValue())
                .branch(branch);

        return TradeLoanApplication.create(builder);
    }
}
