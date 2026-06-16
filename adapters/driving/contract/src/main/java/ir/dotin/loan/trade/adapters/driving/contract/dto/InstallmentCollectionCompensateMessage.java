package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.List;

public record InstallmentCollectionCompensateMessage(
        String operationType, String fileNumber, List<String> transactionNumbers) {}
