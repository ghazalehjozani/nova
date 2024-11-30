package ir.dotin.loan.morabehe.adapters.persistence;


import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanApplicationDocument;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanApplicationDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanApplicationRepository;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<MorabeheLoanApplication> findById(MorabeheLoanApplicationId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanApplication._id").is(id.value().toString()));
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
