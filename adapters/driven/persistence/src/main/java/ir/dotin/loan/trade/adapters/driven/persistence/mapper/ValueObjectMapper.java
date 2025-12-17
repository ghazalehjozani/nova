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
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import ir.dotin.platform.adapter.persistence.embeddable.AmountRangeEmb;
import ir.dotin.platform.adapter.persistence.embeddable.DurationRangeEmb;
import ir.dotin.platform.adapter.persistence.embeddable.FormulaEmb;
import ir.dotin.platform.adapter.persistence.embeddable.InterestRateEmb;
import ir.dotin.platform.adapter.persistence.embeddable.MoneyEmb;
import ir.dotin.platform.adapter.persistence.embeddable.PeriodEmb;
import ir.dotin.platform.commons.domain.vo.CurrencyType;
import ir.dotin.platform.commons.domain.vo.Formula;
import ir.dotin.platform.commons.domain.vo.Money;
import ir.dotin.platform.commons.domain.vo.Rate;
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
import ir.dotin.loan.baseloan.core.domain.shared.enums.RelationType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.AccountNumber;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Active;
import ir.dotin.loan.baseloan.core.domain.shared.vo.Attribute;
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
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityParameterizedFormula;
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
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.AccountId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.document.DepositNumber;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationNumberEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationPartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.AttributeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.BranchEmb;
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
import ir.dotin.loan.trade.adapters.driven.persistence.service.FormulaFieldMappingService;
import ir.dotin.loan.trade.core.domain.loantype.enums.TradeRelationType;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanFacilityFormulaField;
import ir.dotin.loan.trade.core.domain.shared.formula.TradeLoanParameterProvider;

