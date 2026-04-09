package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.Result;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SamatDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.OriginateLoanFacilityApplicationMapper;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.strategy.ApplicationNumberGenerationStrategy;
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
                context.partyInfos().stream().map(PartyInfoResponse::party).collect(Collectors.toSet());

        ApplicationNumberGenerationStrategy strategy = applicationNumberStrategySelector.selectStrategy();

        Result<ApplicationNumber> appNumberResult =
                strategy.generateApplicationNumber(branch, loanTypeCode, primaryApplicant);

        if (appNumberResult.isFailure()) {
            return Result.failure(appNumberResult.notification());
        }

        Result<Void> matchResult = validateApplicationNumberMatch(command, appNumberResult);
        if (matchResult.isFailure()) {
            return Result.failure(matchResult.notification());
        }

        // 4. Build Application
        TradeLoanApplication.Builder builder = applicationMapper
                .map(command.loanApplication())
                .parties(enrichedParties)
                .applicationNumber(appNumberResult.getValue())
                .branch(branch);
        fillInstallmentCount(builder, command);

        SamatDto samatDto = command.loanApplication().samat();
        if (samatDto != null) {
            String isicEconomicSector = samatDto.isicEconomicSector() != null ? samatDto.isicEconomicSector() : "0";
            String subIsicEconomicSector =
                    samatDto.subIsicEconomicSector() != null ? samatDto.subIsicEconomicSector() : "0";
            String useType = samatDto.useType() != null ? samatDto.useType() : "30";
            String exceptionCode = samatDto.exceptionCode() != null ? samatDto.exceptionCode() : "0";
            String consumptionPlaceCode =
                    samatDto.consumptionPlaceCode() != null ? samatDto.consumptionPlaceCode() : "0";
            Result<Samat> samatResult = Samat.of(
                    samatDto.trackingNumber(),
                    isicEconomicSector,
                    subIsicEconomicSector,
                    useType,
                    exceptionCode,
                    consumptionPlaceCode);
            if (samatResult.isFailure()) {
                return Result.failure(samatResult.notification());
            }
            builder.samat(samatResult.getValue());
        }

        return TradeLoanApplication.create(builder);
    }

    private Result<Void> validateApplicationNumberMatch(
            OriginateLoanFacilityCommand command, Result<ApplicationNumber> appNumberResult) {
        String commandAppNumber = command.loanApplication().applicationNumber();

        if (commandAppNumber != null) {
            String generatedAppNumber = appNumberResult.getValue().formattedApplicationNumber();

            if (!generatedAppNumber.equals(commandAppNumber)) {
                return Result.failure(Notification.ofError(
                        OriginateLoanFacilityErrorCodes.APPLICATION_NUMBER_MISMATCH,
                        commandAppNumber,
                        generatedAppNumber));
            }
        }
        return Result.success();
    }

    private void fillInstallmentCount(TradeLoanApplication.Builder builder, OriginateLoanFacilityCommand command) {
        if (command.installmentSchedulePlan() != null) {
            builder.installmentCount(InstallmentCount.of(
                            command.installmentSchedulePlan().installments().size())
                    .orElseThrow());
        } else {
            Integer value = null;
            if (command.loanApplication().installmentCount() != null) {
                value = command.loanApplication().installmentCount().value();
            }
            builder.installmentCount(new InstallmentCount(requireNonNull(value)));
        }
    }
}
