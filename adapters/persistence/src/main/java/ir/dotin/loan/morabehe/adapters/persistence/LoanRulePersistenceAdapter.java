package ir.dotin.loan.morabehe.adapters.persistence;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanRuleDocument;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanRuleDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanRuleRepository;
import ir.dotin.loan.morabehe.core.application.ports.secondary.persistence.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import ir.dotin.loan.morabehe.core.domain.config.valueobject.MorabeheLoanRuleId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class LoanRulePersistenceAdapter implements MorabeheLoanRulePersistencePort {

    private final MongoTemplate mongoTemplate;
    private final MorabeheLoanRuleRepository repository;
    private final MorabeheLoanRuleDocumentMapper mapper;

    public LoanRulePersistenceAdapter(MongoTemplate mongoTemplate,
                                      MorabeheLoanRuleRepository repository,
                                      MorabeheLoanRuleDocumentMapper mapper) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public void save(MorabeheLoanRule rule) {
        MorabeheLoanRuleDocument loanRuleDocument = mapper.mapToDocument(rule);
        repository.save(loanRuleDocument);
    }

    @Override
    public void update(MorabeheLoanRule loanRule) {
        Query query = new Query(Criteria.where("_id").is(loanRule.getId().value().toString()));
        query.fields().include("version").include("createDate").include("updateDate");
        MorabeheLoanRuleDocument document = mongoTemplate
                .findOne(query, MorabeheLoanRuleDocument.class);
        MorabeheLoanRuleDocument loanRuleDocument = mapper.updateDocument(loanRule, document);
        repository.save(loanRuleDocument);
    }

    @Override
    public Optional<MorabeheLoanRule> findById(MorabeheLoanRuleId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString())
                        .and("loanRule.disable").is(false));
        MorabeheLoanRuleDocument result = mongoTemplate.findOne(query,
                                                                MorabeheLoanRuleDocument.class);
        return Optional.ofNullable(result).map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByCode(LoanRuleCode code) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanRule.code").is(code.value()));
        long count = mongoTemplate.count(query, MorabeheLoanRuleDocument.class);
        return count > 0;
    }

    @Override
    public boolean existsByIdAndEnable(MorabeheLoanRuleId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString())
                                  .and("loanRule.disable").is(false));
        long count = mongoTemplate.count(query, MorabeheLoanRuleDocument.class);
        return count > 0;
    }

}
