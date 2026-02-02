package de.bsdlr.rooms.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;

public class RoomsConfig {
    public static final BuilderCodec<RoomsConfig> CODEC = BuilderCodec.builder(RoomsConfig.class, RoomsConfig::new)
            .append(new KeyedCodec<>("MinWallBlockHeight", Codec.INTEGER),
                    RoomsConfig::setMinRoomHeight,
                    RoomsConfig::getMinRoomHeight).add()
            .append(new KeyedCodec<>("MaxRoomHeight", Codec.INTEGER),
                    RoomsConfig::setMaxRoomHeight,
                    RoomsConfig::getMaxRoomHeight).add()
            .append(new KeyedCodec<>("BoundScanRadius", Vector3i.CODEC),
                    RoomsConfig::setBoundScanRadius,
                    RoomsConfig::getBoundScanRadius).add()
            .build();
    private int minRoomHeight = 2;
    private int maxRoomHeight = 100;
    private Vector3i boundScanRadius = new Vector3i(100, maxRoomHeight / 2, 100);

    public void validate() {
        if (minRoomHeight < 2) minRoomHeight = 2;
        if (maxRoomHeight < minRoomHeight) maxRoomHeight = minRoomHeight;
        if (boundScanRadius.x <= 0) boundScanRadius.setX(1);
        if (boundScanRadius.y <= 0) boundScanRadius.setY(1);
        if (boundScanRadius.z <= 0) boundScanRadius.setZ(1);
    }

    public int getMinRoomHeight() {
        return minRoomHeight;
    }

    public void setMinRoomHeight(int minRoomHeight) {
        this.minRoomHeight = minRoomHeight;
    }

    public int getMaxRoomHeight() {
        return maxRoomHeight;
    }

    public void setMaxRoomHeight(int maxRoomHeight) {
        this.maxRoomHeight = maxRoomHeight;
    }

    public Vector3i getBoundScanRadius() {
        return boundScanRadius;
    }

    public void setBoundScanRadius(Vector3i boundScanRadius) {
        if (boundScanRadius.x <= 0) boundScanRadius.setX(1);
        if (boundScanRadius.y <= 0) boundScanRadius.setY(1);
        if (boundScanRadius.z <= 0) boundScanRadius.setZ(1);
        this.boundScanRadius = boundScanRadius;
    }
}
