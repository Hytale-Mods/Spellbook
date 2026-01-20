package net.darkhax.spellbook.impl.command;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import net.darkhax.spellbook.api.command.IEnumCommand;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.function.BiFunction;

public enum HandCommand implements IEnumCommand {

    ID("Prints the ID of the held item.", (p, s) -> Message.raw(s.getItemId())),
    JSON("Prints the held item as a JSON object.", (p, s) -> Message.raw(ItemStack.CODEC.encode(s, new ExtraInfo()).toJson()));

    private final String description;
    private final BiFunction<Player, ItemStack, Message> messageFunc;

    HandCommand(String description, BiFunction<Player, ItemStack, Message> messageFunc) {
        this.description = description;
        this.messageFunc = messageFunc;
    }

    @Override
    public void execute(@NonNullDecl CommandContext ctx) {
        if (ctx.sender() instanceof Player player) {
            final ItemStack heldItem = player.getInventory().getActiveHotbarItem();
            if (heldItem != null && heldItem.isValid()) {
                ctx.sendMessage(this.messageFunc.apply(player, heldItem));
            }
        }
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}