package ir.dotin.loan.morabehe.adapters.driven.persistence.repository;

import ir.dotin.loan.morabehe.adapters.driven.persistence.document.MorabeheLoanRuleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanRuleRepository extends
        MongoRepository<MorabeheLoanRuleDocument, String> {

}
