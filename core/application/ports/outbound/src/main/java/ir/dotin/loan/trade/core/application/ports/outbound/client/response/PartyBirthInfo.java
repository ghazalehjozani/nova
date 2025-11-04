package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

public record PartyBirthInfo(
        String customerNumber,
        Integer age,
        boolean isGrowthOrder,
        boolean isUnderEighteenYearsOld,
        String birthDate,
        boolean isCheckGrowthAg) {}
