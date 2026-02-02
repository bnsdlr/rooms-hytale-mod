package de.bsdlr.rooms.ui;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import de.bsdlr.rooms.lib.room.Room;
import de.bsdlr.rooms.lib.room.RoomTranslationProperties;
import de.bsdlr.rooms.lib.room.RoomType;

import javax.annotation.Nonnull;

public class RoomHud extends CustomUIHud {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String NAME_ID = "#Name.TextSpans";
    private static final String SCORE_ID = "#Score.TextSpans";
    private static final String DESCRIPTION_ID = "#Description.TextSpans";

    public RoomHud(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder ui) {
        ui.append("Pages/RoomHud.ui");
    }

    public void update(@Nonnull Room room) {
        RoomType type = room.getType();
        RoomTranslationProperties translationProperties = type.getTranslationProperties();

        String name = translationProperties != null && translationProperties.getName() != null
                ? translationProperties.getName()
                : type.getId().replace('_', ' ');

        String description = translationProperties != null && translationProperties.getDescription() != null
                ? translationProperties.getDescription() : null;

        LOGGER.atInfo().log("name: %s", name);
        LOGGER.atInfo().log("score: %d", room.getScore());
        LOGGER.atInfo().log("description: %s", description);
        updateName(Message.raw(name).color(type.getColorOrFallback().toString()));
        updateScore(room.getScore());
        updateDescription(description);
    }

    private void update(String id, Message message) {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set(id, message);
        update(false, uiCommandBuilder);
    }

    public void updateName(Message roomName) {
        update(NAME_ID, roomName);
    }

    public void updateScore(int score) {
        update(SCORE_ID, Message.raw(String.valueOf(score)));
    }

    public void updateDescription(String description) {
        if (description == null) {
            update(DESCRIPTION_ID, Message.empty());
        } else {
            update(DESCRIPTION_ID, Message.raw(description));
        }
    }
}