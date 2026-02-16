package de.bsdlr.rooms.lib.room;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import de.bsdlr.rooms.lib.exceptions.AssetValidationException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoomSize implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, RoomSize>> {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final AssetBuilderCodec<String, RoomSize> CODEC = AssetBuilderCodec.builder(
                    RoomSize.class, RoomSize::new, Codec.STRING, (rs, s) -> rs.id = s, (rs -> rs.id), (rs, d) -> rs.data = d, (rs -> rs.data)
            )
            .appendInherited(new KeyedCodec<>("MinArea", Codec.INTEGER),
                    ((roomSize, s) -> roomSize.minArea = s),
                    (roomSize -> roomSize.minArea),
                    ((roomSize, parent) -> roomSize.minArea = parent.minArea))
            .addValidator(Validators.min(1))
            .add()
//            .appendInherited(new KeyedCodec<>("MaxArea", Codec.INTEGER),
//                    ((roomSize, s) -> roomSize.maxArea = s),
//                    (roomSize -> roomSize.maxArea),
//                    ((roomSize, parent) -> roomSize.maxArea = parent.maxArea))
//            .addValidator(Validators.min(1))
//            .add()
            .appendInherited(new KeyedCodec<>("ExtraScore", Codec.INTEGER),
                    ((roomSize, s) -> roomSize.extraScore = s),
                    (roomSize -> roomSize.extraScore),
                    ((roomSize, parent) -> roomSize.extraScore = parent.extraScore))
            .addValidator(Validators.min(0))
            .add()
            .appendInherited(new KeyedCodec<>("Prefix", Codec.STRING),
                    ((roomSize, s) -> roomSize.prefix = s),
                    (roomSize -> roomSize.prefix),
                    ((roomSize, parent) -> roomSize.prefix = parent.prefix))
            .add()
            .validator(((roomSize, results) -> {
                for (RoomSize size : RoomSize.getAssetMap().getAssetMap().values()) {
                    if (Objects.equals(size.id, RoomSize.DEFAULT_KEY) || size.id.equals(roomSize.id)) continue;
                    if (roomSize.minArea == size.minArea) {
                        results.fail("There should not be duplicate min areas for RoomSizes (" + size.id + " has the same min area).");
                    }
                }
            }))
            .build();
    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(RoomSize::getAssetStore));
    private static AssetStore<String, RoomSize, IndexedLookupTableAssetMap<String, RoomSize>> ASSET_STORE;
    public static final String DEFAULT_KEY = "Default";
    public static final RoomSize DEFAULT = new RoomSize(DEFAULT_KEY);
    protected String id;
    protected AssetExtraInfo.Data data;
    protected int minArea = 1;
    protected int extraScore = 0;
    protected String prefix;

    public static AssetStore<String, RoomSize, IndexedLookupTableAssetMap<String, RoomSize>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(RoomSize.class);
        }

        return ASSET_STORE;
    }

    public static IndexedLookupTableAssetMap<String, RoomSize> getAssetMap() {
        return (IndexedLookupTableAssetMap<String, RoomSize>) getAssetStore().getAssetMap();
    }

    public RoomSize() {
    }

    public RoomSize(String id) {
        this.id = id;
    }

    public RoomSize(@Nonnull RoomSize o) {
        this.id = o.id;
        this.data = o.data;
        this.minArea = o.minArea;
        this.extraScore = o.extraScore;
        this.prefix = o.prefix;
    }

    @Override
    public String getId() {
        return id;
    }

    public AssetExtraInfo.Data getData() {
        return data;
    }

    public int getMinArea() {
        return minArea;
    }

    public int getExtraScore() {
        return extraScore;
    }

    public String getPrefix() {
        return prefix;
    }

    public static List<AssetValidationException> validateAllAssets() throws AssetValidationException {
        LOGGER.atInfo().log("Validating RoomSize assets!");
        List<AssetValidationException> exceptions = new ArrayList<>();

        for (String id : getAssetMap().getAssetMap().keySet()) {
            try {
                validateAsset(id);
            } catch (AssetValidationException e) {
                exceptions.add(e);
            }
        }

        return exceptions;
    }

    public static void validateAsset(String id) {
        RoomSize size = getAssetMap().getAsset(id);
        if (size == null) {
            LOGGER.atInfo().log("There is no RoomSize asset with the id: %s", id);
            return;
        }

        List<String> ids = new ArrayList<>();

        for (RoomSize roomSize : getAssetMap().getAssetMap().values()) {
            if (size.minArea == roomSize.minArea) {
                ids.add(roomSize.id);
            }
        }

        ids.remove(DEFAULT_KEY);

        if (ids.size() > 1) {
            throw new AssetValidationException("RoomSize asset is invalid, duplicate requirements for RoomSize assets: " + ids);
        }
    }

    @Nonnull
    public static RoomSize getRoomSizeFromArea(int area) {
        return getRoomSizeFromArea(null, area);
    }

    @Nonnull
    public static RoomSize getRoomSizeFromArea(String[] roomSizeIds, int area) {
        RoomSize currentSize = null;
        if (roomSizeIds == null || roomSizeIds.length == 0) {
            for (Map.Entry<String, RoomSize> entry : getAssetMap().getAssetMap().entrySet()) {
                RoomSize size = entry.getValue();
                if (size.minArea <= area && (currentSize == null || size.minArea > currentSize.minArea)) {
                    currentSize = size;
                }
            }
        } else {
            for (String id : roomSizeIds) {
                RoomSize size = getAssetMap().getAsset(id);
                if (size == null) continue;
                if (size.minArea <= area && (currentSize == null || size.minArea > currentSize.minArea)) {
                    currentSize = size;
                }
            }
        }

        if (currentSize != null) return currentSize;

        return DEFAULT;
    }
}
