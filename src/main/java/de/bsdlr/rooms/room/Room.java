package de.bsdlr.rooms.room;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import de.bsdlr.rooms.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.room.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class Room {
    public static final BuilderCodec CODEC = BuilderCodec.builder(Room.class, Room::new)
            .append(new KeyedCodec<>("AllBlocks", new SetCodec(Block.CODEC, HashSet::new, false), true),
                    Room::setAllBlocks,
                    Room::getAllBlocks).add()
            .build();
    private Set<Block> allBlocks = new HashSet<>();
    private Set<Block> solidBlocks = new HashSet<>();
    private Set<Block> furnitures = new HashSet<>();
    private Set<Block> empty = new HashSet<>();
    private Set<Block> entrances = new HashSet<>();
    private Set<Block> windows = new HashSet<>();
    private Set<Block> lightSources = new HashSet<>();

    private int score;
    private int roomId;

    Room() {
    }

    private void setAllBlocks(Set<Block> blocks) {
        this.allBlocks = blocks;

        for (Block block : blocks) {
            addBlockToCategory(block);
        }
    }

    private void addBlockToCategory(Block block) {
        if (block.getLight() != null) lightSources.add(block);
        switch (block.getRole()) {
            case EMPTY -> empty.add(block);
            case SOLID -> solidBlocks.add(block);
            case FURNITURE -> furnitures.add(block);
            case ENTRANCE -> entrances.add(block);
            case WINDOW -> windows.add(block);
        }
    }

    public Set<Block> getAllBlocks() {
        return allBlocks;
    }

    public Set<Block> getSolidBlocks() {
        return solidBlocks;
    }

    public Set<Block> getFurnitures() {
        return furnitures;
    }

    public Set<Block> getEmpty() {
        return empty;
    }

    public Set<Block> getEntrances() {
        return entrances;
    }

    public Set<Block> getWindows() {
        return windows;
    }

    public Set<Block> getLightSources() {
        return lightSources;
    }

    public static class RoomBuilder {
        public final Room room = new Room();

        public RoomBuilder() {}

        public void addBlock(Block block) {
            this.room.addBlockToCategory(block);
            this.room.allBlocks.add(block);
        }

        public void removeIf(Predicate<Block> f) {
            this.room.allBlocks.removeIf(f);
            this.room.solidBlocks.removeIf(f);
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
