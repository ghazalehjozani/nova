package ir.dotin.loan.morabehe.adapters.driven.persistence;


import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.LoanTypeCode;
import ir.dotin.loan.morabehe.adapters.driven.persistence.document.MorabeheLoanTypeDocument;
import ir.dotin.loan.morabehe.adapters.driven.persistence.mapper.MorabeheLoanTypeDocumentMapper;
import ir.dotin.loan.morabehe.adapters.driven.persistence.repository.MorabeheLoanTypeRepository;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanTypePersistencePort;
import ir.dotin.loan.morabehe.core.domain.config.entity.loantype.MorabeheLoanType;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanTypeId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public Optional<MorabeheLoanType> findById(MorabeheLoanTypeId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString())
                                  .and("loanType.disable").is(false));
        MorabeheLoanTypeDocument result = mongoTemplate.findOne(query,
                                                                MorabeheLoanTypeDocument.class);
        return Optional.ofNullable(result).map(mapper::mapToAggregate);
    }

    @Override
    public Optional<MorabeheLoanType> findByIdAndLoanRuleId(MorabeheLoanTypeId id,
                                                            MorabeheLoanRuleId loanRuleId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString())
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
