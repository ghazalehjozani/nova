package ir.dotin.loan.morabehe.adapters.persistence.repository;

import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanApplicationDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface MorabeheLoanApplicationRepository extends
        MongoRepository<MorabeheLoanApplicationDocument, UUID> {

    boolean existsByLoanApplicationDocument_NumberIgnoreCase(@NonNull String number);


}
