package net.darkhax.spellbook.api.block.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlockPatternRule.AdjacentSide;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.darkhax.spellbook.api.codec.Codecs;
import net.darkhax.spellbook.api.util.EntityHelper;
import net.darkhax.spellbook.api.util.WorldHelper;
import net.darkhax.spellbook.impl.Spellbook;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A block component that pushes entities standing on the block in a given direction. Attach it to a block through the
 * block's BlockEntity components using the DarkhaxSpellbookConveyor ID.
 */
public class ConveyorState implements Component<ChunkStore> {

    @Nonnull
    public static final BuilderCodec<ConveyorState> CODEC = BuilderCodec.builder(ConveyorState.class, ConveyorState::new)
            .documentation("Pushes the mob in a given direction.")
            .append(new KeyedCodec<>("Direction", Codecs.SIDE), (o, v) -> o.direction = v, o -> o.direction)
            .documentation("The direction to push mobs.")
            .add()
            .append(new KeyedCodec<>("Force", Codec.FLOAT), (o, v) -> o.force = v, o -> o.force)
            .documentation("How much force to push the mob with.")
            .add()
            .append(new KeyedCodec<>("Height", Codec.FLOAT), (o, v) -> o.height = v, o -> o.height)
            .documentation("How high should the conveyor search?")
            .add()
            .append(new KeyedCodec<>("Players", Codec.BOOLEAN), (o, v) -> o.players = v, o -> o.players)
            .documentation("Should players be affected?")
            .add()
            .append(new KeyedCodec<>("Items", Codec.BOOLEAN), (o, v) -> o.items = v, o -> o.items)
            .documentation("Should items be affected?")
            .add()
            .append(new KeyedCodec<>("Entities", Codec.BOOLEAN), (o, v) -> o.entities = v, o -> o.entities)
            .documentation("Should entities be affected?")
            .add()
            .build();

    protected AdjacentSide direction = AdjacentSide.North;
    protected float force = 1f;
    protected boolean players = true;
    protected boolean entities = true;
    protected boolean items = true;
    protected float height = 0.99f;

    public static ComponentType<ChunkStore, ConveyorState> getComponentType() {
        return Spellbook.getConveyorComponentType();
    }

    @Nonnull
    @Override
    public Component<ChunkStore> clone() {
        final ConveyorState copy = new ConveyorState();
        copy.direction = this.direction;
        copy.force = this.force;
        copy.players = this.players;
        copy.entities = this.entities;
        copy.items = this.items;
        copy.height = this.height;
        return copy;
    }

    @Nonnull
    public static List<Ref<EntityStore>> getAllEntitiesInBox(Vector3i pos, float height, @Nonnull ComponentAccessor<EntityStore> components, boolean players, boolean entities, boolean items) {
        final List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        final Vector3d min = new Vector3d(pos.x, pos.y, pos.z);
        final Vector3d max = new Vector3d(pos.x + 1, pos.y + height, pos.z + 1);
        if (entities) {
            components.getResource(EntityModule.get().getEntitySpatialResourceType()).getSpatialStructure().collectBox(min, max, results);
        }
        if (players) {
            components.getResource(EntityModule.get().getPlayerSpatialResourceType()).getSpatialStructure().collectBox(min, max, results);
        }
        if (items) {
            components.getResource(EntityModule.get().getItemSpatialResourceType()).getSpatialStructure().collectBox(min, max, results);
        }
        return results;
    }

    /**
     * Ticks every loaded conveyor.
     */
    public static class Ticking extends EntityTickingSystem<ChunkStore> {

        private final ComponentType<ChunkStore, ConveyorState> componentType;
        private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoType = BlockModule.BlockStateInfo.getComponentType();

        public Ticking(ComponentType<ChunkStore, ConveyorState> componentType) {
            this.componentType = componentType;
        }

        @Override
        public Query<ChunkStore> getQuery() {
            return this.componentType;
        }

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
            final ConveyorState conveyor = archeChunk.getComponent(index, this.componentType);
            final BlockModule.BlockStateInfo info = archeChunk.getComponent(index, this.blockStateInfoType);
            if (conveyor == null || info == null) {
                return;
            }
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

            final World world = store.getExternalData().getWorld();
            final Store<EntityStore> entities = world.getEntityStore().getStore();
            final Vector3ic directionOffset = WorldHelper.rotate(conveyor.direction, blocks.getRotationIndex(localX, localY, localZ)).relativePosition;
            final Vector3d forceVec = new Vector3d((directionOffset.x() * 1.5f) * conveyor.force, (directionOffset.y() * 1.5f) * conveyor.force, (directionOffset.z() * 1.5f) * conveyor.force);
            for (Ref<EntityStore> target : getAllEntitiesInBox(pos, conveyor.height, entities, conveyor.players, conveyor.entities, conveyor.items)) {
                final Player player = entities.getComponent(target, Player.getComponentType());
                if (player != null) {
                    if (!EntityHelper.isCrouching(entities, target)) {
                        final Velocity velocity = entities.getComponent(target, Velocity.getComponentType());
                        if (velocity != null) {
                            velocity.addInstruction(new Vector3d(forceVec).mul(12), null, ChangeVelocityType.Set);
                        }
                    }
                }
                else {
                    final TransformComponent targetPos = entities.getComponent(target, TransformComponent.getComponentType());
                    final BoundingBox boundingBox = entities.getComponent(target, BoundingBox.getComponentType());
                    if (targetPos != null && boundingBox != null) {
                        final CollisionResult collision = new CollisionResult();
                        final Box box = boundingBox.getBoundingBox();
                        CollisionModule.findCollisions(box, new Vector3d(targetPos.getPosition()), new Vector3d(forceVec), collision, entities);
                        if (collision.getFirstBlockCollision() == null) {
                            targetPos.getPosition().add(forceVec);
                        }
                    }
                }
            }
        }
    }
}
