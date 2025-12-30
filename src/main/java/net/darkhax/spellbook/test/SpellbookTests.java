package net.darkhax.spellbook.test;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;
import net.darkhax.spellbook.api.codec.output.DropListOutput;
import net.darkhax.spellbook.api.codec.output.IdOutput;
import net.darkhax.spellbook.api.codec.output.ItemOutput;
import net.darkhax.spellbook.api.test.Test;
import net.darkhax.spellbook.api.util.BsonHelper;
import net.darkhax.spellbook.api.util.WorldHelper;
import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.BsonValue;

import java.util.Arrays;
import java.util.stream.Collectors;

import static net.darkhax.spellbook.api.test.Assert.assertEquals;
import static net.darkhax.spellbook.api.test.Assert.assertType;

public class SpellbookTests {

    @Test
    void testRotateDirections() {
        // None
        assertEquals(AdjacentSide.North, WorldHelper.rotate(AdjacentSide.North, Rotation.None));
        assertEquals(AdjacentSide.East, WorldHelper.rotate(AdjacentSide.East, Rotation.None));
        assertEquals(AdjacentSide.South, WorldHelper.rotate(AdjacentSide.South, Rotation.None));
        assertEquals(AdjacentSide.West, WorldHelper.rotate(AdjacentSide.West, Rotation.None));
        assertEquals(AdjacentSide.Up, WorldHelper.rotate(AdjacentSide.Up, Rotation.None));
        assertEquals(AdjacentSide.Down, WorldHelper.rotate(AdjacentSide.Down, Rotation.None));

        // Ninety
        assertEquals(AdjacentSide.West, WorldHelper.rotate(AdjacentSide.North, Rotation.Ninety));
        assertEquals(AdjacentSide.North, WorldHelper.rotate(AdjacentSide.East, Rotation.Ninety));
        assertEquals(AdjacentSide.East, WorldHelper.rotate(AdjacentSide.South, Rotation.Ninety));
        assertEquals(AdjacentSide.South, WorldHelper.rotate(AdjacentSide.West, Rotation.Ninety));
        assertEquals(AdjacentSide.Up, WorldHelper.rotate(AdjacentSide.Up, Rotation.Ninety));
        assertEquals(AdjacentSide.Down, WorldHelper.rotate(AdjacentSide.Down, Rotation.Ninety));

        // OneEighty
        assertEquals(AdjacentSide.South, WorldHelper.rotate(AdjacentSide.North, Rotation.OneEighty));
        assertEquals(AdjacentSide.West, WorldHelper.rotate(AdjacentSide.East, Rotation.OneEighty));
        assertEquals(AdjacentSide.North, WorldHelper.rotate(AdjacentSide.South, Rotation.OneEighty));
        assertEquals(AdjacentSide.East, WorldHelper.rotate(AdjacentSide.West, Rotation.OneEighty));
        assertEquals(AdjacentSide.Up, WorldHelper.rotate(AdjacentSide.Up, Rotation.OneEighty));
        assertEquals(AdjacentSide.Down, WorldHelper.rotate(AdjacentSide.Down, Rotation.OneEighty));

        // TwoSeventy
        assertEquals(AdjacentSide.East, WorldHelper.rotate(AdjacentSide.North, Rotation.TwoSeventy));
        assertEquals(AdjacentSide.South, WorldHelper.rotate(AdjacentSide.East, Rotation.TwoSeventy));
        assertEquals(AdjacentSide.West, WorldHelper.rotate(AdjacentSide.South, Rotation.TwoSeventy));
        assertEquals(AdjacentSide.North, WorldHelper.rotate(AdjacentSide.West, Rotation.TwoSeventy));
        assertEquals(AdjacentSide.Up, WorldHelper.rotate(AdjacentSide.Up, Rotation.TwoSeventy));
        assertEquals(AdjacentSide.Down, WorldHelper.rotate(AdjacentSide.Down, Rotation.TwoSeventy));
    }

    @Test
    public void testItemOutputCodec() {
        final IdOutput idOutput = assertType(IdOutput.class, decode(ItemOutput.CODEC, "{\"Type\":\"Item\",\"ItemId\":\"Ingredient_Life_Essence\",\"Quantity\":5}"));
        assertEquals("Ingredient_Life_Essence", idOutput.itemId());
        assertEquals(5, idOutput.amount());
        idOutput.output(s -> {
            assertEquals(5, s.getQuantity());
            assertEquals("Ingredient_Life_Essence", s.getItemId());
        });

        final DropListOutput dropOut = assertType(DropListOutput.class, decode(ItemOutput.CODEC, "{\"Type\":\"DropList\",\"DropList\":\"Drop_Cow\",\"Count\":3}"));
        assertEquals("Drop_Cow", dropOut.dropListId());
        assertEquals(3, dropOut.count());
        assertEquals(6, dropOut.outputList().size());
    }

    private static BsonArray array(String... values) {
        return new BsonArray(Arrays.stream(values).map(BsonString::new).collect(Collectors.toList()));
    }

    private static <T> T decode(Codec<T> codec, String value) {
        return decode(codec, BsonHelper.parseValue(value));
    }

    @SuppressWarnings("deprecation")
    private static <T> T decode(Codec<T> codec, BsonValue value) {
        return codec.decode(value);
    }
}
