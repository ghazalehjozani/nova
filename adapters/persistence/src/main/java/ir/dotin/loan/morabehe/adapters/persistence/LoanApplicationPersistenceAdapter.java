package ir.dotin.loan.morabehe.adapters.persistence;


import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.ApplicationNumber;
import ir.dotin.loan.morabehe.adapters.persistence.mapper.MorabeheLoanApplicationDocumentMapper;
import ir.dotin.loan.morabehe.adapters.persistence.repository.MorabeheLoanApplicationRepository;
import ir.dotin.loan.morabehe.core.application.ports.secondary.MorabeheLoanApplicationPersistencePort;
import ir.dotin.loan.morabehe.core.domain.loanapplication.entity.application.MorabeheLoanApplication;
import ir.dotin.loan.morabehe.core.domain.loanapplication.valueobject.MorabeheLoanApplicationId;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class LoanApplicationPersistenceAdapter implements MorabeheLoanApplicationPersistencePort {

    private final MorabeheLoanApplicationRepository repository;
    private final MorabeheLoanApplicationDocumentMapper mapper;

    public LoanApplicationPersistenceAdapter(MorabeheLoanApplicationRepository repository,
                                             MorabeheLoanApplicationDocumentMapper mapper) {
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
        return repository.findById(id.value()).map(mapper::mapToAggregate);
    }

    @Override
    public boolean existsByApplicationNumber(ApplicationNumber applicationNumber) {
        return repository.existsByLoanApplicationDocument_NumberIgnoreCase(
                applicationNumber.value());
    }
}
