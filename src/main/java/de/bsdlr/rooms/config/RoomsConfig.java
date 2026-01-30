package de.bsdlr.rooms.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class RoomsConfig {
    public static final BuilderCodec<RoomsConfig> CODEC = BuilderCodec.builder(RoomsConfig.class, RoomsConfig::new)
            .append(new KeyedCodec<>("MinWallBlockHeight", Codec.INTEGER),
                    RoomsConfig::setMinRoomHeight,
                    RoomsConfig::getMinRoomHeight).add()
            .append(new KeyedCodec<>("MaxRoomHeight", Codec.INTEGER),
                    RoomsConfig::setMaxRoomHeight,
                    RoomsConfig::getMaxRoomHeight).add()
            .build();
    private int minRoomHeight = 2;
    private int maxRoomHeight = 100;

    public void validate() {
        if (minRoomHeight < 1) minRoomHeight = 1;
        if (maxRoomHeight < 1) maxRoomHeight = 1;
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
}
