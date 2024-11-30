package ir.dotin.loan.morabehe.adapters.persistence.repository;


import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanTypeDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanTypeRepository extends
        MongoRepository<MorabeheLoanTypeDocument, UUID> {

    boolean existsByLoanType_Code(@NonNull String code);

    @Query("""
            {
                "_id": "?0",
                "loanRuleIds": { "$in": [?1] },
                "loanType.disable": false
            }
            """)
    Optional<MorabeheLoanTypeDocument> findByIdAndLoanRuleIdAndLoanType_DisableFalse(
            @NonNull UUID id, @NonNull UUID loanRuleId);



}
