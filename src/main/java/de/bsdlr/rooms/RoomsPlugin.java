package de.bsdlr.rooms;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import de.bsdlr.rooms.commands.*;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.room.RoomType;
import de.bsdlr.rooms.room.RoomTypeAssetMap;
import de.bsdlr.rooms.set.SetType;
import de.bsdlr.rooms.set.SetTypeAssetMap;

public class RoomsPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static RoomsPlugin instance;

    private final Config<PluginConfig> config;

    public static RoomsPlugin getInstance() {
        return instance;
    }

    public RoomsPlugin(JavaPluginInit init) {
        super(init);
        config = this.withConfig("config", PluginConfig.CODEC);
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
        this.getCommandRegistry().registerCommand(new TestCommand());

        RoomTypeAssetMap<String, RoomType> roomTypeAssetMap = new RoomTypeAssetMap<>(RoomType[]::new, RoomType::getGroup);
        HytaleAssetStore.Builder<String, RoomType, RoomTypeAssetMap<String, RoomType>> roomTypeAssetStoreBuilder = HytaleAssetStore.builder(RoomType.class, roomTypeAssetMap);
        roomTypeAssetStoreBuilder.setPath("Rooms/Rooms");
        roomTypeAssetStoreBuilder.setCodec(RoomType.CODEC);
        roomTypeAssetStoreBuilder.setKeyFunction(RoomType::getId);
        roomTypeAssetStoreBuilder.loadsAfter(BlockType.class);
        roomTypeAssetStoreBuilder.setReplaceOnRemove(RoomType::getUnknownFor);
//        roomTypeAssetStoreBuilder.setPacketGenerator(new RoomTypePacketGenerator());
//        roomTypeAssetStoreBuilder.setNotificationItemFunction(item -> new ItemStack(item, 1).toPacket());
//        roomTypeAssetStoreBuilder.preLoadAssets(Arrays.asList(RoomType.DEFAULT));
//        roomTypeAssetStoreBuilder.setIdProvider(Item.class);
        this.getAssetRegistry().register(roomTypeAssetStoreBuilder.build());

        SetTypeAssetMap<String, SetType> setTypeAssetMap = new SetTypeAssetMap<>(SetType[]::new, SetType::getGroup);
        HytaleAssetStore.Builder<String, SetType, SetTypeAssetMap<String, SetType>> setTypeAssetStoreBuilder = HytaleAssetStore.builder(SetType.class, setTypeAssetMap);
        setTypeAssetStoreBuilder.setPath("Rooms/Sets");
        setTypeAssetStoreBuilder.setCodec(SetType.CODEC);
        setTypeAssetStoreBuilder.setKeyFunction(SetType::getId);
        setTypeAssetStoreBuilder.loadsAfter(BlockType.class);
        setTypeAssetStoreBuilder.setReplaceOnRemove(SetType::getUnknownFor);
        this.getAssetRegistry().register(setTypeAssetStoreBuilder.build());
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log("Plugin starting!");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down RoomsPlugin!");

        config.save();
    }

    public Config<PluginConfig> getConfig() {
        return this.config;
    }
}
