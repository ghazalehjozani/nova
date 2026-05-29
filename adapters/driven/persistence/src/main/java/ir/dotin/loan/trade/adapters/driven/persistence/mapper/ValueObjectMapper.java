package ir.dotin.loan.trade.adapters.driven.persistence.mapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import org.jspecify.annotations.Nullable;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ir.dotin.platform.accounting.document.api.enumeration.RelationType;
import ir.dotin.platform.accounting.document.api.model.AccountId;
import ir.dotin.platform.accounting.document.api.model.AccountNumber;
import ir.dotin.platform.accounting.document.api.model.DepositNumber;
import ir.dotin.platform.formula.api.FormulaId;
import ir.dotin.platform.formula.infrastructure.persistence.embeddable.FormulaIdRefEmb;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.commons.domain.vo.Money;
import ir.dotin.platform.pangaea.commons.domain.vo.Rate;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.AmountRangeEmb;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.DurationRangeEmb;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.InterestRateEmb;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.PeriodEmb;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentAmount;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.InstallmentId;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.RestructuringRecord;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.vo.ScheduleHistory;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.CollateralPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.GracePeriodPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPeriod;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InstallmentPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.InterestPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.LoanArrangementCode;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.PenaltyPolicy;
import ir.dotin.loan.baseloan.core.domain.loanarrangement.vo.RegulatoryCompliancePolicy;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.AccountDisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.ApplicationNumber;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Branch;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CloseFacilityPaidOffInfo;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Collateral;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CollateralSerial;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.CredibilityRank;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DepositDisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Description;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisburseDestination;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisbursementHistory;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisbursementRecord;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisbursementScheduleId;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.GracePeriod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.InstallmentCount;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanTypeCode;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RequestReason;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.RevocationReason;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.Samat;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.SubSource;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.EconomicSectorCurrency;
import ir.dotin.loan.baseloan.core.domain.loantype.vo.LoanApplicationStatus;
import ir.dotin.loan.baseloan.core.domain.shared.enums.InstallmentPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PenaltyPaymentType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Attribute;
import ir.dotin.loan.baseloan.core.domain.shared.vo.CancellationData;
import ir.dotin.loan.baseloan.core.domain.shared.vo.ConfirmType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Disable;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EconomicSector;
import ir.dotin.loan.baseloan.core.domain.shared.vo.EditReason;
import ir.dotin.loan.baseloan.core.domain.shared.vo.IncomeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.InstallmentScheduleId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LifeInsuranceId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanApplicationId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanArrangementId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTopic;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeGroupId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanTypeId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.RespiteSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionSerial;
import ir.dotin.loan.baseloan.core.domain.shared.vo.SanctionedLoanId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Title;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.ApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CoApplicantParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.CustomerName;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuaranteePercentage;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.GuarantorParty;
import ir.dotin.loan.baseloan.core.domain.shared.vo.customer.Party;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationNumberEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationPartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.AttributeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.BranchEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CancellationDataEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CloseFacilityPaidOffInfoEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CollateralSerialEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ConfirmTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CredibilityRankEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DescriptionEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisburseDestinationEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisbursementHistoryEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisbursementRecordEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorCurrencyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EditReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentAmountEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentCountEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InterestPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.LoanApplicationStatusEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.LoanTypeCodeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PenaltyPolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PeriodRangeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RegulatoryCompliancePolicyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RelationTypeLoanTopicEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RequestReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RespiteSerialEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RestructuringRecordEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RevocationReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.SamatEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.SanctionSerialEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ScheduleHistoryEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.SubSourceEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TitleEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TransactionNumberEmb;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;

