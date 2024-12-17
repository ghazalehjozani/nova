package ir.dotin.loan.morabehe.adapters.driven.persistence.loanrule;

import ir.dotin.loan.baseloan.domain.config.valueobject.LoanRuleCode;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loanrule.mapper.MorabeheLoanRuleEntryMapper;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loanrule.model.MorabeheLoanRuleEntry;
import ir.dotin.loan.morabehe.adapters.driven.persistence.loanrule.repository.MorabeheLoanRuleRepository;
import ir.dotin.loan.morabehe.core.application.ports.outbound.persistence.MorabeheLoanRulePersistencePort;
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
    private final MorabeheLoanRuleEntryMapper mapper;

    public LoanRulePersistenceAdapter(MongoTemplate mongoTemplate,
                                      MorabeheLoanRuleRepository repository,
                                      MorabeheLoanRuleEntryMapper mapper) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public void save(MorabeheLoanRule rule) {
        MorabeheLoanRuleEntry loanRuleDocument = mapper.mapToDocument(rule);
        repository.save(loanRuleDocument);
    }

    @Override
    @Transactional
    public void update(MorabeheLoanRule loanRule) {
        Query query = new Query(Criteria.where("_id").is(loanRule.id().value().toString()));
        query.fields().include("version").include("createDate").include("updateDate");
        MorabeheLoanRuleEntry document = mongoTemplate
                .findOne(query, MorabeheLoanRuleEntry.class);
        MorabeheLoanRuleEntry loanRuleDocument = mapper.updateDocument(loanRule, document);
        repository.save(loanRuleDocument);
    }

    @Override
    public Optional<MorabeheLoanRule> findById(MorabeheLoanRuleId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString())
                        .and("loanRule.disable").is(false));
        MorabeheLoanRuleEntry result = mongoTemplate.findOne(query,
                                                                MorabeheLoanRuleEntry.class);
        return Optional.ofNullable(result).map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByCode(LoanRuleCode code) {
        Query query = new Query();
        query.addCriteria(Criteria.where("loanRule.code").is(code.value()));
        long count = mongoTemplate.count(query, MorabeheLoanRuleEntry.class);
        return count > 0;
    }

    @Override
    public boolean existsByIdAndEnable(MorabeheLoanRuleId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id.value().toString())
                                  .and("loanRule.disable").is(false));
        long count = mongoTemplate.count(query, MorabeheLoanRuleEntry.class);
        return count > 0;
    }

}
