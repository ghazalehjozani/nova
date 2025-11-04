package ir.dotin.loan.trade.core.application.ports.outbound.client.response;

public record LoanTopicInfo(
        boolean mainTopicIsDebtor,
        boolean bankCommitmentsTopicIsDebtor,
        boolean customerCommitmentsTopicIsDebtor,
        boolean temporaryDebtorsTopicIsDebtor) {}
