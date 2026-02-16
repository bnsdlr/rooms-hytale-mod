package de.bsdlr.rooms.ui;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import de.bsdlr.rooms.lib.room.Room;
import de.bsdlr.rooms.lib.room.RoomSize;
import de.bsdlr.rooms.lib.room.RoomTranslationProperties;
import de.bsdlr.rooms.lib.room.RoomType;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class RoomHud extends CustomUIHud {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String CONTAINER_ID = "#Container";
    private static final String NAME_ID = "#Name";
    private static final String SCORE_ID = "#Score";
    private static final String DESCRIPTION_ID = "#Description";
    private static final String TEXT_SPANS = ".TextSpans";

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

        StringBuilder nameBuilder = new StringBuilder();

        getPlayerRef().sendMessage(Message.raw(Arrays.toString(type.getRoomSizeIds())));
        getPlayerRef().sendMessage(Message.raw("room area is: " + room.getArea()));
        RoomSize size = RoomSize.getRoomSizeFromArea(type.getRoomSizeIds(), room.getArea());

        LOGGER.atInfo().log("room size prefix: %s; for id: %s", size.getPrefix(), size.getId());
        if (size.getPrefix() != null && !size.getPrefix().isBlank()) {
            nameBuilder.append(size.getPrefix());
            nameBuilder.append(" ");
        }

        nameBuilder.append(translationProperties != null && translationProperties.getName() != null
                ? translationProperties.getName()
                : type.getId().replace('_', ' '));

        String name = nameBuilder.toString();

        String description = translationProperties != null && translationProperties.getDescription() != null
                ? translationProperties.getDescription() : null;

        LOGGER.atInfo().log("name: %s", name);
        LOGGER.atInfo().log("score: %d", room.getScore());
        LOGGER.atInfo().log("description: %s", description);
        updateName(Message.raw(name).color(type.getColorOrFallback().toString()));
        updateScore(room.getScore());
        updateDescription(description == null ? String.format("area: %d\nall blocks: %d",
                room.getArea(),
                room.getBlockMap().values().stream().reduce(Integer::sum).get()) : description);
//        update(SCORE_ID + TEXT_SPANS, Message.raw(room.getScore() + " (" + room.getArea() + ")"));
    }

    private void update(String id, Message message) {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set(id, message);
        update(false, uiCommandBuilder);
    }

    private void updateName(Message roomName) {
        update(NAME_ID + TEXT_SPANS, roomName);
    }

    private void updateScore(int score) {
        update(SCORE_ID + TEXT_SPANS, Message.raw(String.valueOf(score)));
    }

    private void updateDescription(String description) {
        if (description == null) {
            update(DESCRIPTION_ID + TEXT_SPANS, Message.empty());
        } else {
            update(DESCRIPTION_ID + TEXT_SPANS, Message.raw(description));
        }
    }
}