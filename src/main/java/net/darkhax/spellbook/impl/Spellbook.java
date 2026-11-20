package net.darkhax.spellbook.impl;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import net.darkhax.spellbook.api.block.state.ConveyorState;
import net.darkhax.spellbook.api.block.state.ItemGeneratorState;
import net.darkhax.spellbook.api.codec.output.DropListOutput;
import net.darkhax.spellbook.api.codec.output.IdOutput;
import net.darkhax.spellbook.api.codec.output.ItemOutput;
import net.darkhax.spellbook.api.interaction.BlockPushInteraction;
import net.darkhax.spellbook.api.interaction.WarpHomeInteraction;
import net.darkhax.spellbook.api.test.TestHelper;
import net.darkhax.spellbook.impl.command.RootCommand;
import net.darkhax.spellbook.test.SpellbookTests;

import javax.annotation.Nonnull;

public class Spellbook extends JavaPlugin {

    public static final String GROUP = "Darkhax";
    public static final String NAME = "Spellbook";

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static ComponentType<ChunkStore, ItemGeneratorState> itemGeneratorComponentType;
    private static ComponentType<ChunkStore, ConveyorState> conveyorComponentType;

    public Spellbook(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // ItemOutput
        final var itemOutputCodec = this.getCodecRegistry(ItemOutput.CODEC);
        itemOutputCodec.register(IdOutput.ID, IdOutput.class, IdOutput.CODEC);
        itemOutputCodec.register(DropListOutput.ID, DropListOutput.class, DropListOutput.CODEC);

        // Interaction
        final var interactions = this.getCodecRegistry(Interaction.CODEC);
        interactions.register(idPascal("WarpHome"), WarpHomeInteraction.class, WarpHomeInteraction.CODEC);
        interactions.register(idPascal("BlockPush"), BlockPushInteraction.class, BlockPushInteraction.CODEC);

        // Block components
        final ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();
        itemGeneratorComponentType = chunkStoreRegistry.registerComponent(ItemGeneratorState.class, idPascal("ItemGenerator"), ItemGeneratorState.CODEC);
        chunkStoreRegistry.registerSystem(new ItemGeneratorState.Ticking(itemGeneratorComponentType));
        conveyorComponentType = chunkStoreRegistry.registerComponent(ConveyorState.class, idPascal("Conveyor"), ConveyorState.CODEC);
        chunkStoreRegistry.registerSystem(new ConveyorState.Ticking(conveyorComponentType));

        // Commands
        this.getCommandRegistry().registerCommand(new RootCommand());
    }

    @Override
    protected void start() {
        TestHelper.runTests(new SpellbookTests());
    }

    public static ComponentType<ChunkStore, ItemGeneratorState> getItemGeneratorComponentType() {
        return itemGeneratorComponentType;
    }

    public static ComponentType<ChunkStore, ConveyorState> getConveyorComponentType() {
        return conveyorComponentType;
    }

    public static String idPascal(String id) {
        return GROUP + NAME + id;
    }

    public static String idSnake(String id) {
        return GROUP + "_" + NAME + "_" + id;
    }
}