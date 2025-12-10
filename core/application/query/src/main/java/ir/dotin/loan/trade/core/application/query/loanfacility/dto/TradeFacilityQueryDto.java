package ir.dotin.loan.trade.core.application.query.loanfacility.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import ir.dotin.platform.dispatcher.api.query.QueryResult;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.TransactionStatus;

public record TradeFacilityQueryDto(
        UUID id,
        Long version,
        TradeLoanApplicationDto loanApplication,
        TradeSanctionedLoanEntityDto sanctionedLoan,
        UUID loanTypeId,
        UUID loanArrangementId,
        UUID installmentScheduleId,
        FacilityStatus currentState,
        BigDecimal totalDisbursedAmount,
        String totalDisbursedAmountCurrency,
        List<TransactionNumberEmbDto> issueContractTransactionNumbers,
        List<TransactionNumberEmbDto> disbursementTransactionNumbers,
        Map<String, String> accountInfoMap,
        LocalDate disbursementDate,
        String facilityType,
        List<CollateralEmbDto> collaterals)
        implements Serializable, QueryResult {
    /** */
    public record TradeLoanApplicationDto(
            UUID id,
            Long version,
            Instant requestDate,
            Set<PartyEmbDto> parties,
            BigDecimal requestedAmount,
            String requestedAmountCurrency,
            String currency,
            Integer requestedLoanDurationMonths,
            ApplicantChannel applicantChannel,
            Integer gracePeriodDays,
            Integer installmentCount,
            String disburseDestinationDepositNumber,
            DisburseDestinationType disburseDestinationType,
            String economicSectorCode,
            String branchCode,
            String requestReasonCode,
            String subSourceCode,
            String description,
            String credibilityRank,
            String applicationNumber,
            DisbursementMethod disbursementMethod,
            SamatDto samat)
            implements Serializable {
        public record PartyEmbDto(
                String customerNumber,
                PartyType partyType,
                PartyRole partyRole,
                String firstName,
                String lastName,
                @Nullable BigDecimal guaranteePercentage)
                implements Serializable {}
    }

    public record TradeSanctionedLoanEntityDto(
            UUID id,
            Long version,
            String sanctionSerial,
            SanctionType sanctionSerialType,
            BigDecimal approvedAmount,
            String currency,
            Integer gracePeriodDays,
            Integer installmentCount,
            Integer loanDurationMonths,
            String lifeInsuranceId,
            List<ScheduledTrancheEmbDto> disbursementScheduleTranches,
            List<DisbursementRecordEmbDto> disbursementHistoryRecords,
            String revocationReason,
            DisbursementMethod disbursementMethod)
            implements Serializable {
        public record ScheduledTrancheEmbDto(Instant scheduledDate, BigDecimal amount, String currency)
                implements Serializable {}

        public record DisbursementRecordEmbDto(
                BigDecimal amount, String currency, LocalDate disbursedAt, String disbursedBy)
                implements Serializable {}
    }

    public record TransactionNumberEmbDto(String value, Instant createdAt, String trackingId, TransactionStatus status)
            implements Serializable {}

    public record CollateralEmbDto(
            String collateralTypeCode,
            Integer percent,
            String description,
            String collateralSerial,
            BigDecimal usedAmount,
            String usedAmountCurrency)
            implements Serializable {}

    public record SamatDto(String trackingNumber) implements Serializable {}
}
