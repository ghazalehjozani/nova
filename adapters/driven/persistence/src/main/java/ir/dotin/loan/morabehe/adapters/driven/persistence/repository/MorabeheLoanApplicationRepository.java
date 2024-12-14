package ir.dotin.loan.morabehe.adapters.driven.persistence.repository;

import ir.dotin.loan.morabehe.adapters.driven.persistence.document.MorabeheLoanApplicationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanApplicationRepository extends
        MongoRepository<MorabeheLoanApplicationDocument, String> {

}
