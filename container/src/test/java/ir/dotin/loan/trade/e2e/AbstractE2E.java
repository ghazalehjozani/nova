package ir.dotin.loan.trade.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ir.dotin.loan.trade.NovaApplication;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FetchSanctionDetailsPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindAccountByIdPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.FindOrCreateAccountPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.AccountValidationPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.accountservice.TransactionPostingPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.customerservice.CustomerServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.depositservice.DepositServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralReadPort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.CollateralServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.loanservice.LoanServicePort;
import ir.dotin.loan.trade.core.application.ports.outbound.client.samat.ValidateSamatPort;
import ir.dotin.loan.trade.e2e.fixture.FormulaTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanArrangementTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanFacilityTestFixture;
import ir.dotin.loan.trade.e2e.fixture.LoanTypeTestFixture;
import ir.dotin.loan.trade.e2e.orchestrator.MockPortConfigurator;
import ir.dotin.loan.trade.e2e.orchestrator.PrerequisiteOrchestrator;

@SpringBootTest(
        classes = {NovaApplication.class, E2ETestConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Import({
    FormulaTestFixture.class,
    LoanArrangementTestFixture.class,
    LoanTypeTestFixture.class,
    LoanFacilityTestFixture.class,
    MockPortConfigurator.class,
    PrerequisiteOrchestrator.class
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractE2E {

    // FcbValidationAdapter is ONE bean implementing seven ports. A separate @MockitoBean per port would
    // replace that same bean definition seven times over, and only the last replacement survives — the mock
    // then implements one interface and every other injection point fails with BeanNotOfRequiredType. So a
    // single mock carries all seven, and the sibling fields below just autowire the same instance.
    @MockitoBean(
            extraInterfaces = {
                CustomerServicePort.class,
                DepositServicePort.class,
                CollateralServicePort.class,
                CollateralReadPort.class,
                FetchSanctionDetailsPort.class,
                ValidateSamatPort.class
            })
    protected LoanServicePort loanServicePort;

    @MockitoBean
    protected AccountServicePort accountServicePort;

    @MockitoBean
    protected AccountValidationPort accountValidationPort;

    @MockitoBean
    protected TransactionPostingPort transactionPostingPort;

    @Autowired
    protected CollateralServicePort collateralServicePort;

    @Autowired
    protected CollateralReadPort collateralReadPort;

    @Autowired
    protected DepositServicePort depositServicePort;

    @Autowired
    protected CustomerServicePort customerServicePort;

    @MockitoBean
    protected FindOrCreateAccountPort findOrCreateAccountPort;

    @MockitoBean
    protected FindAccountByIdPort findAccountByIdPort;

    @Autowired
    protected FetchSanctionDetailsPort fetchSanctionDetailsPort;

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${e2e.auth.token:}")
    protected String authToken;

    @LocalServerPort
    protected int port;
}
