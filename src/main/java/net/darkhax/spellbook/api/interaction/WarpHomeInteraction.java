package net.darkhax.spellbook.api.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class WarpHomeInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<WarpHomeInteraction> CODEC = BuilderCodec.builder(WarpHomeInteraction.class, WarpHomeInteraction::new, SimpleInstantInteraction.CODEC).build();

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldown) {
        final Ref<EntityStore> ref = context.getEntity();
        final CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (EntityUtils.getEntity(ref, commandBuffer) instanceof Player player) {
            final Transform transform = Player.getRespawnPosition(player.getReference(), player.getWorld().getName(), commandBuffer);
            commandBuffer.addComponent(player.getReference(), Teleport.getComponentType(), new Teleport(null, transform));
        }
    }
}