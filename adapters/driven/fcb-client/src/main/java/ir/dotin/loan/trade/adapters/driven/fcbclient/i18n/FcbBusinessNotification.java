package ir.dotin.loan.trade.adapters.driven.fcbclient.i18n;

import ir.dotin.platform.commons.core.Notification;

public class FcbBusinessNotification extends RuntimeException {

    private final Notification notification;

    public FcbBusinessNotification(Notification notification) {
        super(String.valueOf(notification.errors())); // Use error messages for the exception message
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }
}
