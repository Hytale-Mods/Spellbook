package net.darkhax.spellbook.api.util;

import com.hypixel.hytale.protocol.Rangef;

import java.util.Random;

public class MathsHelper {

    /**
     * A shared random number generator that can be used when one is not provided or available.
     */
    public static final Random RNG = new Random();

    /**
     * Picks a random number between a range.
     *
     * @param range The range of numbers to choose from.
     * @return The chosen int.
     */
    public static int fromRange(Rangef range) {
        int low = (int) Math.ceil(range.min);
        int high = (int) Math.floor(range.max);
        return low + RNG.nextInt(high - low + 1);
    }

    /**
     * Get a random int from an inclusive range.
     *
     * @param random The source of random.
     * @param min    The lowest possible value.
     * @param max    The highest possible value.
     * @return A random number from the range of values.
     */
    public static int nextIntInclusive(Random random, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        return random.nextInt(max - min + 1) + min;
    }
}