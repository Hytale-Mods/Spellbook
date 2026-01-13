package net.darkhax.spellbook.api.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

public class WarpHomeInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<WarpHomeInteraction> CODEC = BuilderCodec.builder(WarpHomeInteraction.class, WarpHomeInteraction::new, SimpleInstantInteraction.CODEC).build();

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldown) {
        final Ref<EntityStore> ref = context.getEntity();
        final CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (EntityUtils.getEntity(ref, commandBuffer) instanceof Player player) {
            final Transform transform = getClosestRespawnPoint(player, commandBuffer);
            if (transform != null) {
                commandBuffer.addComponent(player.getReference(), Teleport.getComponentType(), new Teleport(null, transform));
            }
        }
    }

    private static Transform getClosestRespawnPoint(Player player, ComponentAccessor<EntityStore> componentAccessor) {
        final World world = player.getWorld();
        final Ref<EntityStore> ref = player.getReference();
        final PlayerConfigData playerData = player.getPlayerConfigData();
        final PlayerRespawnPointData[] respawnPoints = playerData.getPerWorldData(world.getName()).getRespawnPoints();
        if (respawnPoints != null && respawnPoints.length != 0) {
            final TransformComponent playerTransformPos = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
            if (playerTransformPos != null) {
                final Vector3d playerPos = playerTransformPos.getPosition();
                final Optional<PlayerRespawnPointData> nearestPos = Arrays.stream(respawnPoints).min((a, b) -> {
                    final Vector3d posA = a.getRespawnPosition();
                    final Vector3d posB = b.getRespawnPosition();
                    return Double.compare(playerPos.distanceSquaredTo(posA.x, playerPos.y, posA.z), playerPos.distanceSquaredTo(posB.x, playerPos.y, posB.z));
                });
                return new Transform((nearestPos.get()).getRespawnPosition());
            }
        }
        final Transform worldSpawnPoint = world.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, componentAccessor);
        worldSpawnPoint.setRotation(Vector3f.ZERO);
        return worldSpawnPoint;
    }
}