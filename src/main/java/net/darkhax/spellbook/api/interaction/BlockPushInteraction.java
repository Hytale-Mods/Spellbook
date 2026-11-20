package net.darkhax.spellbook.api.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.darkhax.spellbook.api.codec.Codecs;
import net.darkhax.spellbook.api.util.WorldHelper;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPushInteraction extends SimpleBlockInteraction {

    @Nonnull
    public static final BuilderCodec<BlockPushInteraction> CODEC = BuilderCodec.builder(BlockPushInteraction.class, BlockPushInteraction::new)
            .documentation("Pushes the mob in a given direction.")
            .append(new KeyedCodec<>("Direction", Codecs.SIDE), (o, v) -> o.direction = v, o -> o.direction)
            .documentation("The direction to push mobs.")
            .add()
            .append(new KeyedCodec<>("Force", Codec.FLOAT), (o, v) -> o.force = v, o -> o.force)
            .documentation("How much force to push the mob with.")
            .add()
            .append(new KeyedCodec<>("PlayerOnly", Codec.BOOLEAN), (o, v) -> o.playersOnly = v, o -> o.playersOnly)
            .documentation("Should the block only push players?")
            .add()
            .build();

    private AdjacentSide direction = AdjacentSide.North;
    private float force = 1f;
    private boolean playersOnly = false;

    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull Vector3i targetBlock, @Nonnull CooldownHandler cooldownHandler) {
        if (WorldHelper.getBlockSection(world, targetBlock.x, targetBlock.y, targetBlock.z) != null) {
            final Vector3i blockPos = WorldHelper.getBaseBlock(world, targetBlock);
            final Ref<EntityStore> entityRef = context.getEntity();
            final Player player = commandBuffer.getComponent(entityRef, Player.getComponentType());
            if (!this.playersOnly || player != null) {
                final Velocity velocity = commandBuffer.getComponent(entityRef, Velocity.getComponentType());
                if (velocity != null) {
                    final Vector3ic directionOffset = WorldHelper.rotate(this.direction, WorldHelper.getRotationIndex(world, blockPos.x, blockPos.y, blockPos.z)).relativePosition;
                    velocity.addInstruction(new Vector3d(directionOffset.x() * this.force, (directionOffset.y() * this.force) + 1.01f, directionOffset.z() * this.force), null, ChangeVelocityType.Set);
                }
            }
        }
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock) {
    }
}
