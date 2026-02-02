package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.lib.room.Room;
import de.bsdlr.rooms.lib.room.RoomDetector;
import de.bsdlr.rooms.lib.room.RoomManager;
import de.bsdlr.rooms.utils.PositionUtils;

import javax.annotation.Nonnull;
import java.util.Set;

public class DetectCommand extends AbstractPlayerCommand {
    public DetectCommand() {
        super("detect", "...");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Vector3d position = playerRef.getTransform().getPosition();
        Vector3i pos = PositionUtils.positionToVector3i(position);

        try {
            Set<Room> rooms = RoomDetector.detectRooms(world, pos.x, pos.y, pos.z);
            if (rooms.isEmpty()) {
                ctx.sendMessage(Message.raw("No rooms detected."));
                return;
            }
            RoomManager roomManager = RoomsPlugin.get().getRoomManager();

            for (Room room : rooms) {
                roomManager.addRoom(room);
                ctx.sendMessage(Message.raw("add room: " + room.getId()));
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log(e.getMessage());
        }
    }
}
