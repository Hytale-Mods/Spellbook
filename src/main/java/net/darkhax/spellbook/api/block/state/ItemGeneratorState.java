package net.darkhax.spellbook.api.block.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import net.darkhax.spellbook.api.codec.Codecs;
import net.darkhax.spellbook.api.codec.output.IdOutput;
import net.darkhax.spellbook.api.codec.output.ItemOutput;
import net.darkhax.spellbook.api.util.MathsHelper;
import net.darkhax.spellbook.api.util.WorldHelper;
import net.darkhax.spellbook.impl.Spellbook;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

/**
 * A block component that periodically generates items and inserts them into adjacent containers. Attach it to a block
 * through the block's BlockEntity components using the DarkhaxSpellbookItemGenerator ID.
 */
public class ItemGeneratorState implements Component<ChunkStore> {

    public static final BuilderCodec<ItemGeneratorState> CODEC = BuilderCodec.builder(ItemGeneratorState.class, ItemGeneratorState::new)
            .append(new KeyedCodec<>("Output", ItemOutput.CODEC), (i, v) -> i.output = v, i -> i.output)
            .documentation("Provides the items to be inserted into the container.")
            .add()

            .append(new KeyedCodec<>("ExportFaces", Codecs.SIDE_ARRAY), (i, v) -> i.exportFaces = v, i -> i.exportFaces)
            .documentation("The adjacent faces to attempt exporting into.")
            .add()

            .append(new KeyedCodec<>("ExportOnce", Codec.BOOLEAN), (i, v) -> i.exportOnce = v, i -> i.exportOnce)
            .documentation("Should the generator only export items to the first valid side that accepts items?")
            .add()

            .append(new KeyedCodec<>("Cooldown", ProtocolCodecs.RANGEF), (i, v) -> i.duration = v, i -> i.duration)
            .documentation("A range that determines the cooldown before the next item is generated.")
            .add()

            .append(new KeyedCodec<>("RemainingTicks", Codec.INTEGER, true), (i, v) -> i.remainingTicks = v, i -> i.remainingTicks)
            .add()
            .build();

    protected ItemOutput output = new IdOutput();
    protected AdjacentSide[] exportFaces = new AdjacentSide[0];
    protected boolean exportOnce = true;
    protected Rangef duration = new Rangef(100, 100);
    protected int remainingTicks = Integer.MAX_VALUE;

    public static ComponentType<ChunkStore, ItemGeneratorState> getComponentType() {
        return Spellbook.getItemGeneratorComponentType();
    }

    protected void reset() {
        this.remainingTicks = MathUtil.ceil(duration.min + (duration.max - duration.min) * MathsHelper.RNG.nextFloat());
    }

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        final ItemGeneratorState copy = new ItemGeneratorState();
        copy.output = this.output;
        copy.exportFaces = this.exportFaces.clone();
        copy.exportOnce = this.exportOnce;
        copy.duration = this.duration;
        copy.remainingTicks = this.remainingTicks;
        return copy;
    }

    /**
     * Ticks every loaded item generator.
     */
    public static class Ticking extends EntityTickingSystem<ChunkStore> {

        private final ComponentType<ChunkStore, ItemGeneratorState> componentType;
        private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoType = BlockModule.BlockStateInfo.getComponentType();

        public Ticking(ComponentType<ChunkStore, ItemGeneratorState> componentType) {
            this.componentType = componentType;
        }

        @Override
        public Query<ChunkStore> getQuery() {
            return this.componentType;
        }

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            final ItemGeneratorState generator = archeChunk.getComponent(index, this.componentType);
            final BlockModule.BlockStateInfo info = archeChunk.getComponent(index, this.blockStateInfoType);
            if (generator == null || info == null) {
                return;
            }
            if (generator.remainingTicks > generator.duration.max) {
                generator.reset();
            }
            else if (generator.remainingTicks <= 0) {
                final Ref<ChunkStore> sectionRef = info.getSectionRef();
                if (!sectionRef.isValid()) {
                    return;
                }
                final ChunkSection section = store.getComponent(sectionRef, ChunkSection.getComponentType());
                final BlockSection blocks = store.getComponent(sectionRef, BlockSection.getComponentType());
                if (section == null || blocks == null) {
                    return;
                }
                final int blockIndex = info.getIndex();
                final int localX = ChunkUtil.xFromIndex(blockIndex);
                final int localY = ChunkUtil.yFromIndex(blockIndex);
                final int localZ = ChunkUtil.zFromIndex(blockIndex);
                final Vector3i pos = new Vector3i(ChunkUtil.worldCoordFromLocalCoord(section.getX(), localX), ChunkUtil.worldCoordFromLocalCoord(section.getY(), localY), ChunkUtil.worldCoordFromLocalCoord(section.getZ(), localZ));
                final int rotation = blocks.getRotationIndex(localX, localY, localZ);
                final World world = store.getExternalData().getWorld();

                for (AdjacentSide side : generator.exportFaces) {
                    boolean exportedItems = false;
                    final Vector3i exportPos = WorldHelper.getBaseBlock(world, new Vector3i(pos).add(WorldHelper.rotate(side, rotation).relativePosition));
                    final ItemContainerBlock container = BlockModule.getComponent(ItemContainerBlock.getComponentType(), world, exportPos.x, exportPos.y, exportPos.z);
                    if (container != null && container.getItemContainer() != null) {
                        for (ItemStack stack : generator.output.outputList()) {
                            final ItemStackTransaction transaction = container.getItemContainer().addItemStack(stack);
                            final ItemStack remainder = transaction.getRemainder();
                            if (transaction.succeeded() && (remainder == null || remainder.isEmpty())) {
                                exportedItems = true;
                            }
                        }
                    }
                    if (generator.exportOnce && exportedItems) {
                        break;
                    }
                }
                generator.reset();
                info.markNeedsSaving(commandBuffer);
            }
            else {
                generator.remainingTicks--;
            }
        }
    }
}
