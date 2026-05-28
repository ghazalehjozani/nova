package ir.dotin.loan.trade.core.application.service.approvefacility.preflight;

import org.springframework.stereotype.Service;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.exception.FailureCauseException;
import ir.dotin.platform.pangaea.dispatcher.api.query.QueryHandler;
import ir.dotin.loan.trade.core.application.ports.inbound.command.ApproveFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.SanctionDetailsDto;
import ir.dotin.loan.trade.core.application.ports.inbound.query.FacilityApprovalPreflightResult;
import ir.dotin.loan.trade.core.application.ports.inbound.query.PrepareFacilityApprovalQuery;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.SanctionDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tx-free pre-flight handler for MANUAL facility approval. Runs the ONE FCB read with no JPA transaction (the query
 * implements {@code NonTransactionalQuery}, so the dispatcher holds no pooled Hikari connection across the seconds-long
 * FCB request/reply round-trip — LN-59412).
 *
 * <p>This issues the exact same call the previous in-transaction {@code ManualApprovalStrategy.approve} ran:
 * {@code fetchSanctionDetailsPort.fetchBySanctionSerial(command.loanFacilityId().toString())}. (The old code passed
 * {@code facility.getId().value().toString()}, which equals {@code command.loanFacilityId().toString()} — so the
 * pre-flight needs NO facility load.)
 *
 * <p>DESIGN NOTE (accepted minor reordering): the {@code DIGITAL_BANK} gate is intentionally NOT replicated here. The
 * command handler's {@code ManualApprovalStrategy.validate} still performs that gate. Consequence: for the misuse case
 * of a {@code DIGITAL_BANK} facility hitting the manual endpoint, this FCB read now happens (in the pre-flight) before
 * the command rejects with {@code MANUAL_APPROVAL_NOT_ALLOWED}. The final error is identical; only one extra FCB READ
 * occurs in that error path. This keeps the {@code DIGITAL_BANK} domain rule in ONE place — the strategy — rather than
 * duplicating it.
 *
 * <p>Any FCB failure is surfaced as a {@link FailureCauseException} carrying the original {@code FailureCause} variant
 * so the protocol layer maps it to the identical HTTP status as before.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrepareFacilityApprovalQueryHandler
        implements QueryHandler<PrepareFacilityApprovalQuery, FacilityApprovalPreflightResult> {

    private final FetchSanctionDetailsPort fetchSanctionDetailsPort;

    @Override
    public FacilityApprovalPreflightResult handle(PrepareFacilityApprovalQuery query) {
        ApproveFacilityCommand command = query.command();
        log.info("Pre-flight sanction-details fetch for manual facility approval: {}", command.loanFacilityId());

        Result<SanctionDetails> sanctionDetailsResult = fetchSanctionDetailsPort.fetchBySanctionSerial(
                command.loanFacilityId().toString());
        if (sanctionDetailsResult.isFailure()) {
            throw new FailureCauseException(sanctionDetailsResult.err().orElseThrow());
        }

        SanctionDetailsDto sanctionDetails = toDto(sanctionDetailsResult.unwrap());
        return new FacilityApprovalPreflightResult(sanctionDetails);
    }

    private SanctionDetailsDto toDto(SanctionDetails details) {
        return SanctionDetailsDto.builder()
                .sanctionSerialValue(details.sanctionSerialValue())
                .sanctionType(details.sanctionType())
                .approvedAmount(details.approvedAmount())
                .currencyCode(details.currency().getCode())
                .gracePeriod(details.gracePeriod())
                .installmentCount(details.installmentCount())
                .loanDuration(details.loanDuration())
                .disbursementMethod(details.disbursementMethod())
                .lifeInsuranceId(details.lifeInsuranceId())
                .collateralSerial(details.collateralSerial())
                .revocationReason(details.revocationReason())
                .confirmTypePersonCode(details.confirmType().personCode())
                .build();
    }
}
