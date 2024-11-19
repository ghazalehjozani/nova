package ir.dotin.loan.morabehe.core.application.service.impl;

import ir.dotin.loan.morabehe.core.application.mapper.LoanRuleCommandMapper;
import ir.dotin.loan.morabehe.core.application.service.CreateLoanRuleService;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import org.springframework.stereotype.Service;

@Service
public class CreateLoanRuleServiceImpl implements CreateLoanRuleService {

    //TODO: use port to save


    @Override
    public String create(MorabeheLoanRule loanRule) {
        loanRule.createLoanRule();
        // save(loanRule)
        return "";
    }
}
