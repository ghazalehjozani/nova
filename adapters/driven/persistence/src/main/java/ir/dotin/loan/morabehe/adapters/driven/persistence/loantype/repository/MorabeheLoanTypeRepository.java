package ir.dotin.loan.morabehe.adapters.driven.persistence.loantype.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.morabehe.adapters.driven.persistence.loantype.model.MorabeheLoanTypeEntry;

@Repository
public interface MorabeheLoanTypeRepository extends MongoRepository<MorabeheLoanTypeEntry, String> {}
