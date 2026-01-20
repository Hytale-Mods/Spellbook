package net.darkhax.spellbook.api.command;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Locale;

public interface IEnumCommand {

    void execute(@NonNullDecl CommandContext ctx);

    default String getName() {
        if (this instanceof Enum<?> e) {
            return e.name().toLowerCase(Locale.ROOT);
        }
        throw new IllegalStateException("Can not resolve name for class " + this.getClass().getCanonicalName());
    }

    String getDescription();

    default AbstractCommand buildCommand() {
        final CommandBase cmd = new CommandBase(this.getName(), this.getDescription()) {
            @Override
            protected void executeSync(@NonNullDecl CommandContext ctx) {
                IEnumCommand.this.execute(ctx);
            }
        };
        return cmd;
    }
}