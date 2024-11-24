package ir.dotin.loan.morabehe.container;

import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanApplicationValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.LoanTypeValidator;
import ir.dotin.loan.baseloan.domain.loanapplication.businessrule.validator.SanctionValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MorabeheLoanApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoanTest() {
        var loanApplicationValidator = applicationContext.getBean(LoanApplicationValidator.class);
        var loanTypeValidator = applicationContext.getBean(LoanTypeValidator.class);
        var sanctionValidator = applicationContext.getBean(SanctionValidator.class);

        assertAll(() -> assertNotNull(loanApplicationValidator),
                  () -> assertNotNull(loanTypeValidator),
                  () -> assertNotNull(sanctionValidator));
    }

}