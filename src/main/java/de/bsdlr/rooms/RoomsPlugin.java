package de.bsdlr.rooms;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import de.bsdlr.rooms.commands.*;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.lib.asset.quality.Quality;
import de.bsdlr.rooms.lib.asset.score.ScoreGroup;
import de.bsdlr.rooms.lib.exceptions.AssetValidationException;
import de.bsdlr.rooms.lib.room.RoomManager;
import de.bsdlr.rooms.lib.room.RoomSize;
import de.bsdlr.rooms.lib.room.RoomType;
import de.bsdlr.rooms.lib.set.FurnitureSetType;
import de.bsdlr.rooms.lib.asset.AssetMapWithGroup;
import de.bsdlr.rooms.lib.storage.Data;
import de.bsdlr.rooms.lib.storage.DataManager;
import de.bsdlr.rooms.lib.systems.BreakBlockEventSystem;
import de.bsdlr.rooms.lib.systems.PlaceBlockEventSystem;
import de.bsdlr.rooms.ui.HudComponent;
import de.bsdlr.rooms.ui.HudManager;
import de.bsdlr.rooms.utils.PositionUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RoomsPlugin extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static RoomsPlugin instance;

    private final Config<PluginConfig> config;
    private final DataManager<UUID, RoomManager> roomDataManager;

    public static RoomsPlugin get() {
        return instance;
    }

    public RoomsPlugin(JavaPluginInit init) {
        super(init);
        config = this.withConfig("config", PluginConfig.CODEC);
        roomDataManager = new DataManager<>(this.getDataDirectory(), "rooms", RoomManager.CODEC);
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

        // RoomSize Asset Store
        HytaleAssetStore.Builder<String, RoomSize, IndexedLookupTableAssetMap<String, RoomSize>> roomSizeAssetStoreBuilder = HytaleAssetStore.builder(RoomSize.class, new IndexedLookupTableAssetMap<>(RoomSize[]::new));
        roomSizeAssetStoreBuilder.setPath("Rooms/RoomSizes");
        roomSizeAssetStoreBuilder.setCodec(RoomSize.CODEC);
        roomSizeAssetStoreBuilder.setKeyFunction(RoomSize::getId);
        roomSizeAssetStoreBuilder.setReplaceOnRemove(RoomSize::new);
        roomSizeAssetStoreBuilder.preLoadAssets(Collections.singletonList(RoomSize.DEFAULT));
        this.getAssetRegistry().register(roomSizeAssetStoreBuilder.build());

        // ScoreGroups Asset Store
        HytaleAssetStore.Builder<String, ScoreGroup, IndexedLookupTableAssetMap<String, ScoreGroup>> scoreGroupAssetStoreBuilder = HytaleAssetStore.builder(ScoreGroup.class, new IndexedLookupTableAssetMap<>(ScoreGroup[]::new));
        scoreGroupAssetStoreBuilder.setPath("Rooms/ScoreGroups");
        scoreGroupAssetStoreBuilder.setCodec(ScoreGroup.CODEC);
        scoreGroupAssetStoreBuilder.setKeyFunction(ScoreGroup::getId);
        scoreGroupAssetStoreBuilder.setReplaceOnRemove(ScoreGroup::new);
        this.getAssetRegistry().register(scoreGroupAssetStoreBuilder.build());

        // Quality Asset Store
        HytaleAssetStore.Builder<String, Quality, IndexedLookupTableAssetMap<String, Quality>> qualityAssetStoreBuilder = HytaleAssetStore.builder(Quality.class, new IndexedLookupTableAssetMap<>(Quality[]::new));
        qualityAssetStoreBuilder.setPath("Rooms/Qualities");
        qualityAssetStoreBuilder.setCodec(Quality.CODEC);
        qualityAssetStoreBuilder.setKeyFunction(Quality::getId);
        qualityAssetStoreBuilder.setReplaceOnRemove(Quality::new);
        qualityAssetStoreBuilder.preLoadAssets(Collections.singletonList(Quality.DEFAULT_QUALITY));
        this.getAssetRegistry().register(qualityAssetStoreBuilder.build());

        // Furniture Set Type Asset Store
        AssetMapWithGroup<String, FurnitureSetType> furnitureSetTypeAssetMap = new AssetMapWithGroup<>(FurnitureSetType[]::new, FurnitureSetType::getGroup);
        HytaleAssetStore.Builder<String, FurnitureSetType, AssetMapWithGroup<String, FurnitureSetType>> setTypeAssetStoreBuilder = HytaleAssetStore.builder(FurnitureSetType.class, furnitureSetTypeAssetMap);
        setTypeAssetStoreBuilder.setPath("Rooms/FurnitureSets");
        setTypeAssetStoreBuilder.setCodec(FurnitureSetType.CODEC);
        setTypeAssetStoreBuilder.setKeyFunction(FurnitureSetType::getId);
        setTypeAssetStoreBuilder.loadsAfter(Quality.class);
        setTypeAssetStoreBuilder.setReplaceOnRemove(FurnitureSetType::getUnknownFor);
        this.getAssetRegistry().register(setTypeAssetStoreBuilder.build());

        // Builder Type Asset Store
        AssetMapWithGroup<String, RoomType> roomTypeAssetMap = new AssetMapWithGroup<>(RoomType[]::new, RoomType::getGroup);
        HytaleAssetStore.Builder<String, RoomType, AssetMapWithGroup<String, RoomType>> roomTypeAssetStoreBuilder = HytaleAssetStore.builder(RoomType.class, roomTypeAssetMap);
        roomTypeAssetStoreBuilder.setPath("Rooms/Rooms");
        roomTypeAssetStoreBuilder.setCodec(RoomType.CODEC);
        roomTypeAssetStoreBuilder.setKeyFunction(RoomType::getId);
        roomTypeAssetStoreBuilder.loadsAfter(FurnitureSetType.class, Quality.class, RoomSize.class);
        roomTypeAssetStoreBuilder.setReplaceOnRemove(RoomType::getUnknownFor);
        roomTypeAssetStoreBuilder.preLoadAssets(Collections.singletonList(RoomType.DEFAULT));
//        roomTypeAssetStoreBuilder.setPacketGenerator(new RoomTypePacketGenerator());
//        roomTypeAssetStoreBuilder.setNotificationItemFunction(item -> new ItemStack(item, 1).toPacket());
//        roomTypeAssetStoreBuilder.setIdProvider(Item.class);
        this.getAssetRegistry().register(roomTypeAssetStoreBuilder.build());
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log("Plugin starting!");
        List<AssetValidationException> assetValidationExceptionList = RoomSize.validateAllAssets();

        this.getEventRegistry().register(
                PlayerConnectEvent.class,
                event -> {
                    PlayerRef playerRef = event.getPlayerRef();
                    if (playerRef == null || !playerRef.isValid()) return;
                    for (AssetValidationException e : assetValidationExceptionList) {
                        playerRef.sendMessage(Message.raw(e.getMessage()).color(Color.RED));
                    }
                }
        );

        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(HudManager::handle, 300, 300, TimeUnit.MILLISECONDS);
        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            config.save();
            roomDataManager.saveAll();
        }, 5, 5, TimeUnit.MINUTES);

        for (RoomType type : RoomType.getAssetMap().getAssetMap().values()) {
            LOGGER.atInfo().log("%s: %s", type.getId(), Arrays.toString(type.getRoomSizeIds()));
        }
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down RoomsPlugin!");
        config.save();
        roomDataManager.saveAll();

//        StringBuilder builder = new StringBuilder();
//
//        for (String key : BlockType.getAssetMap().getAssetMap().keySet()) {
//            builder.append(key);
//            builder.append("\n");
//        }
//
//        try {
//            Files.writeString(this.getDataDirectory().resolve("blockIds.txt"), builder.toString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public Config<PluginConfig> getConfig() {
        return this.config;
    }

    public DataManager<UUID, RoomManager> getRoomDataManager() {
        return roomDataManager;
    }

    public RoomManager getRoomManagerAndComputeIfAbsent(UUID worldUuid) {
        Data<RoomManager> roomManagerData = roomDataManager.getDataAndComputeIfAbsent(worldUuid);
        if (!roomManagerData.isLoaded()) {
            LOGGER.atInfo().log("First room manager load for world: %s", worldUuid);
            roomManagerData.load();
        }
        return roomManagerData.get();
    }
}
