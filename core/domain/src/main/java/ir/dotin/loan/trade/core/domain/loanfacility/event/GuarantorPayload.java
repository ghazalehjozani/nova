package ir.dotin.loan.trade.core.domain.loanfacility.event;

import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;

/**
 * Wire shape of a single guarantor carried by the guarantor add/remove events. Field names MUST stay identical to the
 * FCB-side {@code GuarantorPayloadDto} (customerNumber, partyType, customerName, guaranteePercent).
 */
public record GuarantorPayload(String customerNumber, String partyType, String customerName, String guaranteePercent) {

    public static GuarantorPayload from(GuarantorParty party) {
        CustomerName name = party.name();
        return new GuarantorPayload(
                party.customerNumber(),
                party.partyType().name(),
                name.fullName(),
                party.guaranteePercentage().value().toPlainString());
    }
}
