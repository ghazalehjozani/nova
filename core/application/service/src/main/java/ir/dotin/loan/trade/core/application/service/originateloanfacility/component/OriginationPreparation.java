package ir.dotin.loan.trade.core.application.service.originateloanfacility.component;

import java.util.List;

import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.trade.core.application.ports.outbound.client.response.PartyInfoResponse;

public record OriginationPreparation(List<PartyInfoResponse> partyInfos, ApplicationNumber applicationNumber) {}
