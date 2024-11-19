package ir.dotin.platform.ddd.application.common.command;

import java.io.Serializable;

public interface CommandHandler<C extends Command, R extends Serializable> {

    R handle(C command);
}
