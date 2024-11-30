package ir.dotin.loan.morabehe.adapters.persistence.repository;


import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanTypeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanTypeRepository extends
        MongoRepository<MorabeheLoanTypeDocument, String> {

}
