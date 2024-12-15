package ir.dotin.loan.morabehe.adapters.driven.persistence.repository;

import ir.dotin.loan.morabehe.adapters.driven.persistence.model.MorabeheLoanRuleEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanRuleRepository extends
        MongoRepository<MorabeheLoanRuleEntry, String> {

}
