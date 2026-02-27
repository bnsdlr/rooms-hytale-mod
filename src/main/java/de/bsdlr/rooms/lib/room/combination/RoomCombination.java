package de.bsdlr.rooms.lib.room.combination;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import de.bsdlr.rooms.lib.room.Room;

import javax.annotation.Nonnull;
import java.util.*;

public class RoomCombination {
    public static final BuilderCodec<RoomCombination> CODEC = BuilderCodec.builder(RoomCombination.class, RoomCombination::new)
            .appendInherited(new KeyedCodec<>("RoomCombinationTypeId", Codec.STRING),
                    (roomCombination, s) -> roomCombination.roomCombinationTypeId = s,
                    roomCombination -> roomCombination.roomCombinationTypeId,
                    (roomCombination, parent) -> roomCombination.roomCombinationTypeId = parent.roomCombinationTypeId)
            .add()
            .appendInherited(new KeyedCodec<>("RoomUuids", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new)),
                    (roomCombination, s) -> roomCombination.roomUuids = List.of(s),
                    roomCombination -> roomCombination.roomUuids.toArray(UUID[]::new),
                    (roomCombination, parent) -> roomCombination.roomUuids = parent.roomUuids)
            .add()
            .build();
    protected String roomCombinationTypeId;
    protected List<UUID> roomUuids;

    RoomCombination() {}

    public RoomCombination(String roomCombinationTypeId, List<UUID> roomUuids) {
        this.roomCombinationTypeId = roomCombinationTypeId;
        this.roomUuids = roomUuids;
    }

    public boolean isValid() {
        // TODO: implement this method
        return false;
    }

    public List<UUID> getRoomUuids() {
        return roomUuids;
    }

    @Nonnull
    public String getRoomCombinationTypeId() {
        return roomCombinationTypeId;
    }
}
