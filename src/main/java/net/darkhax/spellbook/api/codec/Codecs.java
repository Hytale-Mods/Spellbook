package net.darkhax.spellbook.api.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;

public class Codecs {

    public static final Codec<AdjacentSide> SIDE = new EnumCodec<>(AdjacentSide.class);
    public static final Codec<AdjacentSide[]> SIDE_ARRAY = new ArrayCodec<>(SIDE, AdjacentSide[]::new);
}