package de.bsdlr.rooms.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;

public class PluginConfig {
    public static final BuilderCodec<PluginConfig> CODEC = BuilderCodec.builder(PluginConfig.class, PluginConfig::new)
            .append(new KeyedCodec<>("ScanRadius", Vector3i.CODEC),
                    PluginConfig::setScanRadius,
                    PluginConfig::getScanRadius)
            .addValidator(new ScanRadiusValidator())
            .add()
            .append(new KeyedCodec<>("Rooms", RoomsConfig.CODEC),
                    PluginConfig::setRoomsConfig,
                    PluginConfig::getRoomsConfig)
            .addValidator(new RoomsConfigValidator())
            .add()
            .build();
    public static final CommonAssetValidator ICON_VALIDATOR = new CommonAssetValidator("png", "Icons/Rooms", "Icons/ItemCategories");
    private Vector3i scanRadius = new Vector3i(3, 3, 3);
    private RoomsConfig roomsConfig = new RoomsConfig();

    public void validate() {
        roomsConfig.validate();
        if (scanRadius.x <= 0) scanRadius.setX(1);
        if (scanRadius.y <= 0) scanRadius.setY(1);
        if (scanRadius.z <= 0) scanRadius.setZ(1);
    }

    public Vector3i getScanRadius() {
        return scanRadius;
    }

    public void setScanRadius(Vector3i scanRadius) {
        if (scanRadius.x < 0) scanRadius.setX(1);
        if (scanRadius.y < 0) scanRadius.setY(1);
        if (scanRadius.z < 0) scanRadius.setZ(1);
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

    public Vector3i getBoundScanRadius() {
        return this.roomsConfig.getBoundScanRadius();
    }

    public void setBoundScanRadius(Vector3i boundScanRadius) {
        this.roomsConfig.setBoundScanRadius(boundScanRadius);
    }
}
