package de.bsdlr.rooms;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import de.bsdlr.rooms.commands.*;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.services.quality.Quality;
import de.bsdlr.rooms.services.room.RoomManager;
import de.bsdlr.rooms.services.room.RoomType;
import de.bsdlr.rooms.services.room.RoomTypeAssetMap;
import de.bsdlr.rooms.services.set.FurnitureSetType;
import de.bsdlr.rooms.services.set.FurnitureSetTypeAssetMap;
import de.bsdlr.rooms.storage.PlayerStorageManager;
import de.bsdlr.rooms.ui.HudComponent;
import de.bsdlr.rooms.ui.HudManager;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RoomsPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static RoomsPlugin instance;

    private final Config<PluginConfig> config;
    private final RoomManager roomManager;
    private final PlayerStorageManager playerStorageManager;

    public static RoomsPlugin get() {
        return instance;
    }

    public RoomsPlugin(JavaPluginInit init) {
        super(init);
        config = this.withConfig("config", PluginConfig.CODEC);
        roomManager = new RoomManager();
        playerStorageManager = new PlayerStorageManager();
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("=========================================================");
        LOGGER.atInfo().log("             Setting up RoomsPlugin!");
        LOGGER.atInfo().log("=========================================================");

        instance = this;
        config.get().validate();
        config.save();

        this.getCommandRegistry().registerCommand(new RoomsCommand());
        this.getCommandRegistry().registerCommand(new BlockInfoCommand());
        this.getCommandRegistry().registerCommand(new RotateCommand());
        this.getCommandRegistry().registerCommand(new TestUICommand());
        this.getCommandRegistry().registerCommand(new SetCommand());

        ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
        HudComponent.setComponentType(entityStoreRegistry.registerComponent(HudComponent.class, "Rooms_Hud", HudComponent.CODEC));

//        this.getEventRegistry().register(
//                EventPriority.EARLY,
//                PlayerDisconnectEvent.class,
//                event -> {
//                    if (event.getPlayerRef().getReference() != null) {
//                        if (event.getPlayerRef().getWorldUuid() == null) {
//                            LOGGER.atInfo().log("players world uuid is null");
//                            return;
//                        }
//
//                        World world = Universe.get().getWorld(event.getPlayerRef().getWorldUuid());
//                        if (world == null) {
//                            LOGGER.atInfo().log("world is null.");
//                            return;
//                        }
//
//                        world.execute(() -> {
//                            LOGGER.atInfo().log("removing hud component");
//                            Store<EntityStore> store = event.getPlayerRef().getReference().getStore();
//                            store.removeComponentIfExists(event.getPlayerRef().getReference(), HudComponent.getComponentType());
//                            LOGGER.atInfo().log("removed hud component");
//                        });
//                    }
//                }
//        );

        // Room Type Asset Store
        RoomTypeAssetMap<String, RoomType> roomTypeAssetMap = new RoomTypeAssetMap<>(RoomType[]::new, RoomType::getGroup);
        HytaleAssetStore.Builder<String, RoomType, RoomTypeAssetMap<String, RoomType>> roomTypeAssetStoreBuilder = HytaleAssetStore.builder(RoomType.class, roomTypeAssetMap);
        roomTypeAssetStoreBuilder.setPath("Rooms/Rooms");
        roomTypeAssetStoreBuilder.setCodec(RoomType.CODEC);
        roomTypeAssetStoreBuilder.setKeyFunction(RoomType::getId);
        roomTypeAssetStoreBuilder.loadsAfter(BlockType.class);
        roomTypeAssetStoreBuilder.setReplaceOnRemove(RoomType::getUnknownFor);
        roomTypeAssetStoreBuilder.preLoadAssets(Collections.singletonList(RoomType.DEFAULT));
//        roomTypeAssetStoreBuilder.setPacketGenerator(new RoomTypePacketGenerator());
//        roomTypeAssetStoreBuilder.setNotificationItemFunction(item -> new ItemStack(item, 1).toPacket());
//        roomTypeAssetStoreBuilder.setIdProvider(Item.class);
        this.getAssetRegistry().register(roomTypeAssetStoreBuilder.build());

        // Furniture Set Type Asset Store
        FurnitureSetTypeAssetMap<String, FurnitureSetType> furnitureSetTypeAssetMap = new FurnitureSetTypeAssetMap<>(FurnitureSetType[]::new, FurnitureSetType::getGroup);
        HytaleAssetStore.Builder<String, FurnitureSetType, FurnitureSetTypeAssetMap<String, FurnitureSetType>> setTypeAssetStoreBuilder = HytaleAssetStore.builder(FurnitureSetType.class, furnitureSetTypeAssetMap);
        setTypeAssetStoreBuilder.setPath("Rooms/FurnitureSets");
        setTypeAssetStoreBuilder.setCodec(FurnitureSetType.CODEC);
        setTypeAssetStoreBuilder.setKeyFunction(FurnitureSetType::getId);
        setTypeAssetStoreBuilder.loadsAfter(BlockType.class);
        setTypeAssetStoreBuilder.setReplaceOnRemove(FurnitureSetType::getUnknownFor);
        this.getAssetRegistry().register(setTypeAssetStoreBuilder.build());

        // Quality Asset Store
        HytaleAssetStore.Builder<String, Quality, IndexedLookupTableAssetMap<String, Quality>> qualityAssetStoreBuilder = HytaleAssetStore.builder(Quality.class, new IndexedLookupTableAssetMap<>(Quality[]::new));
        qualityAssetStoreBuilder.setPath("Rooms/Qualities");
        qualityAssetStoreBuilder.setCodec(Quality.CODEC);
        qualityAssetStoreBuilder.setKeyFunction(Quality::getId);
        qualityAssetStoreBuilder.loadsAfter(ParticleSystem.class);
        qualityAssetStoreBuilder.setReplaceOnRemove(Quality::new);
        qualityAssetStoreBuilder.preLoadAssets(Collections.singletonList(Quality.DEFAULT_QUALITY));
        this.getAssetRegistry().register(qualityAssetStoreBuilder.build());
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log("Plugin starting!");

        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(HudManager::handle, 300, 300, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down RoomsPlugin!");

        config.save();
    }

    public Config<PluginConfig> getConfig() {
        return this.config;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }
}
