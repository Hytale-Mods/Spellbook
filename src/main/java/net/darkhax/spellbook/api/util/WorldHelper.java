package net.darkhax.spellbook.api.util;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
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
            Holder<EntityStore>[] dropHolder = ItemComponent.generateItemDrops(store, drops, position.clone(), Vector3f.ZERO.clone());
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
            Holder<EntityStore>[] dropHolder = ItemComponent.generateItemDrops(store, drops, position.clone(), Vector3f.ZERO.clone());
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
}