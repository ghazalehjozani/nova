package ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication;


import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication.mapper.MorabeheLoanApplicationEntryMapper;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication.model.MorabeheLoanApplicationEntry;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loanapplication.repository.MorabeheLoanApplicationRepository;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class LoanApplicationPersistenceAdapter implements MorabeheLoanApplicationPersistencePort {

    private final MongoTemplate mongoTemplate;
    private final MorabeheLoanApplicationRepository repository;
    private final MorabeheLoanApplicationEntryMapper mapper;

    public LoanApplicationPersistenceAdapter(MongoTemplate mongoTemplate,
                                             MorabeheLoanApplicationRepository repository,
                                             MorabeheLoanApplicationEntryMapper mapper) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    @Transactional
    public void save(MorabeheLoanApplication loanApplication) {
        Optional.ofNullable(loanApplication).map(mapper::mapToDocument).ifPresent(repository::save);
        //TODO: Save To outbox
    }

    @Override
    public void update(MorabeheLoanApplication loanApplication) {
        Query query = new Query(Criteria.where("_id").is(loanApplication.id().value().toString()));
        query.fields().include("version").include("createDate").include("updateDate");
        MorabeheLoanApplicationEntry document = mongoTemplate
                .findOne(query, MorabeheLoanApplicationEntry.class);
        MorabeheLoanApplicationEntry loanApplicationDocument = mapper.updateDocument(loanApplication, document);
        repository.save(loanApplicationDocument);
    }

    @Override
    public Optional<MorabeheLoanApplication> findById(MorabeheLoanApplicationId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString()));
        return Optional.ofNullable(mongoTemplate.findOne(query, MorabeheLoanApplicationEntry.class))
                .map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByApplicationNumber(ApplicationNumber applicationNumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanApplication.number").is(applicationNumber.value()));
        long count = mongoTemplate.count(query, MorabeheLoanApplicationEntry.class);
        return count > 0;
    }

}
