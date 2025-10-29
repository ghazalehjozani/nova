package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

public record ReasonType(
        String code,
        String centralBankCode,
        String description,
        String reasonType,
        boolean shouldHasSerial,
        boolean exemptionOfInquiryNumber) {}
