package ir.dotin.loan.trade.core.application.service.originateloanfacility.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import ir.dotin.platform.pangaea.commons.core.Result;
import ir.dotin.platform.pangaea.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CoApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.core.application.ports.inbound.dto.ResolvedPartyDto;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;

/**
 * Rebuilds {@link PartyInfoResponse} (domain {@link Party} + {@link NationalCode} + flags) from the transport-neutral
 * {@link ResolvedPartyDto}s threaded back onto the command by the tx-free pre-flight. The construction mirrors
 * {@code KafkaValidationMapper.mapToPartyInfoResponse} bit-for-bit: same {@code PartyType}→party mapping per role, same
 * {@link CustomerName} fields, same national-code parsing, same restriction flags. No FCB call happens here.
 */
@Component
public class ResolvedPartyMapper {

    public Result<List<PartyInfoResponse>> reconstruct(List<ResolvedPartyDto> resolvedParties) {
        List<PartyInfoResponse> result = new ArrayList<>(resolvedParties.size());
        for (ResolvedPartyDto dto : resolvedParties) {
            Result<PartyInfoResponse> r = reconstructOne(dto);
            if (r.isFailure()) {
                return Result.failure(r.err().orElseThrow());
            }
            result.add(r.unwrap());
        }
        return Result.success(result);
    }

    private Result<PartyInfoResponse> reconstructOne(ResolvedPartyDto dto) {
        CustomerName customerName = new CustomerName(dto.firstName(), dto.lastName(), dto.companyName());

        Party party =
                switch (dto.role()) {
                    case PRIMARY_APPLICANT -> new ApplicantParty(dto.customerNumber(), dto.partyType(), customerName);
                    case CO_APPLICANT -> new CoApplicantParty(dto.customerNumber(), dto.partyType(), customerName);
                    case GUARANTOR ->
                        GuarantorParty.of(
                                        dto.customerNumber(),
                                        dto.partyType(),
                                        customerName,
                                        dto.guaranteePercentage() != null
                                                ? GuaranteePercentage.of(dto.guaranteePercentage())
                                                : null)
                                .unwrap();
                };

        Result<NationalCode> nationalCodeResult = NationalCode.valueOf(dto.nationalCode());
        if (nationalCodeResult.isFailure()) {
            return Result.failure(nationalCodeResult.err().orElseThrow());
        }

        return Result.success(new PartyInfoResponse(
                party, nationalCodeResult.unwrap(), dto.isInBlackList(), dto.isIncapable(), dto.isInGrayList()));
    }
}
