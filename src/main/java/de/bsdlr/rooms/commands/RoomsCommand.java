package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.lib.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.lib.room.RoomDetector;
import de.bsdlr.rooms.lib.room.Room;
import de.bsdlr.rooms.lib.room.RoomManager;

import javax.annotation.Nonnull;

public class RoomsCommand extends AbstractPlayerCommand {
    public RoomsCommand() {
        super("rooms", "...");
        this.addAliases("room");
        this.requirePermission("rooms.command");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Vector3d position = playerRef.getTransform().getPosition();
        int posX = (position.x < 0) ? (int) Math.floor(position.x) : (int) position.x;
        int posY = (int) position.y;
        int posZ = (int) position.z;

        ctx.sendMessage(Message.raw("at pos: " + posX + " " + posY + " " + posZ));

        try {
            Room room = RoomDetector.getRoomAt(world, posX, posY, posZ);
            if (room == null) {
                ctx.sendMessage(Message.raw("got NO room"));
                return;
            }

            RoomManager roomManager = RoomsPlugin.get().getRoomManagerAndComputeIfAbsent(world.getWorldConfig().getUuid());
            roomManager.addRoom(room);

            ctx.sendMessage(Message.raw("room block count: " + room.getBlocks().size()));
        } catch (FailedToDetectRoomException e) {
            ctx.sendMessage(Message.raw(e.getMessage()));
            LOGGER.atWarning().log(e.getMessage());
        } catch (Exception e) {
            ctx.sendMessage(Message.raw(e.getMessage()));
            LOGGER.atSevere().withCause(e).log();
        }
    }
}