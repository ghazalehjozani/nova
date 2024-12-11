package ir.dotin.loan.morabehe.adapters.client;

import ir.dotin.loan.baseloan.domain.config.valueobject.CollateralType;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Collateral;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.CollateralSerial;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.Sanction;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Money;
import ir.dotin.loan.baseloan.domain.shared.valueobject.Rate;
import ir.dotin.loan.morabehe.core.application.ports.outbound.client.MorabeheSanctionClientPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class MorabeheSanctionClientAdapter implements MorabeheSanctionClientPort {

    @Override
    public Optional<Sanction> getBySerial(SanctionSerial serial) {
        return Optional.of(Sanction.valueOf(serial, Money.valueOf(100), Duration.ofDays(8), Duration.ofDays(8),
                Rate.of(20), Set.of(Collateral.valueOf(new CollateralType("658", "check"),
                        25, "check-1",
                        new CollateralSerial("56")))));
    }

}
