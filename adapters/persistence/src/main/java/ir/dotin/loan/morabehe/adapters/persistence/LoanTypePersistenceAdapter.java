package ir.dotin.loan.morabehe.adapters.persistence;


import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleId;
import ir.dotin.loan.baseloan.domain.config.valueobject.LoanTypeId;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanTypeDocument;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanTypeDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanTypeRepository;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class LoanTypePersistenceAdapter implements MorabeheLoanTypePersistencePort {

    private final MongoTemplate mongoTemplate;
    private final MorabeheLoanTypeRepository repository;
    private final MorabeheLoanTypeDocumentMapper mapper;

    public LoanTypePersistenceAdapter(MongoTemplate mongoTemplate,
                                      MorabeheLoanTypeRepository repository,
                                      MorabeheLoanTypeDocumentMapper mapper) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public void save(MorabeheLoanType loanType) {
        MorabeheLoanTypeDocument loanTypeDocument = mapper.mapToDocument(loanType);
        repository.save(loanTypeDocument);
    }

    @Override
    public Optional<MorabeheLoanType> findById(LoanTypeId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanType._id").is(id.value().toString())
                                  .and("loanType.disable").is(false));
        MorabeheLoanTypeDocument result = mongoTemplate.findOne(query,
                                                                MorabeheLoanTypeDocument.class);
        return Optional.ofNullable(result).map(mapper::mapToAggregate);
    }

    @Override
    public Optional<MorabeheLoanType> findByIdAndLoanRuleId(LoanTypeId id, LoanRuleId loanRuleId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanType._id").is(id.value().toString())
                                  .and("loanRuleIds").in(loanRuleId.value().toString())
                                  .and("loanType.disable").is(false));
        MorabeheLoanTypeDocument result = mongoTemplate.findOne(query,
                                                                MorabeheLoanTypeDocument.class);
        return Optional.ofNullable(result).map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByCode(LoanTypeCode code) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanType.code").is(code.value()));
        long count = mongoTemplate.count(query, MorabeheLoanTypeDocument.class);
        return count > 0;
    }
}
