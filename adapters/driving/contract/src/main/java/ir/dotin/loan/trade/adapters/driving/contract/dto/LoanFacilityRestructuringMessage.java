package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.messaging.api.command.CommandPayload;
import ir.dotin.platform.messaging.api.version.ContractVersion;

@ContractVersion(1)
public record LoanFacilityRestructuringMessage(
        String producerCode,
        String eventUid,
        @Nullable Date dateTime,
        int version,
        @Nullable String responseTopic,
        @Nullable String[] tags,
        String operationType,
        String fileNumber,
        Map<String, Object> metadata,
        String transactionNumber,
        List<InstallmentDetailsDTO> installmentList,
        Integer durationMonths)
        implements CommandPayload {

    public record InstallmentDetailsDTO(
            int installmentSequenceNumber, BigDecimal principalAmount, BigDecimal interestAmount, String dueDate) {}
}
