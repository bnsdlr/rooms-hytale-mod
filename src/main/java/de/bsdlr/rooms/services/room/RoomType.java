package de.bsdlr.rooms.services.room;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.protocol.Color;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.services.quality.Quality;
import de.bsdlr.rooms.services.room.block.RoomBlockType;
import de.bsdlr.rooms.services.set.FurnitureSetType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RoomType implements JsonAssetWithMap<String, RoomTypeAssetMap<String, RoomType>>
//        , NetworkSerializable<de.rooms.protocol.RoomType>
{
    public static final AssetBuilderCodec<String, RoomType> CODEC = AssetBuilderCodec.builder(
                    RoomType.class, RoomType::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
            )
            .appendInherited(new KeyedCodec<>("Priority", Codec.INTEGER),
                    ((roomType, s) -> roomType.priority = s),
                    (roomType -> roomType.priority),
                    ((roomType, parent) -> roomType.priority = parent.priority)
            )
            .documentation("Priority of this room, if there are other matching rooms the one with the higher required blocks or priority will be chosen.")
            .add()
            .appendInherited(new KeyedCodec<>("RoomBlocks", new ArrayCodec<>(RoomBlockType.CODEC, RoomBlockType[]::new)),
                    ((roomType, s) -> roomType.roomBlocks = s),
                    (roomType -> roomType.roomBlocks),
                    ((roomType, parent) -> roomType.roomBlocks = parent.roomBlocks)
            )
            .documentation("Required Blocks in the room.")
            .add()
            .appendInherited(new KeyedCodec<>("Sets", new ArrayCodec<>(Codec.STRING, String[]::new)),
                    ((roomType, s) -> roomType.setIds = s),
                    (roomType -> roomType.setIds),
                    ((roomType, parent) -> roomType.setIds = parent.setIds)
            )
            .addValidator(FurnitureSetType.VALIDATOR_CACHE.getArrayValidator())
            .add()
            .appendInherited(new KeyedCodec<>("Icon", Codec.STRING), (item, s) -> item.icon = s, item -> item.icon, (item, parent) -> item.icon = parent.icon)
            .addValidator(PluginConfig.ICON_VALIDATOR)
            .metadata(new UIEditor(new UIEditor.Icon("Icons/ItemCategories/Build-Roofs.png", 64, 64)))
            .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.ITEM_ICONS))
            .add()
            .appendInherited(
                    new KeyedCodec<>("RoomTranslationProperties", RoomTranslationProperties.CODEC),
                    (item, s) -> item.translationProperties = s,
                    item -> item.translationProperties,
                    (item, parent) -> item.translationProperties = parent.translationProperties
            )
            .documentation("The translation properties for this room.")
            .add()
            .appendInherited(new KeyedCodec<>("Group", Codec.STRING),
                    ((roomType, s) -> roomType.group = s),
                    (roomType -> roomType.group),
                    ((roomType, parent) -> roomType.group = parent.group)
            )
            .add()
            .appendInherited(new KeyedCodec<>("Quality", Codec.STRING),
                    (roomType, s) -> roomType.qualityId = s,
                    roomType -> roomType.qualityId,
                    (roomType, parent) -> roomType.qualityId = parent.qualityId)
            .addValidator(Quality.VALIDATOR_CACHE.getValidator())
            .documentation("Ignore the error... don't know how to prevent it...")
            .add()
            .appendInherited(new KeyedCodec<>("AdditionalScore", Codec.INTEGER),
                    (roomType, s) -> roomType.additionalScore = s,
                    roomType -> roomType.additionalScore,
                    (roomType, parent) -> roomType.additionalScore = parent.additionalScore)
            .add()
            .build();
    public static final String DEFAULT_KEY = "Room";
    public static final RoomTranslationProperties DEFAULT_TRANSLATION_PROPERTIES = new RoomTranslationProperties("Room", null);
    public static final RoomType DEFAULT = new RoomType(DEFAULT_KEY);
    public static final String UNKNOWN_KEY = "Unknown";
    public static final RoomType UNKNOWN = new RoomType(UNKNOWN_KEY) {
        {
            this.unknown = true;
        }
    };
    private static AssetStore<String, RoomType, RoomTypeAssetMap<String, RoomType>> ASSET_STORE;
    protected String id;
    protected int priority;
    protected boolean unknown = false;
    protected AssetExtraInfo.Data data;
    protected RoomTranslationProperties translationProperties = DEFAULT_TRANSLATION_PROPERTIES;
    protected RoomBlockType[] roomBlocks = new RoomBlockType[0];
    protected String icon = "Icons/ItemCategories/Build-Roofs.png";
    protected String[] setIds;
    protected String group;
    protected String qualityId = Quality.DEFAULT_ID;
    protected int additionalScore;
