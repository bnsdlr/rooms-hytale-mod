package de.bsdlr.rooms.ui;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.services.room.RoomEntity;
import de.bsdlr.rooms.utils.Utils;

import java.util.Objects;

public class HudManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void handle() {
        if (Universe.get().getPlayerCount() == 0) {
//            LOGGER.atInfo().log("No players.");
            return;
        }

        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            if (!playerRef.isValid()) {
                LOGGER.atWarning().log("PlayerRef is invalid.");
                return;
            }
            if (playerRef.getWorldUuid() == null) {
                LOGGER.atWarning().log("PlayerRef has no world uuid.");
                return;
            }

            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world == null) {
                LOGGER.atWarning().log("World is null.");
                return;
            }

            Vector3d position = playerRef.getTransform().getPosition();
            Vector3i pos = Utils.positionToVector3i(position);
            long key = Utils.encodePosition(pos);
            RoomEntity room = RoomsPlugin.get().getRoomManager().getRoomEntity(key);

            world.execute(() -> {
                if (!playerRef.isValid()) {
                    LOGGER.atWarning().log("[World Thread] PlayerRef is invalid.");
                    return;
                }
                if (playerRef.getReference() == null) {
                    LOGGER.atWarning().log("[World Thread] Player Entity Ref is null.");
                    return;
                }

                Store<EntityStore> store = world.getEntityStore().getStore();
                if (!store.isInThread()) {
                    LOGGER.atWarning().log("[World Thread] Store is not in right thread.");
                    return;
                }

                Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());

                if (player == null) {
                    LOGGER.atSevere().log("[World Thread] Player is null.");
                    return;
                }

                HudComponent hudComponent = store.getComponent(playerRef.getReference(), HudComponent.getComponentType());

                if (hudComponent == null) {
                    store.addComponent(playerRef.getReference(), HudComponent.getComponentType());
                    playerRef.sendMessage(Message.raw("pos: " + pos.x + " " + pos.y + " " + pos.z + "; key: " + key + "; room: " + room.getId()));
                    hudComponent = store.getComponent(playerRef.getReference(), HudComponent.getComponentType());
                    if (hudComponent == null) {
                        LOGGER.atWarning().log("Showing hud component still null.");
                        return;
                    }
                }

                if (!Objects.equals(hudComponent.getRoomEntity(), room)) {
                    playerRef.sendMessage(Message.raw("rooms are not the same."));
                    LOGGER.atInfo().log("Updating room hud.");
                    hudComponent.setRoomEntity(room);
                    if (room == null) {
                        LOGGER.atInfo().log("Clearing room hud.");
//                        player.getHudManager().resetHud(playerRef);
                        player.getHudManager().setCustomHud(playerRef, new EmptyHud(playerRef));
                        LOGGER.atInfo().log("Cleared room hud.");
//                        playerRef.sendMessage(Message.raw("reset room hud"));
//                        playerRef.sendMessage(Message.raw("pos: " + pos.x + " " + pos.y + " " + pos.z + "; key: " + key + "; room: null"));
                    } else {
                        if (player.getHudManager().getCustomHud() instanceof RoomHud hud) {
                            LOGGER.atInfo().log("Updating room hud values.");
                            hud.update(room);
                        } else {
                            LOGGER.atInfo().log("Creating new room hud.");
                            player.getHudManager().setCustomHud(playerRef, new RoomHud(playerRef));
                            LOGGER.atInfo().log("added new room hud to custom hud.");

                            if (player.getHudManager().getCustomHud() instanceof RoomHud hud) {
                                hud.update(room);
                                LOGGER.atInfo().log("updated room hud");
                            } else {
                                LOGGER.atInfo().log("custom hud is not room hud");
                            }
                        }
                    }
                }
            });
        }
    }
}
