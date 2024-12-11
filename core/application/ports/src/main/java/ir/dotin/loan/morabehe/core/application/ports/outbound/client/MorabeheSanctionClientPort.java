package ir.dotin.loan.morabehe.core.application.ports.outbound.client;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;

import java.util.Optional;

public interface MorabeheSanctionClientPort {

    Optional<Sanction> getBySerial(SanctionSerial serial);

}
