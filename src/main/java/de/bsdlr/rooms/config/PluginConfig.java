package de.bsdlr.rooms.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;

public class PluginConfig {
    public static final BuilderCodec<PluginConfig> CODEC = BuilderCodec.builder(PluginConfig.class, PluginConfig::new)
            .append(new KeyedCodec<>("ScanRadius", Codec.INTEGER),
                    PluginConfig::setScanRadius,
                    PluginConfig::getScanRadius).add()
            .append(new KeyedCodec<>("Rooms", RoomsConfig.CODEC),
                    PluginConfig::setRoomsConfig,
                    PluginConfig::getRoomsConfig).add()
            .build();
    public static final CommonAssetValidator ICON_VALIDATOR = new CommonAssetValidator("png", "Icons/Rooms", "Icons/ItemCategories");
    private int scanRadius = 100;
    private RoomsConfig roomsConfig = new RoomsConfig();

    public void validate() {
        roomsConfig.validate();
        if (scanRadius < 0) scanRadius = 1;
    }

    public int getScanRadius() {
        return scanRadius;
    }

    public void setScanRadius(int scanRadius) {
        if (scanRadius < 0) return;
        this.scanRadius = scanRadius;
    }

    public RoomsConfig getRoomsConfig() {
        return roomsConfig;
    }

    public void setRoomsConfig(RoomsConfig config) {
        this.roomsConfig = config;
    }

    public int getMinRoomHeight() {
        return this.roomsConfig.getMinRoomHeight();
    }

    public void setMinRoomHeight(int minWallBlockHeight) {
        this.roomsConfig.setMinRoomHeight(minWallBlockHeight);
    }

    public int getMaxRoomHeight() {
        return this.roomsConfig.getMaxRoomHeight();
    }

    public void setMaxRoomHeight(int maxRoomHeight) {
        this.roomsConfig.setMaxRoomHeight(maxRoomHeight);
    }
}