@SuppressWarnings("unused")
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class ValueObjectMapper {

    @Mapping(source = "value", target = "amount")
    @Mapping(source = "currency.code", target = "currency")
    public abstract MoneyEmb toMoneyEmb(Money money);

    @Mapping(source = "amount", target = "value")
    @Mapping(source = "currency", target = "currency", qualifiedByName = "stringToCurrency")
    public abstract Money toMoney(MoneyEmb embeddable);

    public FormulaIdRefEmb toFormulaEmb(FormulaId formula) {
        return FormulaIdRefEmb.of(formula);
    }

    @Mapping(source = "value", target = "value")
    public abstract FormulaId toFormulaId(FormulaIdRefEmb embeddable);

    @Mapping(source = "value", target = "value")
    public abstract InterestRateEmb toInterestRateEmb(Rate rate);

    @Mapping(source = "value", target = "value")
    public abstract Rate toRate(InterestRateEmb embeddable);

    public abstract ApplicationNumberEmb toApplicationNumberEmb(ApplicationNumber applicationNumber);

    public abstract ApplicationNumber toApplicationNumber(ApplicationNumberEmb applicationNumberEmb);

    public abstract RespiteSerialEmb toRespiteSerialEmb(RespiteSerial respiteSerial);

    public @Nullable RespiteSerialEmb toRespiteSerialEmb(Optional<RespiteSerial> respiteSerialOpt) {
        return respiteSerialOpt.map(this::toRespiteSerialEmb).orElse(null);
    }

    public abstract RespiteSerial toRespiteSerial(RespiteSerialEmb respiteSerialEmb);

    public Optional<RespiteSerial> mapToOptionalRespiteSerial(RespiteSerialEmb value) {
        return Optional.of(toRespiteSerial(value));
    }

    public @Nullable ApplicationNumberEmb toApplicationNumberEmb(Optional<ApplicationNumber> applicationNumberOpt) {
        return applicationNumberOpt.map(this::toApplicationNumberEmb).orElse(null);
    }

    @Mapping(source = "code.value", target = "code")
    public abstract BranchEmb toBranchEmb(Branch branch);

    @Mapping(source = "code", target = "code.value")
    public abstract Branch toBranch(BranchEmb embeddable);

    public abstract CollateralSerialEmb toCollateralSerialEmb(CollateralSerial collateralSerial);

    public @Nullable CollateralSerialEmb toCollateralSerialEmb(Optional<CollateralSerial> collateralSerial) {
        return collateralSerial.map(this::toCollateralSerialEmb).orElse(null);
    }

    @Named("toScheduleHistoryEmb")
    public @Nullable ScheduleHistoryEmb toScheduleHistoryEmb(ScheduleHistory scheduleHistory) {
        if (scheduleHistory == null) {
            return null;
        }
        ScheduleHistoryEmb emb = new ScheduleHistoryEmb();
        emb.setPreviousScheduleIds(scheduleHistory.getScheduleIds().stream()
                .map(InstallmentScheduleId::value)
                .toList());
        return emb;
    }

    @Named("toScheduleHistory")
    public ScheduleHistory toScheduleHistory(ScheduleHistoryEmb emb) {
        if (emb == null || emb.getPreviousScheduleIds() == null) {
            return ScheduleHistory.empty();
        }
        List<InstallmentScheduleId> scheduleIds = emb.getPreviousScheduleIds().stream()
                .map(InstallmentScheduleId::new)
                .collect(Collectors.toList());
        return ScheduleHistory.of(scheduleIds);
    }

    @Named("toDisbursementHistoryEmb")
    public @Nullable DisbursementHistoryEmb toDisbursementHistoryEmb(DisbursementHistory history) {
        if (history == null) {
            return null;
        }
        DisbursementHistoryEmb emb = new DisbursementHistoryEmb();
        emb.setRecords(
                history.getRecords().stream().map(this::toDisbursementRecordEmb).collect(Collectors.toList()));
        return emb;
    }

    @Named("toDisbursementHistory")
    public DisbursementHistory toDisbursementHistory(DisbursementHistoryEmb emb) {
        if (emb == null || emb.getRecords() == null) {
            return DisbursementHistory.empty();
        }
        List<DisbursementRecord> records =
                emb.getRecords().stream().map(this::toDisbursementRecord).collect(Collectors.toList());
        return DisbursementHistory.of(records);
    }

    public @Nullable DisbursementRecordEmb toDisbursementRecordEmb(DisbursementRecord record) {
        if (record == null) return null;
        DisbursementRecordEmb emb = new DisbursementRecordEmb();
        emb.setAmount(toMoneyEmb(record.amount()));
        emb.setDisbursedAt(record.disbursedAt());
        emb.setDisbursedBy(record.disbursedBy());
        return emb;
    }

    public @Nullable DisbursementRecord toDisbursementRecord(DisbursementRecordEmb emb) {
        if (emb == null) return null;
        return new DisbursementRecord(
                toMoney(Objects.requireNonNull(emb.getAmount(), "disbursementRecord.amount")),
                Objects.requireNonNull(emb.getDisbursedAt(), "disbursementRecord.disbursedAt"),
                Objects.requireNonNull(emb.getDisbursedBy(), "disbursementRecord.disbursedBy"));
    }

    @Named("toRestructuringRecordEmb")
    public @Nullable RestructuringRecordEmb toRestructuringRecordEmb(RestructuringRecord record) {
        if (record == null) return null;

        RestructuringRecordEmb emb = new RestructuringRecordEmb();
        emb.setReason(record.reason());
        emb.setRestructuringAmount(record.addedPrincipal().value());
        emb.setRestructuringAmountCurrency(record.addedPrincipal().currency().getCode());
        emb.setPreviousInstallmentCount(record.previousInstallmentCount());
        emb.setNewInstallmentCount(record.newInstallmentCount());
        emb.setUnpaidInstallmentsCount(record.unpaidInstallmentsCount());
        emb.setPreservedInstallmentsCount(record.preservedInstallmentsCount());
        emb.setRestructuredAt(record.restructuredAt());
        emb.setRestructuredBy(record.restructuredBy());
        return emb;
    }

    @Named("toRestructuringRecord")
    public @Nullable RestructuringRecord toRestructuringRecord(RestructuringRecordEmb emb) {
        if (emb == null) return null;

        Money addedPrincipal = Money.valueOf(
                        Objects.requireNonNull(emb.getRestructuringAmount(), "restructuringAmount"),
                        CurrencyType.valueOf(Objects.requireNonNull(
                                        emb.getRestructuringAmountCurrency(), "restructuringAmountCurrency"))
                                .unwrap())
                .unwrap();

        return new RestructuringRecord(
                Objects.requireNonNull(emb.getReason(), "reason"),
                addedPrincipal,
                Objects.requireNonNull(emb.getPreviousInstallmentCount(), "previousInstallmentCount"),
                Objects.requireNonNull(emb.getNewInstallmentCount(), "newInstallmentCount"),
                Objects.requireNonNull(emb.getUnpaidInstallmentsCount(), "unpaidInstallmentsCount"),
                Objects.requireNonNull(emb.getPreservedInstallmentsCount(), "preservedInstallmentsCount"),
                Objects.requireNonNull(emb.getRestructuredAt(), "restructuredAt"),
                Objects.requireNonNull(emb.getRestructuredBy(), "restructuredBy"));
    }

    public abstract CollateralSerial toCollateralSerial(CollateralSerialEmb embeddable);

    public abstract CredibilityRankEmb toCredibilityRankEmb(CredibilityRank credibilityRank);

    public @Nullable CredibilityRankEmb toCredibilityRankEmb(Optional<CredibilityRank> credibilityRankOpt) {
        return credibilityRankOpt.map(this::toCredibilityRankEmb).orElse(null);
    }

    public abstract CredibilityRank toCredibilityRank(CredibilityRankEmb embeddable);

    public abstract DescriptionEmb toDescriptionEmb(Description description);

    public @Nullable DescriptionEmb toDescriptionEmb(Optional<Description> descriptionOpt) {
        return descriptionOpt.map(this::toDescriptionEmb).orElse(null);
    }

    public abstract Description toDescription(DescriptionEmb embeddable);

    public DisburseDestinationEmb toDisburseDestinationEmb(DisburseDestination destination) {
        return switch (destination) {
            case AccountDisburseDestination(var accountNumber) -> {
                var emb = new DisburseDestinationEmb();
                emb.setType(DisburseDestinationType.ACCOUNT);
                emb.setAccountNumber(accountNumber.orElseThrow().accountNumber());
                yield emb;
            }
            case DepositDisburseDestination(var depositNumber) -> {
                var emb = new DisburseDestinationEmb();
                emb.setType(DisburseDestinationType.DEPOSIT);
                emb.setDepositNumber(depositNumber.orElseThrow().value());
                yield emb;
            }
        };
    }

    public DisburseDestination toDisburseDestination(DisburseDestinationEmb emb) {
        return switch (Objects.requireNonNull(emb.getType(), "disburseDestinationType")) {
            case DEPOSIT ->
                DepositDisburseDestination.of(
                                DepositNumber.valueOf(Objects.requireNonNull(emb.getDepositNumber(), "depositNumber"))
                                        .unwrap())
                        .unwrap();
            case ACCOUNT ->
                AccountDisburseDestination.of(
                                AccountNumber.of(Objects.requireNonNull(emb.getAccountNumber(), "accountNumber"))
                                        .unwrap())
                        .unwrap();
        };
    }

    @Named("optionalDepositNumberToString")
    public @Nullable String optionalDepositNumberToString(Optional<DepositNumber> depositNumber) {
        return depositNumber.map(DepositNumber::value).orElse(null);
    }

    @Named("stringToOptionalDepositNumber")
    public Optional<DepositNumber> stringToOptionalDepositNumber(String depositNumber) {
        if (depositNumber == null || depositNumber.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new DepositNumber(depositNumber));
    }

    public @Nullable GracePeriodEmb toGracePeriodEmb(GracePeriod gracePeriod) {
        if (gracePeriod == null) {
            return null;
        }
        return periodToEmb(gracePeriod.value());
    }

    public @Nullable GracePeriod toGracePeriod(GracePeriodEmb embeddable) {
        if (embeddable == null) {
            return null;
        }
        Period period = embToPeriod(embeddable);
        return new GracePeriod(period);
    }

    public abstract InstallmentCountEmb toInstallmentCountEmb(InstallmentCount installmentCount);

    public abstract InstallmentCount toInstallmentCount(InstallmentCountEmb embeddable);

    public abstract LoanTypeCodeEmb toLoanTypeCodeEmb(LoanTypeCode loanTypeCode);

    public abstract LoanTypeCode toLoanTypeCode(LoanTypeCodeEmb embeddable);

    @Mapping(source = "code", target = "code")
    public abstract RequestReasonEmb toRequestReasonEmb(RequestReason requestReason);

    @Mapping(source = "code", target = "code")
    public abstract RequestReason toRequestReason(RequestReasonEmb embeddable);

    public abstract SanctionSerialEmb toSanctionSerialEmb(SanctionSerial sanctionSerial);

    public abstract SanctionSerial toSanctionSerial(SanctionSerialEmb embeddable);

    @Mapping(source = "code", target = "code")
    public abstract SubSourceEmb toSubSourceEmb(SubSource subSource);

    public @Nullable SubSourceEmb toSubSourceEmb(Optional<SubSource> subSourceOpt) {
        return subSourceOpt.map(this::toSubSourceEmb).orElse(null);
    }

    @Mapping(source = "code", target = "code")
    public abstract SubSource toSubSource(SubSourceEmb embeddable);

    public abstract TitleEmb toTitleEmb(Title title);

    public abstract Title toTitle(TitleEmb embeddable);

    public abstract AttributeEmb toAttributeEmb(Attribute attribute);

    public abstract Attribute toAttribute(AttributeEmb embeddable);

    public abstract ConfirmTypeEmb toConfirmTypeEmb(ConfirmType confirmType);

    public abstract ConfirmType toConfirmType(ConfirmTypeEmb embeddable);

    public abstract EditReasonEmb toEditReasonEmb(EditReason editReason);

    public abstract EditReason toEditReason(EditReasonEmb embeddable);

    public abstract RevocationReasonEmb toRevocationReasonEmb(RevocationReason editReason);

    public abstract RevocationReason toRevocationReason(RevocationReasonEmb embeddable);

    public PartyEmb toPartyEmb(Party party) {
        PartyEmb emb = new PartyEmb();
        emb.setCustomerNumber(party.customerNumber());
        emb.setPartyType(party.partyType());
        emb.setPartyRole(party.partyRole());
        emb.setFirstName(party.name().firstName());
        emb.setLastName(party.name().lastName());
        emb.setCompanyName(party.name().companyName());

        if (party instanceof GuarantorParty guarantor) {
            emb.setGuaranteePercentage(guarantor.guaranteePercentage().value());
        }

        return emb;
    }

    @SuppressWarnings(
            "NullAway") // CustomerName record in @NullMarked base-loan uses @NonNull params but is designed to accept
    // nullable names (firstName/lastName/companyName); see CustomerName#fullName null guards
    public Party toParty(PartyEmb emb) {
        CustomerName name = new CustomerName(emb.getFirstName(), emb.getLastName(), emb.getCompanyName());
        BigDecimal guaranteePct = emb.getGuaranteePercentage();

        return switch (Objects.requireNonNull(emb.getPartyRole(), "partyRole")) {
            case PRIMARY_APPLICANT ->
                new ApplicantParty(
                        Objects.requireNonNull(emb.getCustomerNumber(), "customerNumber"),
                        Objects.requireNonNull(emb.getPartyType(), "partyType"),
                        name);
            case CO_APPLICANT ->
                new CoApplicantParty(
                        Objects.requireNonNull(emb.getCustomerNumber(), "customerNumber"),
                        Objects.requireNonNull(emb.getPartyType(), "partyType"),
                        name);
            case GUARANTOR ->
                GuarantorParty.of(
                                Objects.requireNonNull(emb.getCustomerNumber(), "customerNumber"),
                                Objects.requireNonNull(emb.getPartyType(), "partyType"),
                                name,
                                guaranteePct != null ? GuaranteePercentage.of(guaranteePct) : null)
                        .unwrap();
        };
    }

    public ApplicationPartyEmb toApplicationPartyEmb(Party party) {
        ApplicationPartyEmb emb = new ApplicationPartyEmb();
        emb.setCustomerNumber(party.customerNumber());
        emb.setPartyType(party.partyType());
        emb.setPartyRole(party.partyRole());
        emb.setFirstName(party.name().firstName());
        emb.setLastName(party.name().lastName());
        emb.setCompanyName(party.name().companyName());
        return emb;
    }

    @SuppressWarnings(
            "NullAway") // CustomerName record in @NullMarked base-loan uses @NonNull params but is designed to accept
    // nullable names (firstName/lastName/companyName); see CustomerName#fullName null guards
    public Party toParty(ApplicationPartyEmb emb) {
        CustomerName name = new CustomerName(emb.getFirstName(), emb.getLastName(), emb.getCompanyName());

        return switch (Objects.requireNonNull(emb.getPartyRole(), "partyRole")) {
            case PRIMARY_APPLICANT ->
                new ApplicantParty(
                        Objects.requireNonNull(emb.getCustomerNumber(), "customerNumber"),
                        Objects.requireNonNull(emb.getPartyType(), "partyType"),
                        name);
            case CO_APPLICANT, GUARANTOR ->
                throw new UnsupportedOperationException("Application number should use primary applicant!");
        };
    }

    @Mapping(source = "economicSector.code", target = "economicSectorCode")
    public abstract EconomicSectorCurrencyEmb toEconomicSectorCurrencyEmb(
            EconomicSectorCurrency economicSectorCurrency);

    @Mapping(source = "economicSectorCode", target = "economicSector.code")
    public abstract EconomicSectorCurrency toEconomicSectorCurrency(EconomicSectorCurrencyEmb embeddable);

    public abstract LoanApplicationStatusEmb toLoanApplicationStatusEmb(LoanApplicationStatus loanApplicationStatus);

    public abstract LoanApplicationStatus toLoanApplicationStatus(LoanApplicationStatusEmb embeddable);

    public abstract InstallmentAmountEmb toInstallmentAmountEmb(InstallmentAmount installmentAmount);

    public abstract InstallmentAmount toInstallmentAmount(InstallmentAmountEmb embeddable);

    public abstract TransactionNumberEmb toTransactionNumberEmb(TrackedTransactionNumber trackedTransactionNumber);

    public abstract TrackedTransactionNumber toTrackedTransactionNumber(TransactionNumberEmb embeddable);

    @Named("mapAmountRangeToEmb")
    public @Nullable AmountRangeEmb mapAmountRangeToEmb(Range<Money> amountRange) {
        if (amountRange == null || !amountRange.hasLowerBound() || !amountRange.hasUpperBound()) {
            return null;
        }
        AmountRangeEmb emb = new AmountRangeEmb();
        emb.setMinAmount(amountRange.lowerEndpoint().value());
        emb.setMaxAmount(amountRange.upperEndpoint().value());
        emb.setCurrency(amountRange.lowerEndpoint().currency().getCode());
        return emb;
    }

    @Named("mapInterestPolicyToEmb")
    public @Nullable InterestPolicyEmb mapInterestPolicyToEmb(InterestPolicy policy) {
        if (policy == null) return null;
        InterestPolicyEmb emb = new InterestPolicyEmb();
        emb.setBaseInterestRate(policy.baseInterestRate().value());
        emb.setPreferentialMinRate(
                policy.preferentialRangeRate().lowerEndpoint().value());
        emb.setPreferentialMaxRate(
                policy.preferentialRangeRate().upperEndpoint().value());
        emb.setInterestFormula(toFormulaEmb(policy.interestFormula()));
        emb.setRefundInterestFormula(toFormulaEmb(policy.refundInterestFormula()));
        emb.setDailyInterest(policy.dailyInterest());
        return emb;
    }

    @Named("mapInstallmentPolicyToEmb")
    public @Nullable InstallmentPolicyEmb mapInstallmentPolicyToEmb(InstallmentPolicy policy) {
        if (policy == null) return null;
        InstallmentPolicyEmb emb = new InstallmentPolicyEmb();
        emb.setInstallmentPeriodDays(policy.installmentPeriod().value().getDays());
        emb.setInstallmentFormula(toFormulaEmb(policy.installmentFormula()));
        emb.setInterestComponentFormula(toFormulaEmb(policy.interestComponentFormula()));
        emb.setInstallmentPaymentType(policy.installmentPaymentType().name());
        return emb;
    }

    @Named("mapGracePeriodPolicyToEmb")
    public @Nullable GracePeriodPolicyEmb mapGracePeriodPolicyToEmb(GracePeriodPolicy policy) {
        if (policy == null) return null;
        GracePeriodPolicyEmb emb = new GracePeriodPolicyEmb();
        emb.setMinGracePeriodDays(policy.minGracePeriod().getDays());
        emb.setMaxGracePeriodDays(policy.maxGracePeriod().getDays());
        emb.setGracePeriodFormula(toFormulaEmb(policy.gracePeriodFormula()));
        return emb;
    }

    @Named("mapPenaltyPolicyToEmb")
    public @Nullable PenaltyPolicyEmb mapPenaltyPolicyToEmb(PenaltyPolicy policy) {
        if (policy == null) return null;
        PenaltyPolicyEmb emb = new PenaltyPolicyEmb();
        emb.setPenaltyRate(policy.penaltyRate().value());
        emb.setDeferralInterestRate(policy.deferralInterestRate().value());
        emb.setPenaltyFormula(toFormulaEmb(policy.penaltyFormula()));
        emb.setPenaltyPaymentType(policy.penaltyPaymentType().name());
        return emb;
    }

    @Named("mapCollateralPolicyToEmb")
    public @Nullable CollateralPolicyEmb mapCollateralPolicyToEmb(CollateralPolicy policy) {
        if (policy == null) return null;
        CollateralPolicyEmb emb = new CollateralPolicyEmb();
        emb.setTotalPercent(policy.totalPercent());
        emb.setCollateralTypes(new HashSet<>(policy.collateralTypes()));
        emb.setCollateralCalculationType(policy.collateralCalculationType());
        return emb;
    }

    @Named("mapAmountRangeEmbToRange")
    public @Nullable Range<Money> mapAmountRangeEmbToRange(AmountRangeEmb emb) {
        if (emb == null || emb.getMinAmount() == null || emb.getMaxAmount() == null || emb.getCurrency() == null) {
            return null;
        }
        CurrencyType currency = CurrencyType.valueOf(emb.getCurrency()).unwrap();
        Money min = Money.valueOf(emb.getMinAmount(), currency).unwrap();
        Money max = Money.valueOf(emb.getMaxAmount(), currency).unwrap();
        return Range.closed(min, max);
    }

    @Named("mapInterestPolicyEmbToPolicy")
    public @Nullable InterestPolicy mapInterestPolicyEmbToPolicy(InterestPolicyEmb emb) {
        if (emb == null) return null;
        Rate baseRate = Rate.valueOf(Objects.requireNonNull(emb.getBaseInterestRate(), "baseInterestRate"))
                .unwrap();
        Rate minRate = Rate.valueOf(Objects.requireNonNull(emb.getPreferentialMinRate(), "preferentialMinRate"))
                .unwrap();
        Rate maxRate = Rate.valueOf(Objects.requireNonNull(emb.getPreferentialMaxRate(), "preferentialMaxRate"))
                .unwrap();
        Range<Rate> preferentialRange = Range.closed(minRate, maxRate);
        return InterestPolicy.of(
                        baseRate,
                        preferentialRange,
                        toFormulaId(Objects.requireNonNull(emb.getInterestFormula(), "interestFormula")),
                        toFormulaId(Objects.requireNonNull(emb.getRefundInterestFormula(), "refundInterestFormula")),
                        Objects.requireNonNull(emb.getDailyInterest(), "dailyInterest"))
                .unwrap();
    }

    @Named("mapInstallmentPolicyEmbToPolicy")
    public @Nullable InstallmentPolicy mapInstallmentPolicyEmbToPolicy(InstallmentPolicyEmb emb) {
        if (emb == null) return null;
        InstallmentPeriod installmentPeriod = InstallmentPeriod.of(
                        Period.ofDays(Objects.requireNonNull(emb.getInstallmentPeriodDays(), "installmentPeriodDays")))
                .unwrap();
        return InstallmentPolicy.of(
                        installmentPeriod,
                        toFormulaId(Objects.requireNonNull(emb.getInstallmentFormula(), "installmentFormula")),
                        toFormulaId(
                                Objects.requireNonNull(emb.getInterestComponentFormula(), "interestComponentFormula")),
                        InstallmentPaymentType.valueOf(
                                Objects.requireNonNull(emb.getInstallmentPaymentType(), "installmentPaymentType")))
                .unwrap();
    }

    @Named("mapGracePeriodPolicyEmbToPolicy")
    public @Nullable GracePeriodPolicy mapGracePeriodPolicyEmbToPolicy(GracePeriodPolicyEmb emb) {
        if (emb == null) return null;
        return GracePeriodPolicy.of(
                        Period.ofDays(Objects.requireNonNull(emb.getMinGracePeriodDays(), "minGracePeriodDays")),
                        Period.ofDays(Objects.requireNonNull(emb.getMaxGracePeriodDays(), "maxGracePeriodDays")),
                        toFormulaId(Objects.requireNonNull(emb.getGracePeriodFormula(), "gracePeriodFormula")))
                .unwrap();
    }

    @Named("mapPenaltyPolicyEmbToPolicy")
    public @Nullable PenaltyPolicy mapPenaltyPolicyEmbToPolicy(PenaltyPolicyEmb emb) {
        if (emb == null) return null;
        Rate penaltyRate = Rate.valueOf(Objects.requireNonNull(emb.getPenaltyRate(), "penaltyRate"))
                .unwrap();
        Rate deferralRate = Rate.valueOf(Objects.requireNonNull(emb.getDeferralInterestRate(), "deferralInterestRate"))
                .unwrap();
        PenaltyPaymentType paymentType =
                PenaltyPaymentType.valueOf(Objects.requireNonNull(emb.getPenaltyPaymentType(), "penaltyPaymentType"));
        return PenaltyPolicy.of(
                        penaltyRate,
                        deferralRate,
                        toFormulaId(Objects.requireNonNull(emb.getPenaltyFormula(), "penaltyFormula")),
                        paymentType)
                .unwrap();
    }

    @Named("mapCollateralPolicyEmbToPolicy")
    public @Nullable CollateralPolicy mapCollateralPolicyEmbToPolicy(CollateralPolicyEmb emb) {
        if (emb == null) return null;
        return CollateralPolicy.of(
                        new ArrayList<>(emb.getCollateralTypes()),
                        Objects.requireNonNull(emb.getTotalPercent(), "totalPercent"),
                        Objects.requireNonNull(emb.getCollateralCalculationType(), "collateralCalculationType"))
                .unwrap();
    }

    @Named("toTransactionNumberEmbList")
    public List<TransactionNumberEmb> toTransactionNumberEmbList(
            List<TrackedTransactionNumber> trackedTransactionNumbers) {
        if (trackedTransactionNumbers == null || trackedTransactionNumbers.isEmpty()) {
            return List.of();
        }
        return trackedTransactionNumbers.stream()
                .map(this::toTransactionNumberEmb)
                .collect(Collectors.toList());
    }

    public abstract RegulatoryCompliancePolicyEmb toRegulatoryCompliancePolicy(RegulatoryCompliancePolicy policy);

    public List<TrackedTransactionNumber> toTrackedTransactionNumber(List<TransactionNumberEmb> embeddables) {
        if (embeddables == null || embeddables.isEmpty()) {
            return new ArrayList<>();
        }
        return embeddables.stream().map(this::toTrackedTransactionNumber).collect(Collectors.toList());
    }

    @Named("periodToEmb")
    public GracePeriodEmb periodToEmb(Period period) {
        if (period == null) {
            return new GracePeriodEmb();
        }
        GracePeriodEmb emb = new GracePeriodEmb();
        emb.setDays(period.getDays());
        emb.setMonths(period.getMonths());
        emb.setYears(period.getYears());
        return emb;
    }

    @Named("embToPeriod")
    public Period embToPeriod(GracePeriodEmb emb) {
        if (emb == null) {
            return Period.ZERO;
        }
        int days = emb.getDays() != null ? emb.getDays() : 0;
        int months = emb.getMonths() != null ? emb.getMonths() : 0;
        int years = emb.getYears() != null ? emb.getYears() : 0;
        return Period.of(years, months, days);
    }

    @Named("stringToCurrency")
    public CurrencyType stringToCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return CurrencyType.IRR;
        }
        return CurrencyType.valueOf(currencyCode).unwrapOr(CurrencyType.IRR);
    }

    public CurrencyType currencyTypeEmbToCurrencyType(CurrencyTypeEmb currencyTypeEmb) {
        return CurrencyType.valueOf(Objects.requireNonNull(currencyTypeEmb.getValue(), "currencyType.value"))
                .unwrapOr(CurrencyType.IRR);
    }

    @Named("embToTrackedTransactionNumber")
    public TrackedTransactionNumber embToTrackedTransactionNumber(TransactionNumberEmb emb) {
        return new TrackedTransactionNumber(
                Objects.requireNonNull(emb.getValue(), "transactionNumber.value"),
                Objects.requireNonNull(emb.getCreatedAt(), "transactionNumber.createdAt"),
                Objects.requireNonNull(emb.getTrackingId(), "transactionNumber.trackingId"),
                Objects.requireNonNull(emb.getStatus(), "transactionNumber.status"));
    }

    public @Nullable UUID map(LoanArrangementId value) {
        return value != null ? value.value() : null;
    }

    public @Nullable LoanArrangementId mapToLoanArrangementId(UUID value) {
        return value != null ? LoanArrangementId.of(value) : null;
    }

    public @Nullable UUID map(LoanTypeId value) {
        return value != null ? value.value() : null;
    }

    public @Nullable LoanTypeId mapToLoanTypeId(UUID value) {
        return value != null ? LoanTypeId.of(value) : null;
    }

    public @Nullable UUID map(LoanApplicationId value) {
        return value != null ? value.value() : null;
    }

    public @Nullable LoanApplicationId mapToLoanApplicationId(UUID value) {
        return value != null ? LoanApplicationId.of(value) : null;
    }

    public @Nullable UUID map(LoanFacilityId value) {
        return value != null ? value.value() : null;
    }

    public @Nullable LoanFacilityId mapToLoanFacilityId(UUID value) {
        return value != null ? LoanFacilityId.of(value) : null;
    }

    public @Nullable Boolean map(Active value) {
        return value != null ? value.isActive() : null;
    }

    public @Nullable Active mapToActive(Boolean value) {
        return value != null ? Active.of(value).unwrap() : null;
    }

    public @Nullable Boolean map(Disable value) {
        return value != null ? value.isDisable() : null;
    }

    public @Nullable Disable mapToDisable(Boolean value) {
        return value != null ? Disable.of(value).unwrap() : null;
    }

    public @Nullable BigDecimal mapRateValue(Rate rate) {
        return rate != null ? rate.value() : null;
    }

    public @Nullable Rate mapToRateFromBigDecimal(BigDecimal value) {
        return value != null ? Rate.valueOf(value).unwrap() : null;
    }

    public @Nullable Long mapDurationToDays(Duration duration) {
        return duration != null ? duration.toDays() : null;
    }

    public @Nullable Duration mapDaysToDuration(Long days) {
        return days != null ? Duration.ofDays(days) : null;
    }

    public @Nullable Set<String> mapCurrencyTypesToStrings(Set<CurrencyType> currencies) {
        return currencies != null
                ? currencies.stream().map(CurrencyType::getCode).collect(Collectors.toSet())
                : null;
    }

    public @Nullable Set<CurrencyType> mapStringsToCurrencies(Set<String> currencies) {
        return currencies != null
                ? currencies.stream()
                        .map(currencyCode -> CurrencyType.valueOf(currencyCode).unwrap())
                        .collect(Collectors.toSet())
                : null;
    }

    @Named("periodRangeEmbToLoanDuration")
    public @Nullable Range<LoanDuration> mapPeriodRangeEmbToLoanDuration(PeriodRangeEmb emb) {
        if (emb == null) return null;

        LoanDuration minDuration = Objects.requireNonNull(
                mapPeriodEmbToLoanDuration(Objects.requireNonNull(emb.getMinPeriod(), "minPeriod")), "minDuration");
        LoanDuration maxDuration = Objects.requireNonNull(
                mapPeriodEmbToLoanDuration(Objects.requireNonNull(emb.getMaxPeriod(), "maxPeriod")), "maxDuration");

        return Range.closed(minDuration, maxDuration);
    }

    @Named("loanDurationToPeriodRangeEmb")
    public @Nullable PeriodRangeEmb mapLoanDurationToPeriodRangeEmb(Range<LoanDuration> loanDuration) {
        if (loanDuration == null) return null;

        PeriodRangeEmb emb = new PeriodRangeEmb();
        emb.setMinPeriod(mapLoanDurationToPeriodEmb(loanDuration.lowerEndpoint()));
        emb.setMaxPeriod(mapLoanDurationToPeriodEmb(loanDuration.upperEndpoint()));

        return emb;
    }

    public @Nullable LoanDuration mapPeriodEmbToLoanDuration(PeriodEmb emb) {
        if (emb == null) return null;
        Period period = Period.of(
                emb.getYears() != null ? emb.getYears() : 0,
                emb.getMonths() != null ? emb.getMonths() : 0,
                emb.getDays() != null ? emb.getDays() : 0);
        return LoanDuration.of(period).unwrap();
    }

    public @Nullable PeriodEmb mapLoanDurationToPeriodEmb(LoanDuration loanDuration) {
        if (loanDuration == null) return null;
        PeriodEmb emb = new PeriodEmb();
        emb.setDays(loanDuration.value().getDays());
        emb.setMonths(loanDuration.value().getMonths());
        emb.setYears(loanDuration.value().getYears());
        return emb;
    }

    @Named("mapDurationRangeToEmb")
    public @Nullable DurationRangeEmb mapDurationRangeToEmb(Range<Duration> durationRange) {
        if (durationRange == null || !durationRange.hasLowerBound() || !durationRange.hasUpperBound()) {
            return null;
        }
        DurationRangeEmb emb = new DurationRangeEmb();
        emb.setMinDurationDays(durationRange.lowerEndpoint().toDays());
        emb.setMaxDurationDays(durationRange.upperEndpoint().toDays());
        return emb;
    }

    public @Nullable Boolean map(LoanApplicationStatus value) {
        return value != null ? value.isAllowed() : null;
    }

    public @Nullable LoanApplicationStatus mapToLoanApplicationStatus(Boolean value) {
        return value != null ? (value ? LoanApplicationStatus.ALLOWED : LoanApplicationStatus.NOT_ALLOWED) : null;
    }

    public @Nullable UUID map(IncomeId value) {
        return value != null ? value.value() : null;
    }

    public @Nullable IncomeId mapToIncomeId(UUID value) {
        return value != null ? IncomeId.of(value).unwrap() : null;
    }

    public @Nullable UUID map(LoanTypeGroupId value) {
        return value != null ? value.value() : null;
    }

    public @Nullable LoanTypeGroupId mapToLoanTypeGroupId(UUID value) {
        return value != null ? LoanTypeGroupId.of(value).unwrap() : null;
    }

    public @Nullable Set<UUID> mapIncomeIdsToUUIDs(Set<IncomeId> value) {
        return value != null ? value.stream().map(this::map).collect(Collectors.toSet()) : null;
    }

    public @Nullable Set<IncomeId> mapUUIDsToIncomeIds(Set<UUID> value) {
        return value != null ? value.stream().map(this::mapToIncomeId).collect(Collectors.toSet()) : null;
    }

    public @Nullable Set<UUID> mapLoanArrangementIdsToUUIDs(Set<LoanArrangementId> value) {
        return value != null ? value.stream().map(this::map).collect(Collectors.toSet()) : null;
    }

    public @Nullable Set<LoanArrangementId> mapUUIDsToLoanArrangementIds(Set<UUID> value) {
        return value != null ? value.stream().map(this::mapToLoanArrangementId).collect(Collectors.toSet()) : null;
    }

    public @Nullable SamatEmb mapSamat(Optional<Samat> value) {
        return value.map(this::toSamatEmb).orElse(null);
    }

    @Mapping(target = "trackingNumber", source = "trackingNumber")
    public abstract SamatEmb toSamatEmb(Samat samat);

    // EconomicSectorCurrency set mappings
    public @Nullable Set<EconomicSectorCurrencyEmb> mapEconomicSectorCurrenciesToEmbs(
            Set<EconomicSectorCurrency> value) {
        return value != null
                ? value.stream().map(this::toEconomicSectorCurrencyEmb).collect(Collectors.toSet())
                : null;
    }

    public @Nullable Set<EconomicSectorCurrency> mapEmbsToEconomicSectorCurrencies(
            Set<EconomicSectorCurrencyEmb> value) {
        return value != null
                ? value.stream().map(this::toEconomicSectorCurrency).collect(Collectors.toSet())
                : null;
    }

    @Named("mapRelationTypeLoanTopicsEmbs")
    public Set<RelationTypeLoanTopicEmb> mapRelationTypeLoanTopicsEmbs(
            Multimap<RelationType<TradeRelationType>, LoanTopic> multimap) {

        Set<RelationTypeLoanTopicEmb> embs = new HashSet<>();

        if (multimap == null || multimap.isEmpty()) {
            return embs;
        }

        for (var entry : multimap.entries()) {
            TradeRelationType relationType = (TradeRelationType) entry.getKey();
            LoanTopic loanTopic = entry.getValue();
            RelationTypeLoanTopicEmb emb = new RelationTypeLoanTopicEmb();
            emb.setTradeRelationType(relationType);
            emb.setTopicName(loanTopic.name());
            emb.setTopicCode(loanTopic.code());
            emb.setEconomicSectors(toEconomicSectorEmb(loanTopic.economicSectors()));
            embs.add(emb);
        }

        return embs;
    }

    @Named("mapEmbsRelationTypeLoanTopics")
    public ImmutableSetMultimap<RelationType<TradeRelationType>, LoanTopic> mapEmbsRelationTypeLoanTopics(
            Set<RelationTypeLoanTopicEmb> embs) {

        if (embs == null || embs.isEmpty()) {
            return ImmutableSetMultimap.of();
        }

        ImmutableSetMultimap.Builder<RelationType<TradeRelationType>, LoanTopic> builder =
                ImmutableSetMultimap.builder();

        for (var emb : embs) {
            LoanTopic loanTopic = LoanTopic.of(
                            Objects.requireNonNull(emb.getTopicName(), "topicName"),
                            Objects.requireNonNull(emb.getTopicCode(), "topicCode"),
                            Objects.requireNonNull(emb.getTradeRelationType(), "tradeRelationType"),
                            toEconomicSector(Objects.requireNonNull(emb.getEconomicSectors(), "economicSectors")))
                    .unwrap();

            builder.put(Objects.requireNonNull(emb.getTradeRelationType(), "tradeRelationType"), loanTopic);
        }

        return builder.build();
    }

    @Named("toAccountInfoMapEmb")
    public Map<String, String> toAccountInfoMapEmb(Map<RelationType<?>, AccountId> domainMap) {
        if (domainMap == null || domainMap.isEmpty()) return new HashMap<>();
        Map<String, String> result = new HashMap<>();
        domainMap.forEach((rt, aid) -> result.put(rt.toString(), aid.value()));
        return result;
    }

    @Named("fromAccountInfoMapEmb")
    public Map<RelationType<?>, AccountId> fromAccountInfoMapEmb(Map<String, String> entityMap) {
        if (entityMap == null || entityMap.isEmpty()) return new HashMap<>();
        Map<RelationType<?>, AccountId> result = new HashMap<>();
        entityMap.forEach((rtStr, aidStr) -> {
            TradeRelationType trt = TradeRelationType.valueOf(rtStr);
            result.put(trt, AccountId.valueOf(aidStr).unwrap());
        });
        return result;
    }

    public Set<String> toEconomicSectorEmb(Set<EconomicSector> economicSectors) {
        return economicSectors.stream()
                .filter(Objects::nonNull)
                .map(EconomicSector::code)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<EconomicSector> toEconomicSector(Set<String> economicSectors) {
        return economicSectors.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(code -> EconomicSector.of(code).unwrap())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public @Nullable List<AttributeEmb> mapAttributesToEmbs(List<Attribute> value) {
        return value != null ? value.stream().map(this::toAttributeEmb).collect(Collectors.toList()) : null;
    }

    public @Nullable List<Attribute> mapEmbsToAttributes(List<AttributeEmb> value) {
        return value != null ? value.stream().map(this::toAttribute).collect(Collectors.toList()) : null;
    }

    public @Nullable UUID map(SanctionedLoanId value) {
        return value != null ? value.value() : null;
    }

    public @Nullable SanctionedLoanId mapToSanctionedLoanId(UUID value) {
        return value != null ? SanctionedLoanId.of(value) : null;
    }

    public LifeInsuranceId stringToLifeInsuranceId(String value) {
        return new LifeInsuranceId(value);
    }

    public @Nullable String map(Optional<LifeInsuranceId> value) {
        return value.map(LifeInsuranceId::value).orElse(null);
    }

    public @Nullable UUID mapInstallmentScheduleId(Optional<InstallmentScheduleId> value) {
        return value.map(InstallmentScheduleId::value).orElse(null);
    }

    public UUID map(InstallmentScheduleId installmentScheduleId) {
        return installmentScheduleId.value();
    }

    public InstallmentScheduleId toInstallmentScheduleId(UUID value) {
        return new InstallmentScheduleId(value);
    }

    public UUID map(InstallmentId installmentId) {
        return installmentId.value();
    }

    public InstallmentId toInstallmentId(UUID installmentId) {
        return new InstallmentId(installmentId);
    }

    public UUID map(DisbursementScheduleId disbursementScheduleId) {
        return disbursementScheduleId.value();
    }

    public DisbursementScheduleId toDisbursementScheduleId(UUID disbursementScheduleId) {
        return new DisbursementScheduleId(disbursementScheduleId);
    }

    @Named("loanArrangementCodeToString")
    public String loanArrangementCodeToString(LoanArrangementCode loanArrangementCode) {
        return loanArrangementCode.value();
    }

    @Named("stringToLoanArrangementCode")
    public LoanArrangementCode stringToLoanArrangementCode(String loanArrangementCode) {
        return new LoanArrangementCode(loanArrangementCode);
    }

    @Mapping(source = "collateralType", target = "collateralType")
    public abstract CollateralEmb toTradeCollateralEmb(Collateral collateral);

    @Mapping(source = "collateralType", target = "collateralType")
    public abstract Collateral toCollateral(CollateralEmb embeddable);

    public List<CollateralEmb> toTradeCollateralEmbList(List<Collateral> collaterals) {
        if (collaterals == null) {
            return new ArrayList<>();
        }
        return collaterals.stream().map(this::toTradeCollateralEmb).collect(Collectors.toList());
    }

    public List<Collateral> toCollateralList(List<CollateralEmb> embeddables) {
        if (embeddables == null) {
            return new ArrayList<>();
        }
        return embeddables.stream().map(this::toCollateral).collect(Collectors.toList());
    }

    public @Nullable CancellationDataEmb mapCancellationData(Optional<CancellationData> value) {
        return value.map(this::toCancellationDataEmb).orElse(null);
    }

    @Named("toCancellationDataEmb")
    public abstract CancellationDataEmb toCancellationDataEmb(CancellationData cancellationData);

    @Named("toCloseFacilityPaidOffInfoEmb")
    public @Nullable CloseFacilityPaidOffInfoEmb toCloseFacilityPaidOffInfoEmb(CloseFacilityPaidOffInfo info) {
        if (info == null) {
            return null;
        }
        MoneyEmb moneyEmb = null;
        if (info.totalClosePaidOffAmount() != null) {
            moneyEmb = toMoneyEmb(info.totalClosePaidOffAmount());
        }
        return CloseFacilityPaidOffInfoEmb.builder()
                .closePaidOffDate(info.closePaidOffDate())
                .totalClosePaidOffAmount(moneyEmb)
                .build();
    }

    @Named("fromCloseFacilityPaidOffInfoEmb")
    public @Nullable CloseFacilityPaidOffInfo fromCloseFacilityPaidOffInfoEmb(CloseFacilityPaidOffInfoEmb emb) {
        if (emb == null || emb.getClosePaidOffDate() == null) {
            return null;
        }
        Money totalAmount = null;
        if (emb.getTotalClosePaidOffAmount() != null
                && emb.getTotalClosePaidOffAmount().getAmount() != null
                && emb.getTotalClosePaidOffAmount().getCurrency() != null) {
            CurrencyType currency = CurrencyType.valueOf(
                            emb.getTotalClosePaidOffAmount().getCurrency())
                    .unwrapOrThrow(c -> new IllegalStateException("Unknown currency in CloseFacilityPaidOffInfoEmb: "
                            + emb.getTotalClosePaidOffAmount().getCurrency()));
            totalAmount = Money.valueOf(emb.getTotalClosePaidOffAmount().getAmount(), currency)
                    .unwrapOrThrow(c ->
                            new IllegalStateException("Failed to reconstruct Money from CloseFacilityPaidOffInfoEmb"));
        }
        return new CloseFacilityPaidOffInfo(emb.getClosePaidOffDate(), totalAmount);
    }
}
