package ir.dotin.loan.trade.persistence;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import ir.dotin.platform.accounting.document.api.enumeration.TransactionStatus;
import ir.dotin.platform.pangaea.commons.domain.vo.CurrencyType;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.MoneyEmb;
import ir.dotin.platform.pangaea.persistence.jpa.embeddable.PeriodEmb;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.ApplicantChannel;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisburseDestinationType;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.DisbursementMethod;
import ir.dotin.loan.baseloan.core.domain.loanfacility.enums.FacilityStatus;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.DisbursementRecord;
import ir.dotin.loan.baseloan.core.domain.loanfacility.vo.LoanDuration;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyRole;
import ir.dotin.loan.baseloan.core.domain.shared.enums.PartyType;
import ir.dotin.loan.baseloan.core.domain.shared.vo.LoanFacilityId;
import ir.dotin.loan.baseloan.core.domain.shared.vo.TrackedTransactionNumber;
import ir.dotin.loan.trade.adapters.driven.persistence.config.LoanPersistenceConfiguration;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationNumberEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.ApplicationPartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.BranchEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.CurrencyTypeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisburseDestinationEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisbursementHistoryEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.DisbursementRecordEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.EconomicSectorEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.GracePeriodEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.InstallmentCountEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.LoanTypeCodeEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.PartyEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.RequestReasonEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.embdeddable.TransactionNumberEmb;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanApplicationEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.entity.TradeLoanFacilityEntity;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper.TradeLoanFacilityPersistenceMapper;
import ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.repository.TradeLoanFacilityJpaRepository;
import ir.dotin.loan.trade.core.domain.loanfacility.entity.TradeLoanFacility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Focused persistence round-trip integration test for the base-loan 2026.6.8 schema refactor (changesets 013-016 under
 * {@code db/changelog/trade-loan/v2026.6.9/}).
 *
 * <p>It boots ONLY a real PostgreSQL (Testcontainers) + Liquibase (the real master changelog) + the JPA persistence
 * slice (entities, Spring Data repository, and the real {@link TradeLoanFacilityPersistenceMapper} /
 * {@code ValueObjectMapper}). It is a narrow persistence slice — NO Consul, Kafka, Redis, web layer, or the full
 * {@code NovaApplication} context. The full nova E2E is env-blocked by a pre-existing Consul {@code ${CONSUL_PORT}}
 * config gap; this test proves the entity↔mapper↔migration triad for exactly the parts THIS change touched.
 *
 * <p>The round-trip uses the repo's established entity-construction idiom (mirrors {@code LoanFacilityTestFixture}):
 * build a {@link TradeLoanFacilityEntity}, persist it through the migrated schema (Hibernate INSERT + Liquibase DDL),
 * detach + reload, then map back to the domain {@link TradeLoanFacility} via the real persistence mapper — the exact
 * read path {@code TradeLoanFacilityRepositoryAdapter#findById} takes — and assert the domain getters.
 *
 * <p>Named {@code *Test} (not {@code *IT}) on purpose: the container module has no failsafe binding for non-e2e tests,
 * so surefire is the runner; surefire excludes only {@code **}{@code /e2e/**}, and this test lives outside that
 * package.
 *
 * <p>Boots via a plain {@code @SpringBootTest(webEnvironment = NONE)} against a nested {@link PersistenceSliceConfig}
 * that hand-picks ONLY the datasource / Hibernate-JPA / transaction / Liquibase auto-configurations plus the real
 * persistence config and MapStruct mappers. Spring Boot 4.1 ships {@code @DataJpaTest} in a test-slice artifact this
 * repo does not depend on, so this explicit slice is the equivalent that stays within the project's existing deps.
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        // The main bootstrap.yml (on the test classpath) declares spring.profiles.active=${SPRING_PROFILES_ACTIVE} and
        // a Consul-config block full of ${CONSUL_*}/${HOST_IP} placeholders. Resolve the eager profile placeholder and
        // neutralise the Consul/bootstrap import so this slice never touches the env-blocked Consul wiring.
        properties = {
            "SPRING_PROFILES_ACTIVE=test",
            "spring.profiles.active=test",
            "spring.cloud.bootstrap.enabled=false",
            "spring.cloud.consul.enabled=false",
            "spring.cloud.consul.config.enabled=false",
            "spring.cloud.consul.discovery.enabled=false",
            "spring.cloud.kubernetes.enabled=false",
            "spring.config.import="
        })
@DisplayName("TradeLoanFacility persistence round-trip (migrated v2026.6.9 schema)")
class TradeLoanFacilityPersistenceRoundTripTest {

    /**
     * Narrow Spring slice: datasource + Hibernate JPA + transactions + Liquibase, the persistence config (entity scan +
     * Spring Data repositories), and the real MapStruct mappers. No Consul, Kafka, Redis, web, or
     * {@code NovaApplication}.
     */
    @SpringBootConfiguration
    @EnableJpaAuditing // populate @CreatedDate created_at on PersistentEntity (the app enables this via a pangaea
    // starter)
    @ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        LiquibaseAutoConfiguration.class
    })
    @Import(LoanPersistenceConfiguration.class)
    // Pull in the real MapStruct mapper beans (componentModel=SPRING) for entity<->domain mapping. The ASPECTJ
    // include-filter keeps the scan to the mapper interfaces + their generated impls.
    @ComponentScan(
            basePackages = {
                "ir.dotin.loan.trade.adapters.driven.persistence.mapper",
                "ir.dotin.loan.trade.adapters.driven.persistence.loanfacility.mapper"
            },
            useDefaultFilters = false,
            includeFilters =
                    @ComponentScan.Filter(
                            type = FilterType.ASPECTJ,
                            pattern = "ir.dotin.loan.trade.adapters.driven.persistence..*Mapper*"))
    static class PersistenceSliceConfig {}

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18.0-alpine")
            .withDatabaseName("trade_loan_it")
            .withUsername("it_user")
            .withPassword("it_password");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Let Liquibase own the schema; Hibernate must only validate against the migrated schema, never create it.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.liquibase.enabled", () -> "true");
        // Run the REAL trade-loan domain changelog (the production changesets 001-016, including this change's
        // v2026.6.9 013-016). We point at the trade-loan changelog rather than db.changelog-master.xml on purpose:
        // the master also pulls the framework changelog, whose framework/v2026.3.0/006-delete-suspended-sagas.xml
        // does an unconditional `DELETE FROM saga_compensation` that fails on a *fresh* DB (the saga tables were
        // never created by this repo's changelog — they came from a now-removed pangaea jar; the later
        // v2026.6.4/005-drop-saga-tables.xml is precondition-guarded, but 006 is not). That is a pre-existing
        // framework-changelog fresh-DB defect, orthogonal to this change; the trade-loan changelog owns every table
        // the entities under test touch, so this slice exercises exactly the migrated v2026.6.9 schema.
        registry.add(
                "spring.liquibase.change-log", () -> "classpath:db/changelog/trade-loan/db.changelog-trade-loan.xml");
        // Belt-and-braces: keep the Spring Cloud bootstrap/Consul/Kubernetes machinery out of this slice so the
        // pre-existing ${CONSUL_PORT} gap cannot block the test.
        registry.add("spring.cloud.consul.enabled", () -> "false");
        registry.add("spring.cloud.consul.config.enabled", () -> "false");
        registry.add("spring.cloud.consul.discovery.enabled", () -> "false");
        registry.add("spring.cloud.kubernetes.enabled", () -> "false");
        registry.add("spring.cloud.bootstrap.enabled", () -> "false");
    }

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-17T08:00:00Z"), ZoneOffset.UTC);

    @Autowired
    private TradeLoanFacilityJpaRepository facilityRepository;

    @Autowired
    private TradeLoanFacilityPersistenceMapper mapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("CS-3: disbursement records keyed on loan_facility_id round-trip via the facility's history")
    void disbursementHistoryRoundTrips() {
        UUID facilityId = UUID.randomUUID();
        TradeLoanFacilityEntity entity = newFacilityEntity(facilityId);

        DisbursementRecordEmb record = new DisbursementRecordEmb();
        MoneyEmb amount = new MoneyEmb();
        amount.setAmount(new BigDecimal("40000000.0000"));
        amount.setCurrency("IRR");
        record.setAmount(amount);
        record.setDisbursedAt(LocalDate.of(2026, 5, 1));
        record.setDisbursedBy("operator-7");

        DisbursementHistoryEmb history = new DisbursementHistoryEmb();
        history.setRecords(List.of(record));
        entity.setDisbursementHistory(history);

        TradeLoanFacility reloaded = persistDetachReloadAndMap(entity, facilityId);

        assertThat(reloaded.getDisbursementHistory().getRecords()).hasSize(1);
        DisbursementRecord roundTripped =
                reloaded.getDisbursementHistory().getRecords().getFirst();
        // Compare numerically (the DECIMAL(19,4) column round-trips at scale 4; Money.valueOf normalises IRR to
        // scale 2 — same value, different scale, so use isEqualByComparingTo not equals).
        assertThat(roundTripped.amount().value()).isEqualByComparingTo(new BigDecimal("40000000"));
        assertThat(roundTripped.amount().currency()).isEqualTo(CurrencyType.IRR);
        assertThat(roundTripped.disbursedAt()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(roundTripped.disbursedBy()).isEqualTo("operator-7");
    }

    @Test
    @DisplayName("CS-4: restructuring TrackedTransactionNumber round-trips with value/status/trackingId/createdAt")
    void restructuringTransactionNumbersRoundTrip() {
        UUID facilityId = UUID.randomUUID();
        TradeLoanFacilityEntity entity = newFacilityEntity(facilityId);

        Instant createdAt = Instant.parse("2026-04-15T12:30:00Z");
        TransactionNumberEmb txn = new TransactionNumberEmb();
        txn.setValue("RST-900123");
        txn.setStatus(TransactionStatus.POSTED);
        txn.setTrackingId("TRK-55");
        txn.setCreatedAt(createdAt);
        entity.setRestructuringTransactionNumbers(List.of(txn));

        TradeLoanFacility reloaded = persistDetachReloadAndMap(entity, facilityId);

        List<TrackedTransactionNumber> restructuring = reloaded.getRestructuringTransactionNumbers();
        assertThat(restructuring).hasSize(1);
        TrackedTransactionNumber roundTripped = restructuring.getFirst();
        assertThat(roundTripped.value()).isEqualTo("RST-900123");
        assertThat(roundTripped.status()).isEqualTo(TransactionStatus.POSTED);
        assertThat(roundTripped.trackingId()).isEqualTo("TRK-55");
        assertThat(roundTripped.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("CS-2: effectiveLoanDuration round-trips when present")
    void effectiveLoanDurationRoundTripsWhenPresent() {
        UUID facilityId = UUID.randomUUID();
        TradeLoanFacilityEntity entity = newFacilityEntity(facilityId);

        PeriodEmb effective = new PeriodEmb();
        effective.setYears(1);
        effective.setMonths(6);
        effective.setDays(0);
        entity.setEffectiveLoanDuration(effective);

        TradeLoanFacility reloaded = persistDetachReloadAndMap(entity, facilityId);

        assertThat(reloaded.getEffectiveLoanDuration())
                .contains(LoanDuration.of(Period.of(1, 6, 0)).unwrap());
    }

    @Test
    @DisplayName("CS-2: all-null effective-duration columns rehydrate as Optional.empty() and do not throw")
    void effectiveLoanDurationNullRehydratesEmpty() {
        UUID facilityId = UUID.randomUUID();
        // A never-restructured facility: leave effectiveLoanDuration unset -> all three columns NULL.
        TradeLoanFacilityEntity entity = newFacilityEntity(facilityId);
        persistAndClear(entity);

        // Locks the Hibernate all-null-embeddable -> null assumption: rehydrate-and-map must not throw.
        assertThatCode(() -> reloadAndMap(facilityId)).doesNotThrowAnyException();
        assertThat(reloadAndMap(facilityId).getEffectiveLoanDuration()).isEmpty();
    }

    @Test
    @DisplayName("CS-1: a facility with no revocation column round-trips fine (revocation_reason dropped)")
    void facilityRoundTripsWithRevocationColumnDropped() {
        UUID facilityId = UUID.randomUUID();
        TradeLoanFacilityEntity entity = newFacilityEntity(facilityId);

        TradeLoanFacility reloaded = persistDetachReloadAndMap(entity, facilityId);

        assertThat(reloaded.getId()).isEqualTo(LoanFacilityId.of(facilityId));
        assertThat(reloaded.getCurrentState()).isEqualTo(FacilityStatus.APPROVED);
    }

    // --- helpers ---------------------------------------------------------------------------------------------------

    /** Persist in a write transaction, then detach + reload + map in a separate read-only transaction. */
    private TradeLoanFacility persistDetachReloadAndMap(TradeLoanFacilityEntity entity, UUID facilityId) {
        persistAndClear(entity);
        return reloadAndMap(facilityId);
    }

    private void persistAndClear(TradeLoanFacilityEntity entity) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            facilityRepository.save(entity);
            entityManager.flush();
        });
        // Drop the persistence context so the next read is a genuine reload from Postgres, not a first-level-cache hit.
        entityManager.clear();
    }

    /**
     * Reload + map inside a read-only transaction, exactly as {@code TradeLoanFacilityRepositoryAdapter#findById} does
     * — the open session lets the LAZY disbursementHistory.records collection initialize during mapping.
     */
    private TradeLoanFacility reloadAndMap(UUID facilityId) {
        return new TransactionTemplate(transactionManager)
                .execute(status -> mapper.map(facilityRepository
                        .findById(facilityId)
                        .orElseThrow(() -> new AssertionError("facility not found after save: " + facilityId))));
    }

    /**
     * Build a valid, fully-disbursed facility entity (mirrors {@code LoanFacilityTestFixture}'s construction so the
     * NOT-NULL columns of {@code loan_facilities} + {@code loan_applications} are satisfied). The caller mutates the
     * returned entity (e.g. sets disbursement history / restructuring txns) before persisting it.
     */
    private TradeLoanFacilityEntity newFacilityEntity(UUID facilityId) {
        String branchCode = "1";
        String loanTypeCode = "9001";
        String customerNumber = "12345678";
        String derivedValue = UUID.randomUUID().toString().substring(0, 8);

        TradeLoanApplicationEntity application =
                createApplication(loanTypeCode, derivedValue, branchCode, customerNumber);

        TradeLoanFacilityEntity facility = new TradeLoanFacilityEntity();
        facility.setId(facilityId);
        facility.setLoanApplication(application);
        facility.setLoanTypeId(UUID.randomUUID());
        facility.setLoanArrangementId(UUID.randomUUID());
        facility.setInstallmentScheduleId(UUID.randomUUID());
        // APPROVED: the only facility status that (a) does NOT require a non-null sanctionedLoan and (b) is NOT
        // declared
        // inconsistent with one — AbstractLoanFacility.validateInternalState enforces this on rehydrate. We avoid
        // building a full TradeSanctionedLoan graph (out of scope for this persistence round-trip), and the three
        // collections under test (disbursementHistory, restructuringTransactionNumbers, effectiveLoanDuration) are
        // mapped independently of facility status, so the migrated v2026.6.9 schema is still fully exercised.
        facility.setCurrentState(FacilityStatus.APPROVED);
        facility.setDisbursementDate(LocalDate.now(CLOCK).minusDays(30));
        facility.setFacilityType("TRADE");

        MoneyEmb disbursedAmount = new MoneyEmb();
        disbursedAmount.setAmount(new BigDecimal("100000000.0000"));
        disbursedAmount.setCurrency("IRR");
        facility.setTotalDisbursedAmount(disbursedAmount);

        facility.setIssueContractTransactionNumbers(List.of());
        facility.setDisbursementTransactionNumbers(List.of());
        facility.setCollaterals(List.of());
        facility.setRestructuringTransactionNumbers(List.of());

        return facility;
    }

    private TradeLoanApplicationEntity createApplication(
            String loanTypeCode, String derivedValue, String branchCode, String customerNumber) {

        TradeLoanApplicationEntity app = new TradeLoanApplicationEntity();
        app.setId(UUID.randomUUID());
        app.setRequestDate(Instant.now(CLOCK));
        app.setApplicantChannel(ApplicantChannel.INTERNET_BANK);
        app.setDisbursementMethod(DisbursementMethod.LUMP_SUM);

        MoneyEmb requestedAmount = new MoneyEmb();
        requestedAmount.setAmount(new BigDecimal("100000000.0000"));
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

        // RequestReason is mandatory on the domain AbstractLoanApplication (nullable in the DB), so the
        // facility->domain remap requires it; the entity-only LoanFacilityTestFixture never needed it.
        RequestReasonEmb requestReason = new RequestReasonEmb();
        requestReason.setCode("01");
        app.setRequestReason(requestReason);

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
}
