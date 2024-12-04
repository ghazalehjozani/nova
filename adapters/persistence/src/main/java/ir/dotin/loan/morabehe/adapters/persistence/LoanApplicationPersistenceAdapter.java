package ir.dotin.loan.morabehe.adapters.persistence;


import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanApplicationDocument;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanApplicationDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanApplicationRepository;
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
    private final MorabeheLoanApplicationDocumentMapper mapper;

    public LoanApplicationPersistenceAdapter(MongoTemplate mongoTemplate,
                                             MorabeheLoanApplicationRepository repository,
                                             MorabeheLoanApplicationDocumentMapper mapper) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    @Transactional
    public void save(MorabeheLoanApplication loanApplication) {
        Optional.ofNullable(loanApplication).map(mapper::mapToDocument).ifPresent(repository::save);
    }

    @Override
    public void update(MorabeheLoanApplication loanApplication) {
        Query query = new Query(Criteria.where("_id").is(loanApplication.getId().value().toString()));
        query.fields().include("version").include("createDate").include("updateDate");
        MorabeheLoanApplicationDocument document = mongoTemplate
                .findOne(query, MorabeheLoanApplicationDocument.class);
        MorabeheLoanApplicationDocument loanApplicationDocument = mapper.updateDocument(loanApplication, document);
        repository.save(loanApplicationDocument);
    }

    @Override
    public Optional<MorabeheLoanApplication> findById(MorabeheLoanApplicationId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString()));
        return Optional.ofNullable(mongoTemplate.findOne(query, MorabeheLoanApplicationDocument.class))
                .map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByApplicationNumber(ApplicationNumber applicationNumber) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanApplication.number").is(applicationNumber.value()));
        long count = mongoTemplate.count(query, MorabeheLoanApplicationDocument.class);
        return count > 0;
    }

}
