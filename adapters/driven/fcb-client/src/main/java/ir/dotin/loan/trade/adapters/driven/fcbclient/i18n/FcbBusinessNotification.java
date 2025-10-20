package ir.dotin.loan.trade.adapters.driven.fcbclient.i18n;

import ir.dotin.platform.commons.core.Notification;
import ir.dotin.platform.commons.core.exception.OperationalException;

public class FcbBusinessNotification extends OperationalException {

    private final Notification notification; // TODO: Remove this, business exceptions should not throw in adapter

    public FcbBusinessNotification(Notification notification) {
        super(String.valueOf(notification.errors())); // Use error messages for the exception message
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }
}
