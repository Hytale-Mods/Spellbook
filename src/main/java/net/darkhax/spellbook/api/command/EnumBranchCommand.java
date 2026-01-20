package net.darkhax.spellbook.api.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class EnumBranchCommand<E extends Enum<E> & IEnumCommand> extends CommandBase {

    private final Map<String, String> branchInfo = new HashMap<>();

    public EnumBranchCommand(@NonNullDecl String name, @NonNullDecl String description, Class<E> clazz) {
        super(name, description);
        for (E entry : clazz.getEnumConstants()) {
            this.addSubCommand(entry.buildCommand());
            branchInfo.put(entry.getName(), entry.getDescription());
        }
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext ctx) {
        final StringJoiner output = new StringJoiner("\n");
        for (Map.Entry<String, String> entry : branchInfo.entrySet()) {
            output.add(entry.getKey() + " - " + entry.getValue());
        }
        ctx.sendMessage(Message.raw(output.toString()));
    }
}