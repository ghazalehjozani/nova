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
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
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
            return Result.failure(OriginateLoanFacilityErrorCodes.BRANCH_CODE_REQUIRED);
        }

        Result<Branch> branchResult = BranchCode.of(rawBranchCode).flatMap(Branch::of);
        if (branchResult.isFailure()) {
            return Result.failure(branchResult.err().orElseThrow());
        }
        Branch branch = branchResult.unwrap();

        // 2. Validate Loan Type Code
        Result<LoanTypeCode> loanTypeCodeResult =
                LoanTypeCode.of(requireNonNull(context.loanType().getCode()).value());
        if (loanTypeCodeResult.isFailure()) {
            return Result.failure(loanTypeCodeResult.err().orElseThrow());
        }
        LoanTypeCode loanTypeCode = loanTypeCodeResult.unwrap();

        // 3. Prepare Parties
        Party primaryApplicant = context.primaryApplicant().party();

        Set<Party> enrichedParties =
                context.partyInfos().stream().map(PartyInfoResponse::party).collect(Collectors.toSet());

        // Application number is resolved tx-free in the pre-flight (PrepareFacilityOriginationQuery) and threaded in
        // via resolvedApplicationNumber — reconstruct the VO here rather than re-calling FCB inside the transaction
        // (RB-0002: an in-tx FCB round-trip pins the pooled Hikari connection). Fallback to the configured strategy
        // only on the (non-pre-flight) path where it was not pre-resolved.
        String resolvedDerivedValue = command.resolvedApplicationNumber();
        Result<ApplicationNumber> appNumberResult = (resolvedDerivedValue != null)
                ? ApplicationNumber.of(branch, loanTypeCode, primaryApplicant, resolvedDerivedValue)
                : applicationNumberStrategySelector
                        .selectStrategy()
                        .generateApplicationNumber(branch, loanTypeCode, primaryApplicant);

        if (appNumberResult.isFailure()) {
            return Result.failure(appNumberResult.err().orElseThrow());
        }

        Result<Unit> matchResult = validateApplicationNumberMatch(command, appNumberResult);
        if (matchResult.isFailure()) {
            return Result.failure(matchResult.err().orElseThrow());
        }

        // 4. Build Application
        TradeLoanApplication.Builder builder = applicationMapper
                .map(command.loanApplication())
                .parties(enrichedParties)
                .applicationNumber(appNumberResult.unwrap())
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
                return Result.failure(samatResult.err().orElseThrow());
            }
            builder.samat(samatResult.unwrap());
        }

        return TradeLoanApplication.create(builder);
    }

    /**
     * Tx-free application-number resolution for the origination pre-flight ({@code PrepareFacilityOriginationQuery}).
     * Mirrors {@link #buildApplication}'s branch / loan-type / primary-applicant derivation and runs the configured
     * strategy (the FCB get-application-number round-trip for the external strategy) outside any transaction. The
     * resolved {@code ApplicationNumber.derivedValue()} is threaded back onto the command and reused in
     * {@link #buildApplication}, so the transactional command never pins a pooled connection across FCB (RB-0002).
     */
    public Result<ApplicationNumber> resolveApplicationNumber(
            OriginateLoanFacilityCommand command, List<PartyInfoResponse> partyInfos) {

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

    private Result<Unit> validateApplicationNumberMatch(
            OriginateLoanFacilityCommand command, Result<ApplicationNumber> appNumberResult) {
        String commandAppNumber = command.loanApplication().applicationNumber();

        if (commandAppNumber != null) {
            String generatedAppNumber = appNumberResult.unwrap().formattedApplicationNumber();

            if (!generatedAppNumber.equals(commandAppNumber)) {
                return Result.failure(
                        OriginateLoanFacilityErrorCodes.APPLICATION_NUMBER_MISMATCH,
                        commandAppNumber,
                        generatedAppNumber);
            }
        }
        return Result.success();
    }

    private void fillInstallmentCount(TradeLoanApplication.Builder builder, OriginateLoanFacilityCommand command) {
        if (command.installmentSchedulePlan() != null) {
            builder.installmentCount(InstallmentCount.of(
                            command.installmentSchedulePlan().installments().size())
                    .unwrap());
        } else {
            Integer value = null;
            if (command.loanApplication().installmentCount() != null) {
                value = command.loanApplication().installmentCount().value();
            }
            builder.installmentCount(new InstallmentCount(requireNonNull(value)));
        }
    }
}
