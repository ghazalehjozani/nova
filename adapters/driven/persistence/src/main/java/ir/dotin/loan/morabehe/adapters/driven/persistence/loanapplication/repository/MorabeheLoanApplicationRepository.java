package ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication.model.MorabeheLoanApplicationEntry;

@Repository
public interface MorabeheLoanApplicationRepository extends MongoRepository<MorabeheLoanApplicationEntry, String> {}
