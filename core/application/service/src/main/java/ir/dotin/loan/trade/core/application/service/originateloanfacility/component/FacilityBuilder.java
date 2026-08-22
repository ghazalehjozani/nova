package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import ir.dotin.platform.accounting.document.api.model.BranchCode;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.ProductProfile;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand.InstallmentCountDto;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateUnequalInstallmentFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SamatDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper.OriginateLoanFacilityApplicationMapper;
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

    private static final int SINGLE_INSTALLMENT_COUNT = 1;

    private final OriginateLoanFacilityApplicationMapper applicationMapper;
    private final ApplicationNumberStrategySelector applicationNumberStrategySelector;
    private final Clock clock;

    public Result<TradeLoanFacility> buildFacility(
            OriginateFacilityCommand command,
            FacilityOriginationContext context,
            ProductProfile profile,
            @Nullable InstallmentScheduleId scheduleId,
            @NonNull LoanFacilityId facilityId,
            ApplicationNumber applicationNumber) {

        return buildApplication(command, context, profile, applicationNumber).map(application -> {
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
            OriginateFacilityCommand command,
            FacilityOriginationContext context,
            ProductProfile profile,
            ApplicationNumber appNumber) {

        String rawBranchCode = command.loanApplication().branch().code();
        if (rawBranchCode == null) {
            return Result.failure(OriginateLoanFacilityErrorCodes.BRANCH_CODE_REQUIRED);
        }

        Result<Branch> branchResult = BranchCode.of(rawBranchCode).flatMap(Branch::of);
        if (branchResult.isFailure()) {
            return Result.failure(branchResult.err().orElseThrow());
        }
        Branch branch = branchResult.unwrap();

        Set<Party> enrichedParties =
                context.partyInfos().stream().map(PartyInfoResponse::party).collect(Collectors.toSet());

        Result<Unit> matchResult = validateApplicationNumberMatch(command, appNumber);
        if (matchResult.isFailure()) {
            return Result.failure(matchResult.err().orElseThrow());
        }

        TradeLoanApplication.Builder builder = applicationMapper
                .map(command.loanApplication())
                .parties(enrichedParties)
                .applicationNumber(appNumber)
                .branch(branch);
        Result<Unit> countResult = fillInstallmentCount(builder, command, profile);
        if (countResult.isFailure()) {
            return Result.failure(countResult.err().orElseThrow());
        }

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
                return Result.failure(samatResult.err().orElseThrow());
            }
            builder.samat(samatResult.unwrap());
        }

        return TradeLoanApplication.create(builder);
    }

    public Result<ApplicationNumber> resolveApplicationNumber(
            OriginateFacilityCommand command, List<PartyInfoResponse> partyInfos) {

        String rawBranchCode = command.loanApplication().branch().code();
        if (rawBranchCode == null) {
            return Result.failure(OriginateLoanFacilityErrorCodes.BRANCH_CODE_REQUIRED);
        }
        Result<Branch> branchResult = BranchCode.of(rawBranchCode).flatMap(Branch::of);
        if (branchResult.isFailure()) {
            return Result.failure(branchResult.err().orElseThrow());
        }

        Result<LoanTypeCode> loanTypeCodeResult = LoanTypeCode.of(command.loanTypeCode());
        if (loanTypeCodeResult.isFailure()) {
            return Result.failure(loanTypeCodeResult.err().orElseThrow());
        }

        Party primaryApplicant = partyInfos.stream()
                .filter(info -> info.party().partyRole() == PartyRole.PRIMARY_APPLICANT)
                .map(PartyInfoResponse::party)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Invariant Failure: Primary applicant missing from pre-flight party infos"));

        return applicationNumberStrategySelector
                .selectStrategy()
                .generateApplicationNumber(branchResult.unwrap(), loanTypeCodeResult.unwrap(), primaryApplicant);
    }

    private Result<Unit> validateApplicationNumberMatch(OriginateFacilityCommand command, ApplicationNumber appNumber) {
        String commandAppNumber = command.loanApplication().applicationNumber();

        if (commandAppNumber != null) {
            String generatedAppNumber = appNumber.formattedApplicationNumber();

            if (!generatedAppNumber.equals(commandAppNumber)) {
                return Result.failure(
                        OriginateLoanFacilityErrorCodes.APPLICATION_NUMBER_MISMATCH,
                        commandAppNumber,
                        generatedAppNumber);
            }
        }
        return Result.success();
    }

    // why: TradeLoanApplication requires a non-null positive count, so a single-instalment product — whose caller
    // legitimately sends none — is resolved to 1 here rather than by weakening the shared-kernel invariant. A
    // SCHEDULED product with no count must surface as a business error, not an NPE: the guard that used to catch it
    // lived in the deleted StandardScheduleStrategy, and base-loan's specification runs only at validateForCreation,
    // which is after this method.
    private Result<Unit> fillInstallmentCount(
            TradeLoanApplication.Builder builder, OriginateFacilityCommand command, ProductProfile profile) {

        if (command instanceof OriginateUnequalInstallmentFacilityCommand unequal) {
            builder.installmentCount(InstallmentCount.of(
                            unequal.installmentSchedulePlan().installments().size())
                    .unwrap());
            return Result.success();
        }

        if (profile.installmentScheduleType() == InstallmentScheduleType.SINGLE_INSTALLMENT) {
            builder.installmentCount(
                    InstallmentCount.of(SINGLE_INSTALLMENT_COUNT).unwrap());
            return Result.success();
        }

        InstallmentCountDto countDto = command.loanApplication().installmentCount();
        if (countDto == null || countDto.value() == null) {
            return Result.failure(OriginateLoanFacilityErrorCodes.INSTALLMENT_COUNT_CANNOT_BE_EMPTY);
        }

        return InstallmentCount.of(countDto.value()).map(count -> {
            builder.installmentCount(count);
            return Unit.INSTANCE;
        });
    }
}