//    protected int minArea = 1;
    // TODO:
    // min/max room height (from floor);
    // min/max room area

    @Nullable
    public static RoomType fromString(@Nonnull String input) {
        return getAssetMap().getAsset(input);
    }

    public static AssetStore<String, RoomType, RoomTypeAssetMap<String, RoomType>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(RoomType.class);
        }

        return ASSET_STORE;
    }

    public static RoomTypeAssetMap<String, RoomType> getAssetMap() {
        return (RoomTypeAssetMap<String, RoomType>) getAssetStore().getAssetMap();
    }

    public RoomType() {
    }

    public RoomType(String id) {
        this.id = id;
    }

    public RoomType(@Nonnull RoomType other) {
        this.id = other.id;
        this.priority = other.priority;
        this.unknown = other.unknown;
        this.translationProperties = other.translationProperties;
        this.data = other.data;
        this.roomBlocks = other.roomBlocks;
        this.setIds = other.setIds;
        this.icon = other.icon;
        this.group = other.group;
        this.qualityId = other.qualityId;
        this.additionalScore = other.additionalScore;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public int getPriority() {
        return priority;
    }

    public AssetExtraInfo.Data getData() {
        return data;
    }

    public RoomBlockType[] getRoomBlocks() {
        return roomBlocks;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public RoomTranslationProperties getTranslationProperties() {
        return translationProperties;
    }

    public String getIcon() {
        return icon;
    }

    public String[] getSetIds() {
        return setIds;
    }

    public String getGroup() {
        return group;
    }

    public String getQualityId() {
        return qualityId;
    }

    public int getAdditionalScore() {
        return additionalScore;
    }

    public Quality getQuality() {
        return Quality.getAssetMap().getAsset(qualityId);
    }

    public Quality getQualityOrDefault(Quality defaultVal) {
        Quality quality = Quality.getAssetMap().getAsset(qualityId);
        if (quality == null) return defaultVal;
        return quality;
    }

    public Color getColorOrFallback() {
        Quality quality = Quality.getAssetMap().getAsset(qualityId);
        if (quality == null) quality = Quality.DEFAULT_QUALITY;
        return quality.getColor();
    }

    @Nonnull
    public static RoomType getUnknownFor(String roomTypeKey) {
        return UNKNOWN.clone(roomTypeKey);
    }

    public RoomType clone(@Nonnull String newKey) {
        if (this.id != null && this.id.equals(newKey)) {
            return this;
        } else {
            RoomType cloned = new RoomType(this);
            cloned.id = newKey;
            return cloned;
        }
    }

    public static RoomType getBetter(RoomType o1, RoomType o2) {
        if (o2 == null) return o1;
        if (o1 == null) return o2;
        if (o1.priority < o2.priority) return o2;
        if (o1.priority > o2.priority) return o1;
        Quality q1 = o1.getQualityOrDefault(Quality.DEFAULT_QUALITY);
        Quality q2 = o2.getQualityOrDefault(Quality.DEFAULT_QUALITY);
        if (q1.getQualityValue() < q2.getQualityValue()) return o2;
        if (q1.getQualityValue() > q2.getQualityValue()) return o1;
        if (o1.roomBlocks.length < o2.roomBlocks.length) return o2;
        if (o1.roomBlocks.length > o2.roomBlocks.length) return o1;
        if (o1.additionalScore < o2.additionalScore) return o2;
        return o1;
    }
}
