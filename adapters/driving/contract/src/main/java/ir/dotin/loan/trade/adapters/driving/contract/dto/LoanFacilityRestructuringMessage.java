package ir.dotin.loan.trade.adapters.driving.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ir.dotin.platform.pangaea.messaging.api.command.CommandPayload;

public record LoanFacilityRestructuringMessage(
        String operationType,
        String fileNumber,
        Map<String, Object> metadata,
        String transactionNumber,
        List<InstallmentDetailsDTO> installmentList,
        Integer durationMonths)
        implements CommandPayload {

    public record InstallmentDetailsDTO(
            int installmentSequenceNumber, BigDecimal principalAmount, BigDecimal interestAmount, LocalDate dueDate) {}
}
