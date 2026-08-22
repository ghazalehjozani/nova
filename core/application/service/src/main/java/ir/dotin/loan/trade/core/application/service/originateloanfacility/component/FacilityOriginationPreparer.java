package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.trade.core.application.ports.inbound.command.OriginateFacilityCommand;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel.DIGITAL_BANK;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacilityOriginationPreparer {

    private final FacilityValidator facilityValidator;
    private final CustomerInfoLoader customerInfoLoader;
    private final PartyEligibilityValidator partyEligibilityValidator;
    private final FacilityBuilder facilityBuilder;

    public Result<OriginationPreparation> prepare(OriginateFacilityCommand command) {
        log.info("Starting facility origination for LoanType: {}", command.loanTypeCode());

        Result<Unit> validationResult = facilityValidator.callAndValidateServices(command);
        if (validationResult.isFailure()) {
            return Result.failure(validationResult.err().orElseThrow());
        }

        Result<List<PartyInfoResponse>> partyInfosResult = customerInfoLoader.loadPartyInfos(command);
        if (partyInfosResult.isFailure()) {
            return Result.failure(partyInfosResult.err().orElseThrow());
        }
        List<PartyInfoResponse> partyInfos = partyInfosResult.unwrap();

        if (command.loanApplication().applicantChannel() != DIGITAL_BANK) {
            Result<Unit> eligibilityResult = partyEligibilityValidator.validate(partyInfos);
            if (eligibilityResult.isFailure()) {
                return Result.failure(eligibilityResult.err().orElseThrow());
            }
        }

        return facilityBuilder
                .resolveApplicationNumber(command, partyInfos)
                .map(applicationNumber -> new OriginationPreparation(partyInfos, applicationNumber));
    }
}