@SuppressWarnings("unused")
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class ValueObjectMapper {

    @Autowired
    public FormulaFieldMappingService formulaFieldMappingService;

    @Mapping(source = "value", target = "amount")
    @Mapping(source = "currency.code", target = "currency")
    public abstract MoneyEmb toMoneyEmb(Money money);

    @Mapping(source = "amount", target = "value")
    @Mapping(source = "currency", target = "currency", qualifiedByName = "stringToCurrency")
    public abstract Money toMoney(MoneyEmb embeddable);

    @Mapping(source = "value", target = "formula")
    public abstract FormulaEmb toFormulaEmb(Formula formula);

    @Mapping(source = "formula", target = "value")
    public abstract Formula toFormula(FormulaEmb embeddable);

    @Mapping(source = "value", target = "value")
    public abstract InterestRateEmb toInterestRateEmb(Rate rate);

    @Mapping(source = "value", target = "value")
    public abstract Rate toRate(InterestRateEmb embeddable);

    public abstract ApplicationNumberEmb toApplicationNumberEmb(ApplicationNumber applicationNumber);

    public abstract ApplicationNumber toApplicationNumber(ApplicationNumberEmb applicationNumberEmb);

    public abstract RespiteSerialEmb toRespiteSerialEmb(RespiteSerial respiteSerial);

    public RespiteSerialEmb toRespiteSerialEmb(Optional<RespiteSerial> respiteSerialOpt) {
        return respiteSerialOpt.map(this::toRespiteSerialEmb).orElse(null);
    }

    public abstract RespiteSerial toRespiteSerial(RespiteSerialEmb respiteSerialEmb);

    public Optional<RespiteSerial> mapToOptionalRespiteSerial(RespiteSerialEmb value) {
        return Optional.of(toRespiteSerial(value));
    }

    public ApplicationNumberEmb toApplicationNumberEmb(Optional<ApplicationNumber> applicationNumberOpt) {
        return applicationNumberOpt.map(this::toApplicationNumberEmb).orElse(null);
    }

    @Mapping(source = "code.value", target = "code")
    public abstract BranchEmb toBranchEmb(Branch branch);

    @Mapping(source = "code", target = "code.value")
    public abstract Branch toBranch(BranchEmb embeddable);

    public abstract CollateralSerialEmb toCollateralSerialEmb(CollateralSerial collateralSerial);

    public CollateralSerialEmb toCollateralSerialEmb(Optional<CollateralSerial> collateralSerial) {
        return collateralSerial.map(this::toCollateralSerialEmb).orElse(null);
    }

    @Named("toScheduleHistoryEmb")
    public ScheduleHistoryEmb toScheduleHistoryEmb(ScheduleHistory scheduleHistory) {
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
    public DisbursementHistoryEmb toDisbursementHistoryEmb(DisbursementHistory history) {
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

    public DisbursementRecordEmb toDisbursementRecordEmb(DisbursementRecord record) {
        if (record == null) return null;
        DisbursementRecordEmb emb = new DisbursementRecordEmb();
        emb.setAmount(toMoneyEmb(record.amount()));
        emb.setDisbursedAt(record.disbursedAt());
        emb.setDisbursedBy(record.disbursedBy());
        return emb;
    }

    public DisbursementRecord toDisbursementRecord(DisbursementRecordEmb emb) {
        if (emb == null) return null;
        return new DisbursementRecord(toMoney(emb.getAmount()), emb.getDisbursedAt(), emb.getDisbursedBy());
    }

    @Named("toRestructuringRecordEmb")
    public RestructuringRecordEmb toRestructuringRecordEmb(RestructuringRecord record) {
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
    public RestructuringRecord toRestructuringRecord(RestructuringRecordEmb emb) {
        if (emb == null) return null;

        Money addedPrincipal = Money.valueOf(
                        emb.getRestructuringAmount(),
                        CurrencyType.valueOf(emb.getRestructuringAmountCurrency())
                                .orElseThrow())
                .orElseThrow();

        return new RestructuringRecord(
                emb.getReason(),
                addedPrincipal,
                emb.getPreviousInstallmentCount(),
                emb.getNewInstallmentCount(),
                emb.getUnpaidInstallmentsCount(),
                emb.getPreservedInstallmentsCount(),
                emb.getRestructuredAt(),
                emb.getRestructuredBy());
    }

    public abstract CollateralSerial toCollateralSerial(CollateralSerialEmb embeddable);

    public abstract CredibilityRankEmb toCredibilityRankEmb(CredibilityRank credibilityRank);

    public CredibilityRankEmb toCredibilityRankEmb(Optional<CredibilityRank> credibilityRankOpt) {
        return credibilityRankOpt.map(this::toCredibilityRankEmb).orElse(null);
    }

    public abstract CredibilityRank toCredibilityRank(CredibilityRankEmb embeddable);

    public abstract DescriptionEmb toDescriptionEmb(Description description);

    public DescriptionEmb toDescriptionEmb(Optional<Description> descriptionOpt) {
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
        return switch (emb.getType()) {
            case DEPOSIT ->
                DepositDisburseDestination.of(
                                DepositNumber.valueOf(emb.getDepositNumber()).orElseThrow())
                        .orElseThrow();
            case ACCOUNT ->
                AccountDisburseDestination.of(
                                AccountNumber.of(emb.getAccountNumber()).orElseThrow())
                        .orElseThrow();
        };
    }

    @Named("optionalDepositNumberToString")
    public String optionalDepositNumberToString(Optional<DepositNumber> depositNumber) {
        return depositNumber.map(DepositNumber::value).orElse(null);
    }

    @Named("stringToOptionalDepositNumber")
    public Optional<DepositNumber> stringToOptionalDepositNumber(String depositNumber) {
        if (depositNumber == null || depositNumber.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new DepositNumber(depositNumber));
    }

    public GracePeriodEmb toGracePeriodEmb(GracePeriod gracePeriod) {
        if (gracePeriod == null) {
            return null;
        }
        return periodToEmb(gracePeriod.value());
    }

    public GracePeriod toGracePeriod(GracePeriodEmb embeddable) {
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

    public SubSourceEmb toSubSourceEmb(Optional<SubSource> subSourceOpt) {
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

    public Party toParty(PartyEmb emb) {
        CustomerName name = new CustomerName(emb.getFirstName(), emb.getLastName(), emb.getCompanyName());

        return switch (emb.getPartyRole()) {
            case PRIMARY_APPLICANT -> new ApplicantParty(emb.getCustomerNumber(), emb.getPartyType(), name);
            case CO_APPLICANT -> new CoApplicantParty(emb.getCustomerNumber(), emb.getPartyType(), name);
            case GUARANTOR ->
                GuarantorParty.of(
                                emb.getCustomerNumber(),
                                emb.getPartyType(),
                                name,
                                emb.getGuaranteePercentage() != null
                                        ? GuaranteePercentage.of(emb.getGuaranteePercentage())
                                        : null)
                        .orElseThrow();
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

    public Party toParty(ApplicationPartyEmb emb) {
        CustomerName name = new CustomerName(emb.getFirstName(), emb.getLastName(), emb.getCompanyName());

        return switch (emb.getPartyRole()) {
            case PRIMARY_APPLICANT -> new ApplicantParty(emb.getCustomerNumber(), emb.getPartyType(), name);
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
    public AmountRangeEmb mapAmountRangeToEmb(Range<Money> amountRange) {
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
    public InterestPolicyEmb mapInterestPolicyToEmb(
            InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy) {
        if (policy == null) return null;
        InterestPolicyEmb emb = new InterestPolicyEmb();
        emb.setBaseInterestRate(policy.baseInterestRate().value());
        emb.setPreferentialMinRate(
                policy.preferentialRangeRate().lowerEndpoint().value());
        emb.setPreferentialMaxRate(
                policy.preferentialRangeRate().upperEndpoint().value());
        emb.setInterestFormula(mapParameterizedFormulaToString(policy.interestFormula()));
        emb.setRefundInterestFormula(mapParameterizedFormulaToString(policy.refundInterestFormula()));
        emb.setDailyInterest(policy.dailyInterest());
        return emb;
    }

    @Named("mapInstallmentPolicyToEmb")
    public InstallmentPolicyEmb mapInstallmentPolicyToEmb(
            InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy) {
        if (policy == null) return null;
        InstallmentPolicyEmb emb = new InstallmentPolicyEmb();
        emb.setInstallmentPeriodDays(policy.installmentPeriod().value().getDays());
        emb.setInstallmentFormula(mapParameterizedFormulaToString(policy.installmentFormula()));
        emb.setInterestComponentFormula(mapParameterizedFormulaToString(policy.interestComponentFormula()));
        emb.setInstallmentPaymentType(policy.installmentPaymentType().name());
        return emb;
    }

    @Named("mapGracePeriodPolicyToEmb")
    public GracePeriodPolicyEmb mapGracePeriodPolicyToEmb(
            GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy) {
        if (policy == null) return null;
        GracePeriodPolicyEmb emb = new GracePeriodPolicyEmb();
        emb.setMinGracePeriodDays(policy.minGracePeriod().getDays());
        emb.setMaxGracePeriodDays(policy.maxGracePeriod().getDays());
        emb.setGracePeriodFormula(mapParameterizedFormulaToString(policy.gracePeriodFormula()));
        return emb;
    }

    @Named("mapPenaltyPolicyToEmb")
    public PenaltyPolicyEmb mapPenaltyPolicyToEmb(
            PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> policy) {
        if (policy == null) return null;
        PenaltyPolicyEmb emb = new PenaltyPolicyEmb();
        emb.setPenaltyRate(policy.penaltyRate().value());
        emb.setDeferralInterestRate(policy.deferralInterestRate().value());
        emb.setPenaltyFormula(mapParameterizedFormulaToString(policy.penaltyFormula()));
        emb.setPenaltyPaymentType(policy.penaltyPaymentType().name());
        return emb;
    }

    @Named("mapCollateralPolicyToEmb")
    public CollateralPolicyEmb mapCollateralPolicyToEmb(CollateralPolicy policy) {
        if (policy == null) return null;
        CollateralPolicyEmb emb = new CollateralPolicyEmb();
        emb.setTotalPercent(policy.totalPercent());
        emb.setCollateralTypes(new HashSet<>(policy.collateralTypes()));
        emb.setCollateralCalculationType(policy.collateralCalculationType());
        return emb;
    }

    @Named("mapAmountRangeEmbToRange")
    public Range<Money> mapAmountRangeEmbToRange(AmountRangeEmb emb) {
        if (emb == null || emb.getMinAmount() == null || emb.getMaxAmount() == null || emb.getCurrency() == null) {
            return null;
        }
        CurrencyType currency = CurrencyType.valueOf(emb.getCurrency()).orElseThrow();
        Money min = Money.valueOf(emb.getMinAmount(), currency).orElseThrow();
        Money max = Money.valueOf(emb.getMaxAmount(), currency).orElseThrow();
        return Range.closed(min, max);
    }

    @Named("mapInterestPolicyEmbToPolicy")
    public InterestPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapInterestPolicyEmbToPolicy(
            InterestPolicyEmb emb) {
        if (emb == null) return null;
        Rate baseRate = Rate.valueOf(emb.getBaseInterestRate()).orElseThrow();
        Rate minRate = Rate.valueOf(emb.getPreferentialMinRate()).orElseThrow();
        Rate maxRate = Rate.valueOf(emb.getPreferentialMaxRate()).orElseThrow();
        Range<Rate> preferentialRange = Range.closed(minRate, maxRate);
        return InterestPolicy.of(
                        baseRate,
                        preferentialRange,
                        mapStringToParameterizedFormula(emb.getInterestFormula()),
                        mapStringToParameterizedFormula(emb.getRefundInterestFormula()),
                        emb.getDailyInterest())
                .orElseThrow();
    }

    @Named("mapInstallmentPolicyEmbToPolicy")
    public InstallmentPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapInstallmentPolicyEmbToPolicy(
            InstallmentPolicyEmb emb) {
        if (emb == null) return null;
        InstallmentPeriod installmentPeriod = InstallmentPeriod.of(Period.ofDays(emb.getInstallmentPeriodDays()))
                .orElseThrow();
        return InstallmentPolicy.of(
                        installmentPeriod,
                        mapStringToParameterizedFormula(emb.getInstallmentFormula()),
                        mapStringToParameterizedFormula(emb.getInterestComponentFormula()),
                        InstallmentPaymentType.valueOf(emb.getInstallmentPaymentType()))
                .orElseThrow();
    }

    @Named("mapGracePeriodPolicyEmbToPolicy")
    public GracePeriodPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapGracePeriodPolicyEmbToPolicy(
            GracePeriodPolicyEmb emb) {
        if (emb == null) return null;
        return GracePeriodPolicy.of(
                        Period.ofDays(emb.getMinGracePeriodDays()),
                        Period.ofDays(emb.getMaxGracePeriodDays()),
                        mapStringToParameterizedFormula(emb.getGracePeriodFormula()))
                .orElseThrow();
    }

    @Named("mapPenaltyPolicyEmbToPolicy")
    public PenaltyPolicy<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> mapPenaltyPolicyEmbToPolicy(
            PenaltyPolicyEmb emb) {
        if (emb == null) return null;
        Rate penaltyRate = Rate.valueOf(emb.getPenaltyRate()).orElseThrow();
        Rate deferralRate = Rate.valueOf(emb.getDeferralInterestRate()).orElseThrow();
        PenaltyPaymentType paymentType = PenaltyPaymentType.valueOf(emb.getPenaltyPaymentType());
        return PenaltyPolicy.of(
                        penaltyRate,
                        deferralRate,
                        mapStringToParameterizedFormula(emb.getPenaltyFormula()),
                        paymentType)
                .orElseThrow();
    }

    @Named("mapCollateralPolicyEmbToPolicy")
    public CollateralPolicy mapCollateralPolicyEmbToPolicy(CollateralPolicyEmb emb) {
        if (emb == null) return null;
        return CollateralPolicy.of(
                        new ArrayList<>(emb.getCollateralTypes()),
                        emb.getTotalPercent(),
                        emb.getCollateralCalculationType())
                .orElseThrow();
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
        return CurrencyType.valueOf(currencyCode).orElse(CurrencyType.IRR);
    }

    public CurrencyType currencyTypeEmbToCurrencyType(CurrencyTypeEmb currencyTypeEmb) {
        return CurrencyType.valueOf(currencyTypeEmb.getValue()).orElse(CurrencyType.IRR);
    }

    @Named("embToTrackedTransactionNumber")
    public TrackedTransactionNumber embToTrackedTransactionNumber(TransactionNumberEmb emb) {
        return new TrackedTransactionNumber(emb.getValue(), emb.getCreatedAt(), emb.getTrackingId(), emb.getStatus());
    }

    public String mapParameterizedFormulaToString(
            LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField> value) {
        return formulaFieldMappingService.serializeParameterizedFormula(value);
    }

    public LoanFacilityParameterizedFormula<TradeLoanParameterProvider, TradeLoanFacilityFormulaField>
            mapStringToParameterizedFormula(String value) {
        return formulaFieldMappingService.deserializeParameterizedFormula(value);
    }

    public UUID map(LoanArrangementId value) {
        return value != null ? value.value() : null;
    }

    public LoanArrangementId mapToLoanArrangementId(UUID value) {
        return value != null ? LoanArrangementId.of(value) : null;
    }

    public UUID map(LoanTypeId value) {
        return value != null ? value.value() : null;
    }

    public LoanTypeId mapToLoanTypeId(UUID value) {
        return value != null ? LoanTypeId.of(value) : null;
    }

    public UUID map(LoanApplicationId value) {
        return value != null ? value.value() : null;
    }

    public LoanApplicationId mapToLoanApplicationId(UUID value) {
        return value != null ? LoanApplicationId.of(value) : null;
    }

    public UUID map(LoanFacilityId value) {
        return value != null ? value.value() : null;
    }

    public LoanFacilityId mapToLoanFacilityId(UUID value) {
        return value != null ? LoanFacilityId.of(value) : null;
    }

    public Boolean map(Active value) {
        return value != null ? value.isActive() : null;
    }

    public Active mapToActive(Boolean value) {
        return value != null ? Active.of(value).orElseThrow() : null;
    }

    public Boolean map(Disable value) {
        return value != null ? value.isDisable() : null;
    }

    public Disable mapToDisable(Boolean value) {
        return value != null ? Disable.of(value).orElseThrow() : null;
    }

    public BigDecimal mapRateValue(Rate rate) {
        return rate != null ? rate.value() : null;
    }

    public Rate mapToRateFromBigDecimal(BigDecimal value) {
        return value != null ? Rate.valueOf(value).orElseThrow() : null;
    }

    public Long mapDurationToDays(Duration duration) {
        return duration != null ? duration.toDays() : null;
    }

    public Duration mapDaysToDuration(Long days) {
        return days != null ? Duration.ofDays(days) : null;
    }

    public Set<String> mapCurrencyTypesToStrings(Set<CurrencyType> currencies) {
        return currencies != null
                ? currencies.stream().map(CurrencyType::getCode).collect(Collectors.toSet())
                : null;
    }

    public Set<CurrencyType> mapStringsToCurrencies(Set<String> currencies) {
        return currencies != null
                ? currencies.stream()
                        .map(currencyCode -> CurrencyType.valueOf(currencyCode).orElseThrow())
                        .collect(Collectors.toSet())
                : null;
    }

    @Named("periodRangeEmbToLoanDuration")
    public Range<LoanDuration> mapPeriodRangeEmbToLoanDuration(PeriodRangeEmb emb) {
        if (emb == null) return null;

        LoanDuration minDuration = mapPeriodEmbToLoanDuration(emb.getMinPeriod());
        LoanDuration maxDuration = mapPeriodEmbToLoanDuration(emb.getMaxPeriod());

        return Range.closed(minDuration, maxDuration);
    }

    @Named("loanDurationToPeriodRangeEmb")
    public PeriodRangeEmb mapLoanDurationToPeriodRangeEmb(Range<LoanDuration> loanDuration) {
        if (loanDuration == null) return null;

        PeriodRangeEmb emb = new PeriodRangeEmb();
        emb.setMinPeriod(mapLoanDurationToPeriodEmb(loanDuration.lowerEndpoint()));
        emb.setMaxPeriod(mapLoanDurationToPeriodEmb(loanDuration.upperEndpoint()));

        return emb;
    }

    public LoanDuration mapPeriodEmbToLoanDuration(PeriodEmb emb) {
        if (emb == null) return null;
        Period period = Period.of(
                emb.getYears() != null ? emb.getYears() : 0,
                emb.getMonths() != null ? emb.getMonths() : 0,
                emb.getDays() != null ? emb.getDays() : 0);
        return LoanDuration.of(period).orElseThrow();
    }

    public PeriodEmb mapLoanDurationToPeriodEmb(LoanDuration loanDuration) {
        if (loanDuration == null) return null;
        PeriodEmb emb = new PeriodEmb();
        emb.setDays(loanDuration.value().getDays());
        emb.setMonths(loanDuration.value().getMonths());
        emb.setYears(loanDuration.value().getYears());
        return emb;
    }

    @Named("mapDurationRangeToEmb")
    public DurationRangeEmb mapDurationRangeToEmb(Range<Duration> durationRange) {
        if (durationRange == null || !durationRange.hasLowerBound() || !durationRange.hasUpperBound()) {
            return null;
        }
        DurationRangeEmb emb = new DurationRangeEmb();
        emb.setMinDurationDays(durationRange.lowerEndpoint().toDays());
        emb.setMaxDurationDays(durationRange.upperEndpoint().toDays());
        return emb;
    }

    public Boolean map(LoanApplicationStatus value) {
        return value != null ? value.isAllowed() : null;
    }

    public LoanApplicationStatus mapToLoanApplicationStatus(Boolean value) {
        return value != null ? (value ? LoanApplicationStatus.ALLOWED : LoanApplicationStatus.NOT_ALLOWED) : null;
    }

    public UUID map(IncomeId value) {
        return value != null ? value.value() : null;
    }

    public IncomeId mapToIncomeId(UUID value) {
        return value != null ? IncomeId.of(value).orElseThrow() : null;
    }

    public UUID map(LoanTypeGroupId value) {
        return value != null ? value.value() : null;
    }

    public LoanTypeGroupId mapToLoanTypeGroupId(UUID value) {
        return value != null ? LoanTypeGroupId.of(value).orElseThrow() : null;
    }

    public Set<UUID> mapIncomeIdsToUUIDs(Set<IncomeId> value) {
        return value != null ? value.stream().map(this::map).collect(Collectors.toSet()) : null;
    }

    public Set<IncomeId> mapUUIDsToIncomeIds(Set<UUID> value) {
        return value != null ? value.stream().map(this::mapToIncomeId).collect(Collectors.toSet()) : null;
    }

    public Set<UUID> mapLoanArrangementIdsToUUIDs(Set<LoanArrangementId> value) {
        return value != null ? value.stream().map(this::map).collect(Collectors.toSet()) : null;
    }

    public Set<LoanArrangementId> mapUUIDsToLoanArrangementIds(Set<UUID> value) {
        return value != null ? value.stream().map(this::mapToLoanArrangementId).collect(Collectors.toSet()) : null;
    }

    public SamatEmb mapSamat(Optional<Samat> value) {
        return value.map(this::toSamatEmb).orElse(null);
    }

    @Mapping(target = "trackingNumber", source = "trackingNumber")
    public abstract SamatEmb toSamatEmb(Samat samat);

    // EconomicSectorCurrency set mappings
    public Set<EconomicSectorCurrencyEmb> mapEconomicSectorCurrenciesToEmbs(Set<EconomicSectorCurrency> value) {
        return value != null
                ? value.stream().map(this::toEconomicSectorCurrencyEmb).collect(Collectors.toSet())
                : null;
    }

    public Set<EconomicSectorCurrency> mapEmbsToEconomicSectorCurrencies(Set<EconomicSectorCurrencyEmb> value) {
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
                            emb.getTopicName(),
                            emb.getTopicCode(),
                            emb.getTradeRelationType(),
                            toEconomicSector(emb.getEconomicSectors()))
                    .orElseThrow();

            builder.put(emb.getTradeRelationType(), loanTopic);
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
            result.put(trt, AccountId.valueOf(aidStr).orElseThrow());
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
                .map(code -> EconomicSector.of(code).getValue())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<AttributeEmb> mapAttributesToEmbs(List<Attribute> value) {
        return value != null ? value.stream().map(this::toAttributeEmb).collect(Collectors.toList()) : null;
    }

    public List<Attribute> mapEmbsToAttributes(List<AttributeEmb> value) {
        return value != null ? value.stream().map(this::toAttribute).collect(Collectors.toList()) : null;
    }

    public UUID map(SanctionedLoanId value) {
        return value != null ? value.value() : null;
    }

    public SanctionedLoanId mapToSanctionedLoanId(UUID value) {
        return value != null ? SanctionedLoanId.of(value) : null;
    }

    public LifeInsuranceId stringToLifeInsuranceId(String value) {
        return new LifeInsuranceId(value);
    }

    public String map(Optional<LifeInsuranceId> value) {
        return value.map(LifeInsuranceId::value).orElse(null);
    }

    public UUID mapInstallmentScheduleId(Optional<InstallmentScheduleId> value) {
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
}
