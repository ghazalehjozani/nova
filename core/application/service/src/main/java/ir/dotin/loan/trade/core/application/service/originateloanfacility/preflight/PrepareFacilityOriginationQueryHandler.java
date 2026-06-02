package ir.dotin.loan.trade.core.application.service.originateloanfacility.preflight;

import java.math.BigDecimal;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.platform.pangaea.commons.core.error.FailureCause;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateLoanFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.ResolvedPartyDto;
import ir.dotin.loan.trade.core.application.ports.inbound.query.FacilityOriginationPreflightResult;
import ir.dotin.loan.trade.core.application.ports.inbound.query.PrepareFacilityOriginationQuery;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.CustomerInfoLoader;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityBuilder;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.FacilityValidator;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.component.PartyEligibilityValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tx-free pre-flight handler for facility origination. Runs the FCB validations and customer-info loads with no JPA
 * transaction (the query implements {@code NonTransactionalQuery}, so the dispatcher holds no pooled Hikari connection
 * across the seconds-long FCB round-trips).
 *
 * <p>Behaviour is preserved bit-for-bit from the previous in-transaction orchestrator: validation runs FIRST and
 * short-circuits on failure (mirroring {@code FacilityOriginationOrchestrator} returning the validation failure before
 * {@code loadDependencies}); only on success are customer-infos loaded. Any failure is surfaced as a
 * {@link FailureCauseException} carrying the original {@link FailureCause} variant so the protocol layer maps it to the
 * identical HTTP status as before.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrepareFacilityOriginationQueryHandler
        implements QueryHandler<PrepareFacilityOriginationQuery, FacilityOriginationPreflightResult> {

    private final FacilityValidator facilityValidator;
    private final CustomerInfoLoader customerInfoLoader;
    private final PartyEligibilityValidator partyEligibilityValidator;
    private final FacilityBuilder facilityBuilder;

    @Override
    public FacilityOriginationPreflightResult handle(PrepareFacilityOriginationQuery query) {
        OriginateLoanFacilityCommand command = query.command();
        log.info("Pre-flight validation for facility origination, LoanType: {}", command.loanTypeCode());

        Result<Unit> validationResult = facilityValidator.callAndValidateServices(command);
        if (validationResult.isFailure()) {
            throw new FailureCauseException(validationResult.err().orElseThrow());
        }

        Result<List<PartyInfoResponse>> partyInfosResult = customerInfoLoader.loadPartyInfos(command);
        if (partyInfosResult.isFailure()) {
            throw new FailureCauseException(partyInfosResult.err().orElseThrow());
        }

        List<PartyInfoResponse> partyInfos = partyInfosResult.unwrap();

        // Gate origination on FCB eligibility flags (blacklist / incapacity / graylist). FCB returns these as data
        // and does not throw, so screening is enforced here before any party is resolved.
        Result<Unit> eligibilityResult = partyEligibilityValidator.validate(partyInfos);
        if (eligibilityResult.isFailure()) {
            throw new FailureCauseException(eligibilityResult.err().orElseThrow());
        }

        // Resolve the application number here, tx-free — this is the FCB get-application-number round-trip that
        // would otherwise pin a pooled Hikari connection inside the transactional command (see RB-0002).
        Result<ApplicationNumber> appNumberResult = facilityBuilder.resolveApplicationNumber(command, partyInfos);
        if (appNumberResult.isFailure()) {
            throw new FailureCauseException(appNumberResult.err().orElseThrow());
        }

        List<ResolvedPartyDto> parties =
                partyInfos.stream().map(this::toResolvedParty).toList();

        return new FacilityOriginationPreflightResult(
                parties, appNumberResult.unwrap().derivedValue());
    }

    private ResolvedPartyDto toResolvedParty(PartyInfoResponse info) {
        Party party = info.party();
        CustomerName name = party.name();
        BigDecimal guaranteePercentage = extractGuaranteePercentage(party);

        return ResolvedPartyDto.builder()
                .customerNumber(party.customerNumber())
                .partyType(party.partyType())
                .role(party.partyRole())
                .firstName(name.firstName())
                .lastName(name.lastName())
                .companyName(name.companyName())
                .guaranteePercentage(guaranteePercentage)
                .nationalCode(info.nationalCode().value())
                .isInBlackList(info.isInBlackList())
                .isIncapable(info.isIncapable())
                .isInGrayList(info.isInGrayList())
                .build();
    }

    private @Nullable BigDecimal extractGuaranteePercentage(Party party) {
        return party instanceof GuarantorParty guarantor
                ? guarantor.guaranteePercentage().value()
                : null;
    }
}
