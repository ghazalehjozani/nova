package ir.dotin.loan.trade.core.application.query.loanfacility.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String createdBy,
        String modifiedBy,
        TradeLoanApplicationEntityDto loanApplication,
        TradeSanctionedLoanEntityDto sanctionedLoan,
        UUID loanTypeId,
        UUID loanArrangementId,
        FacilityStatus currentState,
        MoneyEmbDto totalDisbursedAmount,
        List<TransactionNumberEmbDto> issueContractTransactionNumbers,
        List<TransactionNumberEmbDto> disbursementTransactionNumbers,
        String facilityType)
        implements QueryResult {

    public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}

    public record CurrencyTypeDto(String value) implements Serializable {}

    public record PeriodEmbDto(Integer years, Integer months, Integer days) implements Serializable {}

    public record GracePeriodEmbDto(Integer days, Integer months, Integer years) implements Serializable {}

    public record InstallmentCountEmbDto(Integer value) implements Serializable {}

    public record PartyEmbDto(
            String customerNumber, PartyType partyType, PartyRole partyRole, String firstName, String lastName)
            implements Serializable {}

    public record TradeLoanApplicationEntityDto(
            UUID id,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            String createdBy,
            String modifiedBy,
            Instant requestDate,
            Set<PartyEmbDto> parties,
            MoneyEmbDto requestedAmount,
            CurrencyTypeDto currency,
            PeriodEmbDto requestedLoanDuration,
            ApplicantChannel applicantChannel,
            GracePeriodEmbDto gracePeriod,
            InstallmentCountEmbDto installmentCount,
            DisburseDestinationEmbDto disburseDestination,
            EconomicSectorEmbDto economicSector,
            BranchEmbDto branch,
            RequestReasonEmbDto requestReason,
            SubSourceEmbDto subSource,
            DescriptionEmbDto description,
            CredibilityRankEmbDto credibilityRank,
            ApplicationNumberEmbDto applicationNumber,
            Set<CertificateEmbDto> certificates,
            DisbursementMethod disbursementMethod)
            implements Serializable {

        public record DisburseDestinationEmbDto(String depositNumber, DisburseDestinationType type)
                implements Serializable {}

        public record EconomicSectorEmbDto(String code) implements Serializable {}

        public record BranchEmbDto(String code) implements Serializable {}

        public record RequestReasonEmbDto(String core) implements Serializable {}

        public record SubSourceEmbDto(String core) implements Serializable {}

        public record DescriptionEmbDto(String value) implements Serializable {}

        public record CredibilityRankEmbDto(String value) implements Serializable {}

        public record ApplicationNumberEmbDto(
                BranchEmbDto branch, LoanTypeCodeEmbDto loanTypeCode, PartyEmbDto party, String derivedValue)
                implements Serializable {

            public record LoanTypeCodeEmbDto(String value) implements Serializable {}
        }

        public record CertificateEmbDto(String serial) implements Serializable {}
    }

    public record TradeSanctionedLoanEntityDto(
            UUID id,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            String createdBy,
            String modifiedBy,
            SanctionSerialEmbDto sanctionSerial,
            MoneyEmbDto approvedAmount,
            CurrencyTypeDto currency,
            GracePeriodEmbDto gracePeriod,
            InstallmentCountEmbDto installmentCount,
            PeriodEmbDto loanDuration,
            String lifeInsuranceId,
            DisbursementScheduleEntityDto disbursementSchedule,
            RevocationReasonEmbDto revocationReason,
            DisbursementMethod disbursementMethod)
            implements Serializable {

        public record SanctionSerialEmbDto(String value, SanctionType type) implements Serializable {}

        public record CollateralSerialEmbDto(String value) implements Serializable {}

        public record DisbursementScheduleEntityDto(
                UUID id,
                Long version,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt,
                String createdBy,
                String modifiedBy,
                List<ScheduledTrancheEmbDto> tranches)
                implements Serializable {

            public record ScheduledTrancheEmbDto(Instant scheduledDate, MoneyEmbDto amount) implements Serializable {}
        }

        public record RevocationReasonEmbDto(String text) implements Serializable {}
    }

    public record TransactionNumberEmbDto(String value, Instant createdAt, String trackingId, TransactionStatus status)
            implements Serializable {}
}
