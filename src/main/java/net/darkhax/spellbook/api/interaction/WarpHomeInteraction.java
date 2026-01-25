package net.darkhax.spellbook.api.interaction;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class WarpHomeInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<WarpHomeInteraction> CODEC = BuilderCodec.builder(WarpHomeInteraction.class, WarpHomeInteraction::new, SimpleInstantInteraction.CODEC).build();

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldown) {
        final CommandBuffer<EntityStore> buffer = context.getCommandBuffer();
        if (buffer != null) {
            final Ref<EntityStore> ref = context.getEntity();
            final World world = buffer.getExternalData().getWorld();
            final TransformComponent transform = buffer.getComponent(ref, TransformComponent.getComponentType());
            if (transform != null) {
                final HeadRotation headRotation = buffer.getComponent(ref, HeadRotation.getComponentType());
                if (headRotation != null) {
                    final Vector3d oldPos = transform.getPosition().clone();
                    final Vector3f oldRotation = headRotation.getRotation().clone();
                    buffer.ensureAndGetComponent(ref, TeleportHistory.getComponentType()).append(world, oldPos, oldRotation, "Home");
                    Player.getRespawnPosition(ref, world.getName(), buffer).thenAcceptAsync((homeTransform) -> {
                        Teleport teleportComponent = Teleport.createForPlayer((World) null, homeTransform);
                        buffer.addComponent(ref, Teleport.getComponentType(), teleportComponent);
                    }, world);
                }
            }
        }
    }
}