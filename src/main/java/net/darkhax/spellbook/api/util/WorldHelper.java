package net.darkhax.spellbook.api.util;

import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;

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
            case North, East, South, West -> {
                int horizontalIndex = side.ordinal() - 2;
                int rotatedIndex = Math.floorMod(horizontalIndex - rotation, 4);
                yield side(rotatedIndex + 2);
            }
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
}