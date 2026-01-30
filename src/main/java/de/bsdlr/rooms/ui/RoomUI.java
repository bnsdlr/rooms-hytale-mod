package de.bsdlr.rooms.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public class RoomUI extends CustomUIHud {
    public RoomUI(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder ui) {
        ui.append("Room.ui");
    }

    private void set(String id, Message message) {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set(id, message);
        update(false, uiCommandBuilder);
    }

    public void setRoomName(String roomName) {
        set("#RoomName.TextSpans", Message.raw(roomName));
    }

    public void setScore(int score) {
        set("#Score.TextSpans", Message.raw(String.valueOf(score)));
    }

    public void setLightSourceCount(int lightSourceCount) {
        set("#LightSourceCount.TextSpans", Message.raw(String.valueOf(lightSourceCount)));
    }

    public void setEmptyBlockCount(int emptyBlockCount) {
        set("#EmptyBlockCount.TextSpans", Message.raw(String.valueOf(emptyBlockCount)));
    }

    public void setSolidBlockCount(int solidBlockCount) {
        set("#SolidBlockCount.TextSpans", Message.raw(String.valueOf(solidBlockCount)));
    }

    public void setFurnitureCount(int furnitureCount) {
        set("#FurnitureCount.TextSpans", Message.raw(String.valueOf(furnitureCount)));
    }

    public void setEntranceCount(int entranceCount) {
        set("#EntranceCount.TextSpans", Message.raw(String.valueOf(entranceCount)));
    }

    public void setWindowCount(int windowCount) {
        set("#WindowCount.TextSpans", Message.raw(String.valueOf(windowCount)));
    }
}