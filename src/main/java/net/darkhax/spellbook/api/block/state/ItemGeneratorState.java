package net.darkhax.spellbook.api.block.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.StateData;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.state.TickableBlockState;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import net.darkhax.spellbook.api.codec.Codecs;
import net.darkhax.spellbook.api.codec.output.IdOutput;
import net.darkhax.spellbook.api.codec.output.ItemOutput;
import net.darkhax.spellbook.api.util.WorldHelper;

import java.time.Duration;
import java.time.Instant;

public class ItemGeneratorState extends BlockState implements TickableBlockState {

    public static final BuilderCodec<ItemGeneratorState> CODEC = BuilderCodec.builder(ItemGeneratorState.class, ItemGeneratorState::new, BlockState.BASE_CODEC)
            .append(new KeyedCodec<>("StartTime", Codec.INSTANT, true), (i, v) -> i.startTime = v, i -> i.startTime).add()
            .append(new KeyedCodec<>("Timer", Codec.DOUBLE, true), (i, v) -> i.timer = v, i -> i.timer).add()
            .build();

    protected Instant startTime;
    protected double timer = -1;
    protected Data data;

    @Override
    public boolean initialize(BlockType blockType) {
        if (super.initialize(blockType) && blockType.getState() instanceof Data data) {
            this.data = data;
            return true;
        }
        return false;
    }

    @Override
    public void tick(float dt, int index, ArchetypeChunk<ChunkStore> archeChunk, Store<ChunkStore> store, CommandBuffer<ChunkStore> commandBuffer) {
        final World world = store.getExternalData().getWorld();
        final Instant currentTime = world.getEntityStore().getStore().getResource(WorldTimeResource.getResourceType()).getGameTime();
        if (startTime == null || startTime.isAfter(currentTime) || timer <= 0) {
            this.reset(currentTime);
        }
        else if (timer > 0 && Duration.between(startTime, currentTime).getSeconds() >= timer) {
            final Vector3i generatorPos = this.getBlockPosition();
            final BlockPosition pos = world.getBaseBlock(new BlockPosition(generatorPos.x, generatorPos.y, generatorPos.z));
            for (AdjacentSide side : this.data.exportFaces) {
                boolean exportedItems = false;
                final Vector3i exportPos = new Vector3i(pos.x, pos.y, pos.z).add(WorldHelper.rotate(side, this.getRotationIndex()).relativePosition);
                if (world.getState(exportPos.x, exportPos.y, exportPos.z, true) instanceof ItemContainerState containerState) {
                    for (ItemStack stack : this.data.output.outputList()) {
                        final ItemStackTransaction transaction = containerState.getItemContainer().addItemStack(stack);
                        final ItemStack remainder = transaction.getRemainder();
                        if (transaction.succeeded() && (remainder == null || remainder.isEmpty())) {
                            exportedItems = true;
                        }
                    }
                }
                if (this.data.exportOnce && exportedItems) {
                    break;
                }
            }
            this.reset(currentTime);
        }
    }

    protected void reset(Instant currentTime) {
        startTime = currentTime;
        timer = data.duration.min + (data.duration.max - data.duration.min) * HashUtil.random(startTime.getEpochSecond(), this.getBlockX(), this.getBlockY(), this.getBlockZ());
    }

    public static class Data extends StateData {

        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new, StateData.DEFAULT_CODEC)
                .appendInherited(new KeyedCodec<>("Output", ItemOutput.CODEC), (i, v) -> i.output = v, i -> i.output, (o, p) -> o.output = p.output)
                .documentation("Provides the items to be inserted into the container.")
                .add()

                .appendInherited(new KeyedCodec<>("ExportFaces", Codecs.SIDE_ARRAY), (i, v) -> i.exportFaces = v, i -> i.exportFaces, (o, p) -> o.exportFaces = p.exportFaces)
                .documentation("The adjacent faces to attempt exporting into.")
                .add()

                .appendInherited(new KeyedCodec<>("ExportOnce", Codec.BOOLEAN), (i, v) -> i.exportOnce = v, i -> i.exportOnce, (o, p) -> o.exportOnce = p.exportOnce)
                .documentation("Should the generator only export items to the first valid side that accepts items?")
                .add()

                .appendInherited(new KeyedCodec<>("Cooldown", ProtocolCodecs.RANGEF), (i, v) -> i.duration = v, i -> i.duration, (o, p) -> o.duration = p.duration)
                .documentation("A range that determines the cooldown before the next item is generated.")
                .add()
                .build();

        private ItemOutput output = new IdOutput();
        private AdjacentSide[] exportFaces = new AdjacentSide[0];
        private boolean exportOnce = true;
        protected Rangef duration;
    }
}