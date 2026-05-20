package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.util.Date;
import java.util.List;

public record InstallmentCollectionCompensateMessage(
        String producerCode,
        String eventUid,
        Date dateTime,
        int version,
        String responseTopic,
        String[] tags,
        String operationType,
        String fileNumber,
        List<String> transactionNumbers) {}
