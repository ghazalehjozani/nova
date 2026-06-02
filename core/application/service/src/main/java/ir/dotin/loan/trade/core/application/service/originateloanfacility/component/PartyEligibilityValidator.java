package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Notification;
import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.core.Unit;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;
import ir.dotin.loan.trade.core.application.service.originateloanfacility.i18n.OriginateLoanFacilityErrorCodes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gates facility origination on the eligibility flags FCB returns for each party. FCB surfaces
 * {@code isInBlackList} / {@code isIncapable} / {@code isInGrayList} as data on {@link PartyInfoResponse} and never
 * throws on them, so screening must be enforced here before the parties are resolved.
 *
 * <p>Every party — applicant and guarantor alike — is screened identically. Checks are ordered most-severe first
 * (blacklist, then incapacity, then graylist) and short-circuit on the first violation, mirroring the failure style of
 * {@link FacilityValidator}: {@code Result.failure(Notification.ofError(...))}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartyEligibilityValidator {

    public Result<Unit> validate(List<PartyInfoResponse> partyInfos) {
        log.debug("Screening eligibility for {} parties", partyInfos.size());

        for (PartyInfoResponse info : partyInfos) {
            Result<Unit> result = screen(info);
            if (result.isFailure()) {
                return result;
            }
        }

        return Result.success();
    }

    private Result<Unit> screen(PartyInfoResponse info) {
        Party party = info.party();
        String nationalCode = info.nationalCode().value();

        if (info.isInBlackList()) {
            return Result.failure(Notification.ofError(
                    OriginateLoanFacilityErrorCodes.APPLICANT_BLACKLISTED, nationalCode, party.partyRole()));
        }
        if (info.isIncapable()) {
            return Result.failure(Notification.ofError(
                    OriginateLoanFacilityErrorCodes.APPLICANT_INCAPABLE, nationalCode, party.partyRole()));
        }
        if (info.isInGrayList()) {
            return Result.failure(Notification.ofError(
                    OriginateLoanFacilityErrorCodes.APPLICANT_GRAYLISTED, nationalCode, party.partyRole()));
        }
        return Result.success();
    }
}
