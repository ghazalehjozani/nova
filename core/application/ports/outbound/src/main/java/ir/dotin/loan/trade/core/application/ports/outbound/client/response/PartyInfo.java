package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

import ir.dotin.platform.commons.domain.vo.NationalCode;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;

public record PartyInfo(
        Party party, NationalCode nationalCode, boolean isInBlackList, boolean isIncapable, boolean isInGrayList) {}
