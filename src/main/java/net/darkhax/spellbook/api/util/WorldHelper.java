package net.darkhax.spellbook.api.util;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class WorldHelper {

    /**
     * Applies a horizontal rotation to an {@link AdjacentSide}.
     *
     * @param side     The side to rotate.
     * @param rotation The number of 90-degree counter-clockwise rotations to apply.
     * @return The rotated {@link AdjacentSide} or the input if the side is not horizontal.
     */
    public static AdjacentSide rotate(AdjacentSide side, int rotation) {
        return switch (side) {
            case North, East, South, West -> side(Math.floorMod((side.ordinal() - 2) - rotation, 4) + 2);
            case Up, Down -> side;
        };
    }

    /**
     * Applies a horizontal rotation to an {@link AdjacentSide}.
     *
     * @param side     The side to rotate.
     * @param rotation The amount to rotate.
     * @return The rotated {@link AdjacentSide} or the input if the side is not horizontal.
     */
    public static AdjacentSide rotate(AdjacentSide side, Rotation rotation) {
        return rotate(side, rotation.ordinal());
    }

    /**
     * Gets a side by its ordinal value.
     *
     * @param ordinal The ordinal to get.
     * @return The side corresponding to the given ordinal.
     */
    public static AdjacentSide side(int ordinal) {
        if (ordinal < 0 || ordinal > 5) {
            throw new IllegalArgumentException("AdjacentSide ordinal must be between 0 and 5. Received '" + ordinal + "'!");
        }
        return AdjacentSide.values()[ordinal];
    }

    /**
     * Gets a rotation by its ordinal value.
     *
     * @param ordinal The ordinal to get.
     * @return The rotation corresponding to the given ordinal.
     */
    public static Rotation rotation(int ordinal) {
        if (ordinal < 0 || ordinal > 3) {
            throw new IllegalArgumentException("Rotation ordinal must be between 0 and 3. Received '" + ordinal + "'!");
        }
        return Rotation.values()[ordinal];
    }

    /**
     * Drops items in the world at a given position.
     *
     * @param world    The world to spawn the items in.
     * @param drops    The items to drop.
     * @param position The position to spawn the items at.
     */
    public static void dropItems(World world, List<ItemStack> drops, Vector3d position) {
        final Store<EntityStore> store = world.getEntityStore().getStore();
        if (store != null && !drops.isEmpty()) {
            Holder<EntityStore>[] dropHolder = ItemComponent.generateItemDrops(store, drops, new Vector3d(position), new Rotation3f());
            store.addEntities(dropHolder, AddReason.SPAWN);
        }
    }

    /**
     * Drops items in the world at a given position.
     *
     * @param drops         The items to drop.
     * @param position      The position to spawn the items at.
     * @param store         The entity store to add the items to.
     * @param commandBuffer A buffer to add the items to.
     */
    public static void dropItems(List<ItemStack> drops, Vector3d position, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        if (!drops.isEmpty()) {
            Holder<EntityStore>[] dropHolder = ItemComponent.generateItemDrops(store, drops, new Vector3d(position), new Rotation3f());
            commandBuffer.addEntities(dropHolder, AddReason.SPAWN);
        }
    }

    /**
     * Determines if a range overlaps with a given section.
     *
     * @param sectionIndex The section index. Represents a 32 tall section of the world.
     * @param minY         The minium of the range.
     * @param maxY         The maximum of the range.
     * @return If the range overlaps with the given section.
     */
    public static boolean isRangeInSection(int sectionIndex, int minY, int maxY) {
        final int minRange = sectionIndex * 32;
        return minRange >= minY && (minRange + 32) <= maxY;
    }

    /**
     * Creates a vector representing the position of a block based on its index within a chunk section.
     *
     * @param blockIndex The index of a block within a chunk index.
     * @return A vector representing the position of the block.
     */
    public static Vector3i getPositionFromBlockIndex(int blockIndex) {
        return new Vector3i(ChunkUtil.xFromIndex(blockIndex), ChunkUtil.yFromIndex(blockIndex), ChunkUtil.zFromIndex(blockIndex));
    }

    /**
     * Gets the block section that contains a given world position.
     *
     * @param world The world to look in.
     * @param x     The x position of the block.
     * @param y     The y position of the block.
     * @param z     The z position of the block.
     * @return The block section, or null if it is not loaded.
     */
    @Nullable
    public static BlockSection getBlockSection(World world, int x, int y, int z) {
        final Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        return sectionRef != null && sectionRef.isValid() ? world.getChunkStore().getStore().getComponent(sectionRef, BlockSection.getComponentType()) : null;
    }

    /**
     * Gets the position of the base block for a given position. Multi-block structures are made of a base block and
     * filler blocks, and this resolves filler blocks back to their base.
     *
     * @param world The world to look in.
     * @param pos   The position to resolve.
     * @return The position of the base block, or the input position if it is not a filler block.
     */
    public static Vector3i getBaseBlock(World world, Vector3i pos) {
        final BlockSection section = getBlockSection(world, pos.x, pos.y, pos.z);
        if (section != null) {
            final int filler = section.getFiller(pos.x, pos.y, pos.z);
            if (filler != FillerBlockUtil.NO_FILLER) {
                return new Vector3i(pos.x - FillerBlockUtil.unpackX(filler), pos.y - FillerBlockUtil.unpackY(filler), pos.z - FillerBlockUtil.unpackZ(filler));
            }
        }
        return new Vector3i(pos);
    }

    /**
     * Gets the rotation index of a block in the world.
     *
     * @param world The world to look in.
     * @param x     The x position of the block.
     * @param y     The y position of the block.
     * @param z     The z position of the block.
     * @return The rotation index of the block, or 0 if the block is not loaded.
     */
    public static int getRotationIndex(World world, int x, int y, int z) {
        final BlockSection section = getBlockSection(world, x, y, z);
        return section != null ? section.getRotationIndex(x, y, z) : 0;
    }
}