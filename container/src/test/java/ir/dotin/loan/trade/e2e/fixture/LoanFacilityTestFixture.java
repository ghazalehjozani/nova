package ir.dotin.loan.trade.e2e.fixture;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.test.context.TestComponent;

import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.PeriodEmb;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleStatus;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentScheduleType;
import ir.dotin.loan.baseloan.core.domain.installmentschedule.enums.InstallmentStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationNumberEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationPartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.BranchEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisburseDestinationEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentAmountEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentCountEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.LoanTypeCodeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.entity.InstallmentScheduleEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.installmentschedule.repository.InstallmentScheduleJpaRepository;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanApplicationEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;

import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class LoanFacilityTestFixture {

    private final TradeLoanFacilityJpaRepository facilityRepository;
    private final InstallmentScheduleJpaRepository scheduleRepository;

    public record DisbursedFacilityResult(String applicationNumber, UUID facilityId, UUID installmentScheduleId) {}

    public DisbursedFacilityResult createDisbursedFacilityForCollection(
            UUID loanTypeId, String loanTypeCode, UUID loanArrangementId) {

        UUID facilityId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        String branchCode = "1";
        String customerNumber = "12345678";
        String derivedValue = UUID.randomUUID().toString().substring(0, 8);
        String applicationNumber = branchCode + "-" + loanTypeCode + "-" + customerNumber + "-" + derivedValue;

        TradeLoanApplicationEntity application = createApplication(
                applicationId, loanTypeCode, applicationNumber, derivedValue, branchCode, customerNumber);

        TradeLoanFacilityEntity facility =
                createFacility(facilityId, application, loanTypeId, loanArrangementId, scheduleId);

        facilityRepository.save(facility);

        InstallmentScheduleEntity schedule = createScheduleWithInstallments(scheduleId, facilityId, 3);

        scheduleRepository.save(schedule);

        return new DisbursedFacilityResult(applicationNumber, facilityId, scheduleId);
    }

    private TradeLoanApplicationEntity createApplication(
            UUID applicationId,
            String loanTypeCode,
            String applicationNumber,
            String derivedValue,
            String branchCode,
            String customerNumber) {

        TradeLoanApplicationEntity app = new TradeLoanApplicationEntity();
        app.setId(applicationId);
        app.setRequestDate(Instant.now());
        app.setApplicantChannel(ApplicantChannel.INTERNET_BANK);
        app.setDisbursementMethod(DisbursementMethod.LUMP_SUM);

        MoneyEmb requestedAmount = new MoneyEmb();
        requestedAmount.setAmount(new BigDecimal("100000000"));
        requestedAmount.setCurrency("IRR");
        app.setRequestedAmount(requestedAmount);

        CurrencyTypeEmb currency = new CurrencyTypeEmb();
        currency.setValue("IRR");
        app.setCurrency(currency);

        PeriodEmb duration = new PeriodEmb();
        duration.setYears(0);
        duration.setMonths(12);
        duration.setDays(0);
        app.setRequestedLoanDuration(duration);

        GracePeriodEmb gracePeriod = new GracePeriodEmb();
        gracePeriod.setDays(30);
        app.setGracePeriod(gracePeriod);

        InstallmentCountEmb installmentCount = new InstallmentCountEmb();
        installmentCount.setValue(3);
        app.setInstallmentCount(installmentCount);

        DisburseDestinationEmb destination = new DisburseDestinationEmb();
        destination.setAccountNumber("123-456-789");
        destination.setType(DisburseDestinationType.ACCOUNT);
        app.setDisburseDestination(destination);

        EconomicSectorEmb economicSector = new EconomicSectorEmb();
        economicSector.setCode("EXCHANGE");
        app.setEconomicSector(economicSector);

        BranchEmb branch = new BranchEmb();
        branch.setCode(branchCode);
        app.setBranch(branch);

        PartyEmb applicantParty = new PartyEmb();
        applicantParty.setCustomerNumber(customerNumber);
        applicantParty.setPartyType(PartyType.REAL);
        applicantParty.setPartyRole(PartyRole.PRIMARY_APPLICANT);
        applicantParty.setFirstName("Test");
        applicantParty.setLastName("User");
        Set<PartyEmb> parties = new HashSet<>();
        parties.add(applicantParty);
        app.setParties(parties);

        ApplicationNumberEmb appNumber = new ApplicationNumberEmb();
        BranchEmb appBranch = new BranchEmb();
        appBranch.setCode(branchCode);
        appNumber.setBranch(appBranch);

        LoanTypeCodeEmb appLoanTypeCode = new LoanTypeCodeEmb();
        appLoanTypeCode.setValue(loanTypeCode);
        appNumber.setLoanTypeCode(appLoanTypeCode);

        ApplicationPartyEmb appParty = new ApplicationPartyEmb();
        appParty.setCustomerNumber(customerNumber);
        appParty.setPartyType(PartyType.REAL);
        appParty.setPartyRole(PartyRole.PRIMARY_APPLICANT);
        appParty.setFirstName("Test");
        appParty.setLastName("User");
        appNumber.setParty(appParty);
        appNumber.setDerivedValue(derivedValue);
        app.setApplicationNumber(appNumber);

        return app;
    }

    private TradeLoanFacilityEntity createFacility(
            UUID facilityId,
            TradeLoanApplicationEntity application,
            UUID loanTypeId,
            UUID loanArrangementId,
            UUID scheduleId) {

        TradeLoanFacilityEntity facility = new TradeLoanFacilityEntity();
        facility.setId(facilityId);
        facility.setLoanApplication(application);
        facility.setLoanTypeId(loanTypeId);
        facility.setLoanArrangementId(loanArrangementId);
        facility.setInstallmentScheduleId(scheduleId);
        facility.setCurrentState(FacilityStatus.FULLY_DISBURSED);
        facility.setDisbursementDate(LocalDate.now().minusDays(30));
        facility.setFacilityType("TRADE");

        MoneyEmb disbursedAmount = new MoneyEmb();
        disbursedAmount.setAmount(new BigDecimal("100000000"));
        disbursedAmount.setCurrency("IRR");
        facility.setTotalDisbursedAmount(disbursedAmount);

        facility.setIssueContractTransactionNumbers(List.of());
        facility.setDisbursementTransactionNumbers(List.of());
        facility.setCollaterals(List.of());

        return facility;
    }

    private InstallmentScheduleEntity createScheduleWithInstallments(
            UUID scheduleId, UUID facilityId, int installmentCount) {

        InstallmentScheduleEntity schedule = new InstallmentScheduleEntity();
        schedule.setId(scheduleId);
        schedule.setLoanFacilityId(facilityId);
        schedule.setScheduleType(InstallmentScheduleType.EQUAL_INSTALLMENTS);
        schedule.setStatus(InstallmentScheduleStatus.ACTIVE);
        schedule.setInitiatedAt(Instant.now());
        schedule.setLastModifiedAt(Instant.now());
        schedule.setInterestRate(new BigDecimal("18.000000"));

        GracePeriodEmb scheduleGracePeriod = new GracePeriodEmb();
        scheduleGracePeriod.setDays(30);
        schedule.setGracePeriod(scheduleGracePeriod);

        MoneyEmb totalAmount = new MoneyEmb();
        totalAmount.setAmount(new BigDecimal("100000000"));
        totalAmount.setCurrency("IRR");
        schedule.setTotalLoanAmount(totalAmount);

        CurrencyTypeEmb currency = new CurrencyTypeEmb();
        currency.setValue("IRR");
        schedule.setCurrency(currency);

        BigDecimal principalPerInstallment = new BigDecimal("100000000")
                .divide(BigDecimal.valueOf(installmentCount), 4, java.math.RoundingMode.HALF_UP);
        BigDecimal interestPerInstallment = new BigDecimal("5000000");
        BigDecimal totalPerInstallment = principalPerInstallment.add(interestPerInstallment);

        List<InstallmentEntity> installments = new ArrayList<>();
        for (int i = 1; i <= installmentCount; i++) {
            InstallmentEntity installment = new InstallmentEntity();
            installment.setId(UUID.randomUUID());
            installment.setSequenceNumber(i);
            installment.setDueDate(LocalDate.now().plusMonths(i));
            installment.setStatus(InstallmentStatus.SCHEDULED);
            installment.setInstallmentSchedule(schedule);

            InstallmentAmountEmb scheduledAmount = new InstallmentAmountEmb();

            MoneyEmb principal = new MoneyEmb();
            principal.setAmount(principalPerInstallment);
            principal.setCurrency("IRR");
            scheduledAmount.setPrincipalAmount(principal);

            MoneyEmb interest = new MoneyEmb();
            interest.setAmount(interestPerInstallment);
            interest.setCurrency("IRR");
            scheduledAmount.setInterestAmount(interest);

            MoneyEmb total = new MoneyEmb();
            total.setAmount(totalPerInstallment);
            total.setCurrency("IRR");
            scheduledAmount.setTotalAmount(total);

            installment.setScheduledAmount(scheduledAmount);

            MoneyEmb outstanding = new MoneyEmb();
            outstanding.setAmount(totalPerInstallment);
            outstanding.setCurrency("IRR");
            installment.setOutstandingAmount(outstanding);

            MoneyEmb paidAmount = new MoneyEmb();
            paidAmount.setAmount(BigDecimal.ZERO);
            paidAmount.setCurrency("IRR");
            installment.setPaidAmount(paidAmount);

            installments.add(installment);
        }

        schedule.setInstallments(installments);
        return schedule;
    }
}
