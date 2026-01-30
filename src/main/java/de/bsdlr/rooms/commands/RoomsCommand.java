package de.bsdlr.rooms.commands;

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
import de.bsdlr.rooms.detector.RoomDetector;
import de.bsdlr.rooms.exceptions.FailedToDetectRoomException;
import de.bsdlr.rooms.room.Room;
import de.bsdlr.rooms.ui.RoomUI;

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
                    player.getHudManager().setCustomHud(playerRef, new RoomUI(playerRef));
                    ctx.sendMessage(Message.raw("Room ui visible"));
                }
                if (player.getHudManager().getCustomHud() instanceof RoomUI roomUI) {
                    ctx.sendMessage(Message.raw("Setting room ui values..."));
                    roomUI.setRoomName("Some room name");
                    roomUI.setScore(room.getAllBlocks().size());
                    roomUI.setEmptyBlockCount(room.getEmpty().size());
                    roomUI.setEntranceCount(room.getEntrances().size());
                    roomUI.setFurnitureCount(room.getFurnitures().size());
                    roomUI.setLightSourceCount(room.getLightSources().size());
                    roomUI.setSolidBlockCount(room.getSolidBlocks().size());
                    roomUI.setWindowCount(room.getWindows().size());
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