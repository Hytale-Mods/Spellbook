package net.darkhax.spellbook.impl;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateRegistry;
import net.darkhax.spellbook.api.block.state.ItemGeneratorState;
import net.darkhax.spellbook.api.codec.output.DropListOutput;
import net.darkhax.spellbook.api.codec.output.IdOutput;
import net.darkhax.spellbook.api.codec.output.ItemOutput;
import net.darkhax.spellbook.api.test.TestHelper;
import net.darkhax.spellbook.test.SpellbookTests;

import javax.annotation.Nonnull;

public class Spellbook extends JavaPlugin {

    public static final String GROUP = "Darkhax";
    public static final String NAME = "Spellbook";

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Spellbook(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // ItemOutput
        final var itemOutputCodec = this.getCodecRegistry(ItemOutput.CODEC);
        itemOutputCodec.register(IdOutput.ID, IdOutput.class, IdOutput.CODEC);
        itemOutputCodec.register(DropListOutput.ID, DropListOutput.class, DropListOutput.CODEC);

        final BlockStateRegistry blockStateRegistry = this.getBlockStateRegistry();
        blockStateRegistry.registerBlockState(ItemGeneratorState.class, idPascal("ItemGenerator"), ItemGeneratorState.CODEC, ItemGeneratorState.Data.class, ItemGeneratorState.Data.CODEC);
    }

    @Override
    protected void start() {
        TestHelper.runTests(new SpellbookTests());
    }

    public static String idPascal(String id) {
        return GROUP + NAME + id;
    }

    public static String idSnake(String id) {
        return GROUP + "_" + NAME + "_" + id;
    }
}