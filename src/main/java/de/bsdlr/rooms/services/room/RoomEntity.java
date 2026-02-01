package de.bsdlr.rooms.services.room;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.server.core.Message;
import de.bsdlr.rooms.services.quality.Quality;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RoomEntity {
    public static final BuilderCodec<RoomEntity> CODEC = BuilderCodec.builder(RoomEntity.class, RoomEntity::new)
            .append(new KeyedCodec<>("UUID", Codec.UUID_BINARY),
                    (room, s) -> room.uuid = s,
                    room -> room.uuid)
            .add()
            .append(new KeyedCodec<>("Id", Codec.STRING),
                    (room, s) -> room.id = s,
                    room -> room.id)
            .add()
            .append(new KeyedCodec<>("Score", Codec.INTEGER),
                    (room, s) -> room.score = s,
                    room -> room.score)
            .add()
            .append(new KeyedCodec<>("Blocks", new SetCodec<>(Codec.LONG, HashSet::new, false)),
                    (room, s) -> room.blocks = s,
                    room -> room.blocks)
            .add()
            .build();
    protected UUID uuid;
    protected String id;
    protected int score;
    protected Set<Long> blocks;

    RoomEntity() {
        this.uuid = UUID.randomUUID();
    }

    public RoomEntity(@Nonnull Room room) {
        this.uuid = UUID.randomUUID();
        RoomType type = room.findRoomType();
        this.id = type.getId() == null ? "Room" : type.getId();
        this.score = room.calculateScore();
        this.blocks = room.getEncodedBlocks();
    }

    public RoomType getType() {
        return RoomType.getAssetMap().getAsset(id);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public Set<Long> getBlocks() {
        return blocks;
    }
}
