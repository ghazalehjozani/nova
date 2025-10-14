package ir.dotin.loan.trade.core.application.ports.outbound.query.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ir.dotin.loan.baseloan.core.domain.loanarrangement.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.SanctionType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.transaction.TransactionStatus;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

/** DTO for TradeLoanFacilityEntity */
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
        AccountEmbDto disbursementDestinationAccount,
        String facilityType)
        implements Serializable {
    /** DTO for TradeLoanApplicationEntity */
    public record TradeLoanApplicationEntityDto(
            UUID id,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            String createdBy,
            String modifiedBy,
            Instant requestDate,
            PartyEmbDto customer,
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
            Set<PartyEmbDto> guarantors,
            Set<CertificateEmbDto> certificates,
            DisbursementMethod disbursementMethod)
            implements Serializable {
        /** DTO for PartyEmb */
        public record PartyEmbDto(String customerNumber, String partyType, String firstName, String lastName)
                implements Serializable {}

        /** DTO for MoneyEmb */
        public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}

        /** DTO for ir.dotin.platform.commons.domain.vo.CurrencyType */
        public record CurrencyTypeDto(String value) implements Serializable {}

        /** DTO for PeriodEmb */
        public record PeriodEmbDto(Integer years, Integer months, Integer days) implements Serializable {}

        /** DTO for GracePeriodEmb */
        public record GracePeriodEmbDto(Integer days, Integer months, Integer years) implements Serializable {}

        /** DTO for InstallmentCountEmb */
        public record InstallmentCountEmbDto(Integer value) implements Serializable {}

        /** DTO for DisburseDestinationEmb */
        public record DisburseDestinationEmbDto(String depositNumber, DisburseDestinationType type)
                implements Serializable {}

        /** DTO for EconomicSectorEmb */
        public record EconomicSectorEmbDto(String code, String name) implements Serializable {}

        /** DTO for BranchEmb */
        public record BranchEmbDto(String code, String name) implements Serializable {}

        /** DTO for RequestReasonEmb */
        public record RequestReasonEmbDto(String core, String name) implements Serializable {}

        /** DTO for SubSourceEmb */
        public record SubSourceEmbDto(String core, String name) implements Serializable {}

        /** DTO for DescriptionEmb */
        public record DescriptionEmbDto(String value) implements Serializable {}

        /** DTO for CredibilityRankEmb */
        public record CredibilityRankEmbDto(String value) implements Serializable {}

        /** DTO for ApplicationNumberEmb */
        public record ApplicationNumberEmbDto(
                BranchEmbDto branch,
                LoanTypeCodeEmbDto loanTypeCode,
                PartyEmbDto party,
                RespiteSerialEmbDto respiteSerial,
                String derivedValue)
                implements Serializable {
            /** DTO for BranchEmb */
            public record BranchEmbDto(String code, String name) implements Serializable {}

            /** DTO for LoanTypeCodeEmb */
            public record LoanTypeCodeEmbDto(String value) implements Serializable {}

            /** DTO for PartyEmb */
            public record PartyEmbDto(String customerNumber, String partyType, String firstName, String lastName)
                    implements Serializable {}

            /** DTO for RespiteSerialEmb */
            public record RespiteSerialEmbDto(String value) implements Serializable {}
        }

        /** DTO for CertificateEmb */
        public record CertificateEmbDto(String serial) implements Serializable {}
    }

    /** DTO for TradeSanctionedLoanEntity */
    public record TradeSanctionedLoanEntityDto(
            UUID id,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            String createdBy,
            String modifiedBy,
            SanctionSerialEmbDto sanctionSerial,
            MoneyEmbDto approvedAmount,
            CurrencyTypeEmbDto currency,
            GracePeriodEmbDto gracePeriod,
            InstallmentCountEmbDto installmentCount,
            PeriodEmbDto loanDuration,
            String lifeInsuranceId,
            CollateralSerialEmbDto collateralSerial,
            DisbursementScheduleEntityDto disbursementSchedule,
            RevocationReasonEmbDto revocationReason,
            DisbursementMethod disbursementMethod)
            implements Serializable {
        /** DTO for SanctionSerialEmb */
        public record SanctionSerialEmbDto(String value, SanctionType type) implements Serializable {}

        /** DTO for MoneyEmb */
        public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}

        /** DTO for CurrencyTypeEmb */
        public record CurrencyTypeEmbDto(String value) implements Serializable {}

        /** DTO for GracePeriodEmb */
        public record GracePeriodEmbDto(Integer days, Integer months, Integer years) implements Serializable {}

        /** DTO for InstallmentCountEmb */
        public record InstallmentCountEmbDto(Integer value) implements Serializable {}

        /** DTO for PeriodEmb */
        public record PeriodEmbDto(Integer years, Integer months, Integer days) implements Serializable {}

        /** DTO for CollateralSerialEmb */
        public record CollateralSerialEmbDto(String value) implements Serializable {}

        /** DTO for DisbursementScheduleEntity */
        public record DisbursementScheduleEntityDto(
                UUID id,
                Long version,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt,
                String createdBy,
                String modifiedBy,
                List<ScheduledTrancheEmbDto> tranches)
                implements Serializable {
            /** DTO for ScheduledTrancheEmb */
            public record ScheduledTrancheEmbDto(Instant scheduledDate, MoneyEmbDto amount) implements Serializable {
                /** DTO for MoneyEmb */
                public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}
            }
        }

        /** DTO for RevocationReasonEmb */
        public record RevocationReasonEmbDto(String text) implements Serializable {}
    }

    /** DTO for MoneyEmb */
    public record MoneyEmbDto(BigDecimal amount, String currency) implements Serializable {}

    /** DTO for TransactionNumberEmb */
    public record TransactionNumberEmbDto(
            String value,
            TradeRelationType relationType,
            Instant createdAt,
            String trackingId,
            TransactionStatus status)
            implements Serializable {}

    /** DTO for AccountEmb */
    public record AccountEmbDto(
            String accountNumber, String accountHolder, String bankCode, String branchCode, String iban)
            implements Serializable {}
}
