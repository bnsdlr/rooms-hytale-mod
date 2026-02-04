package de.bsdlr.rooms;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import de.bsdlr.rooms.commands.*;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.lib.asset.quality.Quality;
import de.bsdlr.rooms.lib.room.RoomManager;
import de.bsdlr.rooms.lib.room.RoomType;
import de.bsdlr.rooms.lib.set.FurnitureSetType;
import de.bsdlr.rooms.lib.asset.AssetMapWithGroup;
import de.bsdlr.rooms.lib.storage.Data;
import de.bsdlr.rooms.lib.systems.BreakBlockEventSystem;
import de.bsdlr.rooms.lib.systems.PlaceBlockEventSystem;
import de.bsdlr.rooms.ui.HudComponent;
import de.bsdlr.rooms.ui.HudManager;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RoomsPlugin extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static RoomsPlugin instance;

    private final Config<PluginConfig> config;
    private final Data<RoomManager> roomManager;

    public static RoomsPlugin get() {
        return instance;
    }

    public RoomsPlugin(JavaPluginInit init) {
        super(init);
        config = this.withConfig("config", PluginConfig.CODEC);
        roomManager = new Data<>(this.getDataDirectory(), "rooms", RoomManager.CODEC);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("=========================================================");
        LOGGER.atInfo().log("             Setting up RoomsPlugin!");
        LOGGER.atInfo().log("=========================================================");

//        Vector3d p1 = new Vector3d(123.123, 100.5, 193.6);
//        long key = PositionUtils.encodePosition(p1);
//        Vector3d r1 = new Vector3d(PositionUtils.decodeX(key), PositionUtils.decodeY(key), PositionUtils.decodeZ(key));
//        LOGGER.atInfo().log("input : %.1f %.1f %.1f", p1.x, p1.y, p1.z);
//        LOGGER.atInfo().log("output: %.1f %.1f %.1f", r1.x, r1.y, r1.z);
//
//        Vector3d p2 = new Vector3d(-123.123, 100.5, -193.6);
//        long key2 = PositionUtils.encodePosition(p2);
//        Vector3d r2 = new Vector3d(PositionUtils.decodeX(key2), PositionUtils.decodeY(key2), PositionUtils.decodeZ(key2));
//        LOGGER.atInfo().log("input : %.1f %.1f %.1f", p2.x, p2.y, p2.z);
//        LOGGER.atInfo().log("output: %.1f %.1f %.1f", r2.x, r2.y, r2.z);

        instance = this;
        config.get().validate();
        config.save();

        roomManager.load();
        roomManager.get();
        roomManager.save();

        this.getCommandRegistry().registerCommand(new RoomsCommand());
        this.getCommandRegistry().registerCommand(new DetectCommand());
        this.getCommandRegistry().registerCommand(new RoomsConfigCommand());
        this.getCommandRegistry().registerCommand(new BlockInfoCommand());
        this.getCommandRegistry().registerCommand(new RotateCommand());
        this.getCommandRegistry().registerCommand(new TestUICommand());
        this.getCommandRegistry().registerCommand(new SetCommand());

        this.getEntityStoreRegistry().registerSystem(new BreakBlockEventSystem());
        this.getEntityStoreRegistry().registerSystem(new PlaceBlockEventSystem());

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

        // Builder Type Asset Store
        AssetMapWithGroup<String, RoomType> roomTypeAssetMap = new AssetMapWithGroup<>(RoomType[]::new, RoomType::getGroup);
        HytaleAssetStore.Builder<String, RoomType, AssetMapWithGroup<String, RoomType>> roomTypeAssetStoreBuilder = HytaleAssetStore.builder(RoomType.class, roomTypeAssetMap);
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
        AssetMapWithGroup<String, FurnitureSetType> furnitureSetTypeAssetMap = new AssetMapWithGroup<>(FurnitureSetType[]::new, FurnitureSetType::getGroup);
        HytaleAssetStore.Builder<String, FurnitureSetType, AssetMapWithGroup<String, FurnitureSetType>> setTypeAssetStoreBuilder = HytaleAssetStore.builder(FurnitureSetType.class, furnitureSetTypeAssetMap);
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
        roomManager.save();
    }

    public Config<PluginConfig> getConfig() {
        return this.config;
    }

    public RoomManager getRoomManager() {
        return roomManager.get();
    }
}
