package de.bsdlr.rooms.ui;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.services.room.RoomEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HudComponent implements Component<EntityStore> {
    public static final BuilderCodec<HudComponent> CODEC = BuilderCodec.builder(HudComponent.class, HudComponent::new)
            .append(new KeyedCodec<>("Room", RoomEntity.CODEC),
                    (hud, s) -> hud.roomEntity = s,
                    hud -> hud.roomEntity)
            .add()
            .build();
    public static ComponentType<EntityStore, HudComponent> TYPE;
    private RoomEntity roomEntity = null;

    public HudComponent() {}

    public HudComponent(@Nonnull HudComponent other) {
        this.roomEntity = other.roomEntity;
    }

    public static void setComponentType(ComponentType<EntityStore, HudComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, HudComponent> getComponentType() {
        return TYPE;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new HudComponent(this);
    }

    public RoomEntity getRoomEntity() {
        return roomEntity;
    }

    public void setRoomEntity(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
    }
}
