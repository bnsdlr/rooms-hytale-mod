package de.bsdlr.rooms.services.room;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.services.room.block.RoomBlock;
import de.bsdlr.rooms.services.room.block.RoomBlockRole;
import de.bsdlr.rooms.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class Room {
    public static final BuilderCodec CODEC = BuilderCodec.builder(Room.class, Room::new)
            .append(new KeyedCodec<>("AllBlocks", new SetCodec(RoomBlock.CODEC, HashSet::new, false), true),
                    Room::setAllBlocks,
                    Room::getAllBlocks).add()
            .build();
    private String id;
    private Set<RoomBlock> allRoomBlocks = new HashSet<>();
    private final Set<RoomBlock> solidRoomBlocks = new HashSet<>();
    private final Set<RoomBlock> furnitures = new HashSet<>();
    private final Set<RoomBlock> empty = new HashSet<>();
    private final Set<RoomBlock> entrances = new HashSet<>();
    private final Set<RoomBlock> windows = new HashSet<>();
    private final Set<RoomBlock> lightSources = new HashSet<>();

    Room() {
    }

    private void setId(String id) {
        this.id = id;
    }

    private void setAllBlocks(Set<RoomBlock> roomBlocks) {
        this.allRoomBlocks = roomBlocks;

        for (RoomBlock roomBlock : roomBlocks) {
            addBlockToCategory(roomBlock);
        }
    }

    private void addBlockToCategory(RoomBlock roomBlock) {
        if (roomBlock.getLight() != null) lightSources.add(roomBlock);
        switch (roomBlock.getRole()) {
            case EMPTY -> empty.add(roomBlock);
            case SOLID -> solidRoomBlocks.add(roomBlock);
            case FURNITURE -> furnitures.add(roomBlock);
            case ENTRANCE -> entrances.add(roomBlock);
            case WINDOW -> windows.add(roomBlock);
        }
    }

    public String getId() {
        return id;
    }

    public Set<RoomBlock> getAllBlocks() {
        return allRoomBlocks;
    }

    public Set<RoomBlock> getSolidBlocks() {
        return solidRoomBlocks;
    }

    public Set<RoomBlock> getFurnitures() {
        return furnitures;
    }

    public Set<RoomBlock> getEmpty() {
        return empty;
    }

    public Set<RoomBlock> getEntrances() {
        return entrances;
    }

    public Set<RoomBlock> getWindows() {
        return windows;
    }

    public Set<RoomBlock> getLightSources() {
        return lightSources;
    }

    public Holder<EntityStore> createEntity(World world, Vector3d position) {
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Minecart");
        assert modelAsset != null;
        Model model = Model.createScaledModel(modelAsset, 1.0f);

        TransformComponent transform = new TransformComponent(position, new Vector3f(0, 0, 0));

        holder.addComponent(TransformComponent.getComponentType(), transform);
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        assert model.getBoundingBox() != null;
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(world.getEntityStore().getStore().getExternalData().takeNextNetworkId()));

        holder.ensureComponent(UUIDComponent.getComponentType());

        return holder;
    }

    public Set<Long> getEncodedBlocks() {
        Set<Long> blocks = new HashSet<>();

        for (RoomBlock roomBlock : allRoomBlocks) {
            blocks.add(Utils.encodePosition(roomBlock.getPos()));
        }

        return blocks;
    }

    public int calculateScore() {
        int score = 0;

        for (RoomBlock roomBlock : allRoomBlocks) {
            if (roomBlock.getBlockId() == BlockType.EMPTY_ID) continue;
            if (roomBlock.getBlockId() == BlockType.UNKNOWN_ID) continue;
            if (roomBlock.getRole() == RoomBlockRole.NONE) continue;
            switch (roomBlock.getRole()) {
                case SOLID -> score += 5;
                case FURNITURE -> score += 20;
                case ENTRANCE -> score += 50;
                case WINDOW -> score += 10;
            }
            if (roomBlock.getLight() != null) {
                score += 5;
            }
        }

        return score;
    }

    public static class Builder {
        public final Room room = new Room();

        public Builder() {}

        public void addBlock(RoomBlock roomBlock) {
            this.room.addBlockToCategory(roomBlock);
            this.room.allRoomBlocks.add(roomBlock);
        }

        public void removeIf(Predicate<RoomBlock> f) {
            this.room.allRoomBlocks.removeIf(f);
            this.room.solidRoomBlocks.removeIf(f);
            this.room.furnitures.removeIf(f);
            this.room.empty.removeIf(f);
            this.room.entrances.removeIf(f);
            this.room.lightSources.removeIf(f);
        }

        public Room build() throws FailedToDetectRoomException {
            if (this.room.entrances.isEmpty()) throw new FailedToDetectRoomException("Could not build room: room has no entrance");
            if (this.room.lightSources.isEmpty()) throw new FailedToDetectRoomException("Could not build room: room has no light source");
            return this.room;
        }
    }
}
