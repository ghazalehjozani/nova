package ir.dotin.loan.morabehe.adapters.persistence.repository;


import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanTypeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MorabeheLoanTypeRepository extends MongoRepository<MorabeheLoanTypeDocument, UUID> {

    boolean existsByLoanType_Code(@NonNull String code);
}
