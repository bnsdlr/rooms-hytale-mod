package de.bsdlr.rooms.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import de.bsdlr.rooms.services.room.RoomEntity;

import javax.annotation.Nonnull;

public class RoomHud extends CustomUIHud {
    public RoomHud(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder ui) {
        ui.append("Pages/RoomHud.ui");
    }

    public void update(@Nonnull RoomEntity room) {
        updateRoomName(room.getFormattedId());
        updateScore(room.getScore());
    }

    private void update(String id, Message message) {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set(id, message);
        update(false, uiCommandBuilder);
    }

    public void updateRoomName(Message roomName) {
        update("#Name.TextSpans", roomName);
    }

    public void updateScore(int score) {
        update("#Score.TextSpans", Message.raw(String.valueOf(score)));
    }

    public void updateDescription(Message description) {
        update("#Score.TextSpans", description);
    }
}