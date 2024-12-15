package ir.dotin.loan.morabehe.adapters.driven.persistence.repository;


import ir.dotin.loan.morabehe.adapters.driven.persistence.model.MorabeheLoanTypeEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanTypeRepository extends
        MongoRepository<MorabeheLoanTypeEntry, String> {

}
