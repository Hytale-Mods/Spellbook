package net.darkhax.spellbook.impl.command;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import net.darkhax.spellbook.api.command.EnumBranchCommand;

public class RootCommand extends AbstractCommandCollection {

    public RootCommand() {
        super("spellbook", "The root command for all commands added by spellbook.");
        this.addSubCommand(new EnumBranchCommand<>("hand", "Prints info about the held item.", HandCommand.class));
    }
}