package ir.dotin.loan.morabehe.core.application.ports.outbound.client;

import java.util.Optional;

import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;

public interface MorabeheSanctionClientPort {

    Optional<Sanction> getBySerial(SanctionSerial serial);
}
