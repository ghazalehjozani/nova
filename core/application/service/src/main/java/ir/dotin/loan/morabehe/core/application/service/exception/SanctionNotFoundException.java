package ir.dotin.loan.morabehe.core.application.service.exception;

import ir.dotin.loan.baseloan.core.application.service.exception.ValueObjectNotFound;
import ir.dotin.loan.baseloan.domain.loanapplication.valueobject.SanctionSerial;

public class SanctionNotFoundException extends ValueObjectNotFound {

    public SanctionNotFoundException(SanctionSerial serial) {
        super(serial.toString());
    }
}
