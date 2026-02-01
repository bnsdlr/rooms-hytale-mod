package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.services.room.Room;
import de.bsdlr.rooms.services.room.RoomDetector;
import de.bsdlr.rooms.services.room.RoomEntity;
import de.bsdlr.rooms.services.room.RoomManager;
import de.bsdlr.rooms.ui.RoomHud;

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

            RoomManager roomManager = RoomsPlugin.get().getRoomManager();
            roomManager.addRoomEntity(new RoomEntity(room));

            ctx.sendMessage(Message.raw("room block count: " + room.getAllBlocks().size()));
            ctx.sendMessage(Message.raw("light source: " + room.getLightSources().size()));
            ctx.sendMessage(Message.raw("empty block count: " + room.getEmpty().size()));
            ctx.sendMessage(Message.raw("solid block count: " + room.getSolidBlocks().size()));
            ctx.sendMessage(Message.raw("furniture count: " + room.getFurnitures().size()));
            ctx.sendMessage(Message.raw("entrance count: " + room.getEntrances().size()));
            ctx.sendMessage(Message.raw("window count: " + room.getWindows().size()));

            if (ctx.sender() instanceof Player) {
                Player player = ctx.senderAs(Player.class);

                if (player.getHudManager().getCustomHud() == null) {
                    player.getHudManager().setCustomHud(playerRef, new RoomHud(playerRef));
                    ctx.sendMessage(Message.raw("Room ui visible"));
                }
                if (player.getHudManager().getCustomHud() instanceof RoomHud roomHud) {
                    ctx.sendMessage(Message.raw("Setting room ui values..."));
//                    roomHud.updateRoomName("Some room name");
//                    roomHud.updateScore(room.getAllBlocks().size());
                } else {
                    ctx.sendMessage(Message.raw("there is no room ui..."));
                }
            }
        } catch (FailedToDetectRoomException e) {
            ctx.sendMessage(Message.raw(e.getMessage()));
            LOGGER.atWarning().log(e.getMessage());
        } catch (Exception e) {
            ctx.sendMessage(Message.raw(e.getMessage()));
            LOGGER.atSevere().withCause(e).log();
        }

//        IntSet intSet = chunk.blocks();
//
//        if (intSet == null) {
//            LOGGER.atInfo().log("IntSet is null");
//            return;
//        }
//
//        for (int furniture : chunk.blocks()) {
//            LOGGER.atInfo().log("furniture: " + furniture);
//        }
    }
}