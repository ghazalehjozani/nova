package ir.dotin.loan.morabehe.adapters.persistence;

import ir.dotin.loan.morabehe.adapters.persistence.document.MorabeheLoanRuleDocument;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanRuleDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanRuleRepository;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanRulePersistencePort;
import ir.dotin.loan.morabehe.core.domain.config.entity.loanrule.MorabeheLoanRule;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class LoanRulePersistenceAdapter implements MorabeheLoanRulePersistencePort {

    private final MorabeheLoanRuleRepository repository;
    private final MorabeheLoanRuleDocumentMapper mapper;

    public LoanRulePersistenceAdapter(MorabeheLoanRuleRepository repository,
                                      MorabeheLoanRuleDocumentMapper mapper) {
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
    public Optional<MorabeheLoanRule> findById(UUID id) {
        return repository.findById(id).map(mapper::mapToAggregate);
    }

}
