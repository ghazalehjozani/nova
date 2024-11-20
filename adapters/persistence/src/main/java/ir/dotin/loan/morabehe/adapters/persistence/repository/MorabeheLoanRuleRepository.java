package ir.dotin.loan.morabehe.adapters.persistence.repository;

import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanRuleDocument;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanRuleRepository extends MongoRepository<MorabeheLoanRuleDocument, UUID> {

}
