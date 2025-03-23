package ir.dotin.loan.morabehe.adapters.driven.persistence.loanrule.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.morabehe.adapters.driven.persistence.loanrule.model.MorabeheLoanRuleEntry;

@Repository
public interface MorabeheLoanRuleRepository extends MongoRepository<MorabeheLoanRuleEntry, String> {}
