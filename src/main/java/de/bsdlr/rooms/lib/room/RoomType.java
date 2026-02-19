package de.bsdlr.rooms.lib.room;

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
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.lib.asset.AssetMapWithGroup;
import de.bsdlr.rooms.lib.asset.quality.Quality;
import de.bsdlr.rooms.lib.blocks.PreferredBlockType;
import de.bsdlr.rooms.lib.room.block.BoundRoomBlockType;
import de.bsdlr.rooms.lib.room.block.BoundRoomBlockTypeValidator;
import de.bsdlr.rooms.lib.room.block.RoomBlockType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class RoomType implements JsonAssetWithMap<String, AssetMapWithGroup<String, RoomType>> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
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
//            .appendInherited(new KeyedCodec<>("AllowedBlockIdPattern", Codec.STRING_ARRAY),
//                    ((roomType, s) -> roomType.allowedBlockIdPatterns = s),
//                    (roomType -> roomType.allowedBlockIdPatterns),
//                    ((roomType, parent) -> roomType.allowedBlockIdPatterns = parent.allowedBlockIdPatterns)
//            )
//            .documentation("Defaults to all.")
//            .addValidator(new ArrayValidator<>(PatternValidator.BLOCK_TYPE_KEYS_VALIDATOR))
//            .add()
            .appendInherited(new KeyedCodec<>("DisallowedBlockIdPattern", Codec.STRING_ARRAY),
                    ((roomType, s) -> roomType.disallowedBlockIdPatterns = s),
                    (roomType -> roomType.disallowedBlockIdPatterns),
                    ((roomType, parent) -> roomType.disallowedBlockIdPatterns = parent.disallowedBlockIdPatterns)
            )
            .documentation("List of disallowed block id patterns.")
            .addValidator(Validators.nonNullArrayElements())
            .add()
            .appendInherited(new KeyedCodec<>("DisallowedBlockGroups", Codec.STRING_ARRAY),
                    ((roomType, s) -> roomType.disallowedBlockGroups = s),
                    (roomType -> roomType.disallowedBlockGroups),
                    ((roomType, parent) -> roomType.disallowedBlockGroups = parent.disallowedBlockGroups)
            )
            .documentation("List of disallowed block groups.")
            .addValidator(Validators.nonNullArrayElements())
            .add()
            // TODO: disallow blocks that damage entities
//            .appendInherited(new KeyedCodec<>("DisallowedHitboxTypes", Codec.STRING_ARRAY),
//                    ((roomType, s) -> roomType.disallowedHitboxTypes = s),
//                    (roomType -> roomType.disallowedHitboxTypes),
//                    ((roomType, parent) -> roomType.disallowedHitboxTypes = parent.disallowedHitboxTypes)
//            )
//            .documentation("List of disallowed hitbox types.")
//            .addValidator(Validators.nonNullArrayElements())
//            .add()
            .appendInherited(new KeyedCodec<>("RoomBlocks", new ArrayCodec<>(RoomBlockType.CODEC, RoomBlockType[]::new)),
                    ((roomType, s) -> roomType.roomBlocks = s),
                    (roomType -> roomType.roomBlocks),
                    ((roomType, parent) -> roomType.roomBlocks = parent.roomBlocks)
            )
            .documentation("Required Blocks in the room.")
            .addValidator(RoomBlockType.VALIDATOR_CACHE.getArrayValidator())
            .add()
            .appendInherited(new KeyedCodec<>("FloorBlocks", new ArrayCodec<>(BoundRoomBlockType.CODEC, BoundRoomBlockType[]::new)),
                    ((roomType, s) -> roomType.floorBlocks = s),
                    (roomType -> roomType.floorBlocks),
                    ((roomType, parent) -> roomType.floorBlocks = parent.floorBlocks)
            )
            .documentation("Required Floor blocks.")
            .addValidator(new ArrayValidator<>(new BoundRoomBlockTypeValidator()))
            .add()
            .appendInherited(new KeyedCodec<>("WallBlocks", new ArrayCodec<>(BoundRoomBlockType.CODEC, BoundRoomBlockType[]::new)),
                    ((roomType, s) -> roomType.wallBlocks = s),
                    (roomType -> roomType.wallBlocks),
                    ((roomType, parent) -> roomType.wallBlocks = parent.wallBlocks)
            )
            .documentation("Required wall blocks.")
            .addValidator(new ArrayValidator<>(new BoundRoomBlockTypeValidator()))
            .add()
            .appendInherited(new KeyedCodec<>("PreferredWallBlocks", new ArrayCodec<>(PreferredBlockType.CODEC, PreferredBlockType[]::new)),
                    ((roomType, s) -> roomType.preferredWallBlocks = s),
                    (roomType -> roomType.preferredWallBlocks),
                    ((roomType, parent) -> roomType.preferredWallBlocks = parent.preferredWallBlocks)
            )
            .documentation("Preferred wall blocks.")
            .add()
            .appendInherited(new KeyedCodec<>("PreferredFloorBlocks", new ArrayCodec<>(PreferredBlockType.CODEC, PreferredBlockType[]::new)),
                    ((roomType, s) -> roomType.preferredFloorBlocks = s),
                    (roomType -> roomType.preferredFloorBlocks),
                    ((roomType, parent) -> roomType.preferredFloorBlocks = parent.preferredFloorBlocks)
            )
            .documentation("Preferred floor blocks.")
            .add()
//            .appendInherited(new KeyedCodec<>("Sets", new ArrayCodec<>(Codec.STRING, String[]::new)),
//                    ((roomType, s) -> roomType.setIds = s),
//                    (roomType -> roomType.setIds),
//                    ((roomType, parent) -> roomType.setIds = parent.setIds)
//            )
//            .addValidator(FurnitureSetType.VALIDATOR_CACHE.getArrayValidator())
//            .add()
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
            .addValidator(Validators.nonNull())
            .addValidator(Quality.VALIDATOR_CACHE.getValidator())
            .documentation("Ignore the error... don't know how to prevent it...")
            .add()
            .appendInherited(new KeyedCodec<>("Score", Codec.INTEGER),
                    (roomType, s) -> roomType.score = s,
                    roomType -> roomType.score,
                    (roomType, parent) -> roomType.score = parent.score)
            .add()
            .appendInherited(new KeyedCodec<>("MinArea", Codec.INTEGER),
                    (roomType, s) -> roomType.minArea = s,
                    roomType -> roomType.minArea,
                    (roomType, parent) -> roomType.minArea = parent.minArea)
            .addValidator(Validators.min(1))
            .add()
            .appendInherited(new KeyedCodec<>("RoomSizeIds", new ArrayCodec<>(Codec.STRING, String[]::new)),
                    ((roomType, s) -> roomType.roomSizeIds = s),
                    (roomType -> roomType.roomSizeIds),
                    ((roomType, parent) -> roomType.roomSizeIds = parent.roomSizeIds)
            )
            .documentation("If no room sizes are selected it will default to all (select normal if you want no prefix...).")
            .addValidator(RoomSize.VALIDATOR_CACHE.getArrayValidator())
            .addValidator(Validators.nonNullArrayElements())
            .add()
            .afterDecode(roomType -> {
                for (RoomBlockType roomBlockType : roomType.roomBlocks) {
                    roomType.minRoomBlockCount += roomBlockType.getMinCount();
                    RoomBlockType.addMatchingBlockIds(roomBlockType, roomType);
                    RoomBlockType.addMatchingBlockIdsForLogicOrs(roomBlockType, roomType);
                }
                for (BoundRoomBlockType roomBlockType : roomType.wallBlocks) {
                    roomType.minWallBlockCount += roomBlockType.getMinCount();
                    BoundRoomBlockType.addMatchingBlockIds(roomBlockType, roomType);
                }
                for (BoundRoomBlockType roomBlockType : roomType.wallBlocks) {
                    roomType.minFloorBlockCount += roomBlockType.getMinCount();
                    BoundRoomBlockType.addMatchingBlockIds(roomBlockType, roomType);
                }
            })
            .build();
    public static final String DEFAULT_KEY = "Room";
    public static final RoomType DEFAULT = new RoomType(DEFAULT_KEY);
    public static final String UNKNOWN_KEY = "Unknown_Room";
    public static final RoomType UNKNOWN = new RoomType(UNKNOWN_KEY) {
        {
            this.unknown = true;
        }
    };
    private static AssetStore<String, RoomType, AssetMapWithGroup<String, RoomType>> ASSET_STORE;
    protected String id;
    protected int priority;
    protected boolean unknown = false;
    protected AssetExtraInfo.Data data;
    protected RoomTranslationProperties translationProperties;
    //    protected String[] allowedBlockIdPatterns = null;
    protected String[] disallowedBlockIdPatterns = null;
    protected String[] disallowedBlockGroups = null;
    //    protected String[] disallowedHitboxTypes = null;
    protected RoomBlockType[] roomBlocks = new RoomBlockType[0];
    protected BoundRoomBlockType[] floorBlocks = new BoundRoomBlockType[0];
    protected BoundRoomBlockType[] wallBlocks = new BoundRoomBlockType[0];
    protected PreferredBlockType[] preferredWallBlocks = new PreferredBlockType[0];
    protected PreferredBlockType[] preferredFloorBlocks = new PreferredBlockType[0];
    protected String icon = "Icons/ItemCategories/Build-Roofs.png";
    protected String[] setIds;
    protected String group;
    protected String qualityId = Quality.COMMON_ID;
    protected String[] roomSizeIds = RoomSize.getAssetMap().getAssetMap().keySet().toArray(new String[0]);
    protected int score;
    protected int minArea = 1;

    protected int minRoomBlockCount = 0;
    protected int minWallBlockCount = 0;
    protected int minFloorBlockCount = 0;

    @Nullable
    public static RoomType fromString(@Nonnull String input) {
        return getAssetMap().getAsset(input);
    }

    public static AssetStore<String, RoomType, AssetMapWithGroup<String, RoomType>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(RoomType.class);
        }

        return ASSET_STORE;
    }

    public static AssetMapWithGroup<String, RoomType> getAssetMap() {
        return (AssetMapWithGroup<String, RoomType>) getAssetStore().getAssetMap();
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
//        this.allowedBlockIdPatterns = other.allowedBlockIdPatterns;
        this.disallowedBlockIdPatterns = other.disallowedBlockIdPatterns;
        this.disallowedBlockGroups = other.disallowedBlockGroups;
        this.roomBlocks = other.roomBlocks;
        this.wallBlocks = other.wallBlocks;
        this.floorBlocks = other.floorBlocks;
        this.preferredWallBlocks = other.preferredWallBlocks;
        this.preferredFloorBlocks = other.preferredFloorBlocks;
        this.setIds = other.setIds;
        this.icon = other.icon;
        this.roomSizeIds = other.roomSizeIds;
        this.group = other.group;
        this.qualityId = other.qualityId;
        this.score = other.score;
        this.minArea = other.minArea;

        this.minRoomBlockCount = other.minRoomBlockCount;
        this.minWallBlockCount = other.minWallBlockCount;
        this.minFloorBlockCount = other.minFloorBlockCount;
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

//    public String[] getAllowedBlockIdPatterns() {
//        return allowedBlockIdPatterns;
//    }

    public String[] getDisallowedBlockIdPatterns() {
        return disallowedBlockIdPatterns;
    }

    public RoomBlockType[] getRoomBlocks() {
        return roomBlocks;
    }

    public BoundRoomBlockType[] getFloorBlocks() {
        return floorBlocks;
    }

    public BoundRoomBlockType[] getWallBlocks() {
        return wallBlocks;
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

    public PreferredBlockType[] getPreferredWallBlocks() {
        return preferredWallBlocks;
    }

    public PreferredBlockType[] getPreferredFloorBlocks() {
        return preferredFloorBlocks;
    }

    public int getScore() {
        return score;
    }

    public String[] getDisallowedBlockGroups() {
        return disallowedBlockGroups;
    }

    public int getMinRoomBlockCount() {
        return minRoomBlockCount;
    }

    public int getMinWallBlockCount() {
        return minWallBlockCount;
    }

    public int getMinFloorBlockCount() {
        return minFloorBlockCount;
    }

    public int getMinArea() {
        return minArea;
    }

    public String[] getRoomSizeIds() {
        return roomSizeIds;
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

    public int getExtraScore(String id, Map<String, Integer> wallBlockId2Count, Map<String, Integer> floorBlockId2Count) {
        int extraScore = 0;

        if (wallBlockId2Count.containsKey(id)) {
            for (PreferredBlockType preferredBlockType : preferredWallBlocks) {
                if (preferredBlockType.matches(id)) {
                    extraScore += preferredBlockType.getScore() * wallBlockId2Count.getOrDefault(id, 0);
                }
            }
        }

        if (floorBlockId2Count.containsKey(id)) {
            for (PreferredBlockType preferredBlockType : preferredFloorBlocks) {
                if (preferredBlockType.matches(id)) {
                    extraScore += preferredBlockType.getScore() * floorBlockId2Count.getOrDefault(id, 0);
                }
            }
        }

        return extraScore;
    }

    public static RoomType findBetter(RoomType o1, RoomType o2) {
        if (o2 == null) return o1;
        if (o1 == null) return o2;

        if (o1.priority < o2.priority) return o2;
        if (o1.priority > o2.priority) return o1;

        Quality q1 = o1.getQualityOrDefault(Quality.DEFAULT_QUALITY);
        Quality q2 = o2.getQualityOrDefault(Quality.DEFAULT_QUALITY);
        if (q1.getQualityValue() < q2.getQualityValue()) return o2;
        if (q1.getQualityValue() > q2.getQualityValue()) return o1;

        if (o1.minRoomBlockCount < o2.minRoomBlockCount) return o2;
        if (o1.minRoomBlockCount > o2.minRoomBlockCount) return o1;

        int fw1 = o1.minWallBlockCount * o1.minFloorBlockCount;
        int fw2 = o2.minWallBlockCount * o2.minFloorBlockCount;
        if (fw1 < fw2) return o2;
        if (fw1 > fw2) return o1;

//        if (o1.minWallBlockCount < o2.minWallBlockCount) return o2;
//        if (o1.minWallBlockCount > o2.minWallBlockCount) return o1;
//
//        if (o1.minFloorBlockCount < o2.minFloorBlockCount) return o2;
//        if (o1.minFloorBlockCount > o2.minFloorBlockCount) return o1;

        if (o1.roomBlocks.length < o2.roomBlocks.length) return o2;
        if (o1.roomBlocks.length > o2.roomBlocks.length) return o1;

        if (o1.score < o2.score) return o2;
        return o1;
    }

    public boolean matches(int area, Map<String, Integer> blockId2Count, Map<String, Integer> wallBlockId2Count, int wallBlockCount, Map<String, Integer> floorBlockId2Count, int floorBlockCount) {
        if (area < minArea) return false;
        boolean matches = true;

        if (disallowedBlockIdPatterns != null) {
            for (String blockId : blockId2Count.keySet()) {
                for (String disallowedBlockIdPattern : disallowedBlockIdPatterns) {
                    if (StringUtil.isGlobMatching(disallowedBlockIdPattern, blockId)) {
                        LOGGER.atInfo().log("%s is disallowed by the pattern: %s", blockId, disallowedBlockIdPattern);
                        return false;
                    }
                }
            }
        }

//                LOGGER.atInfo().log("block count: %d", type.getRoomBlocks().length);
        for (RoomBlockType blockType : getRoomBlocks()) {
            int count = (int) Math.floor(blockType.getCount(blockId2Count));
            LOGGER.atInfo().log("%d matches for %s (min: %d, max: %d)", count, blockType.getBlockIdPattern(), blockType.getMinCount(), blockType.getMaxCount());
            if (blockType.getMinCount() > count || blockType.getMaxCount() < count) {
                matches = false;
                break;
            }
        }

        if (!matches) return false;

        for (BoundRoomBlockType blockType : getWallBlocks()) {
            double count = blockType.getCount(wallBlockId2Count);
            LOGGER.atInfo().log("(WALL) %f matches for %s (min: %d, max: %d)", count, blockType.getBlockIdPattern(), blockType.getMinCount(), blockType.getMaxCount());

            double percentage = (count / wallBlockCount) * 100;
            LOGGER.atInfo().log("%f / %d = %f", count, wallBlockCount, percentage);

            if (blockType.getMinPercentage() > percentage || blockType.getMaxPercentage() < percentage || blockType.getMinCount() > count || blockType.getMaxCount() < count) {
                matches = false;
                break;
            }
        }

        if (!matches) return false;

        for (BoundRoomBlockType blockType : getFloorBlocks()) {
            double count = blockType.getCount(floorBlockId2Count);
            LOGGER.atInfo().log("(FLOOR) %f matches for %s (min: %d, max: %d)", count, blockType.getBlockIdPattern(), blockType.getMinCount(), blockType.getMaxCount());

            double percentage = (count / floorBlockCount) * 100;
            LOGGER.atInfo().log("%f / %d = %f", count, floorBlockCount, percentage);

            if (blockType.getMinPercentage() > percentage || blockType.getMaxPercentage() < percentage || blockType.getMinCount() > count || blockType.getMaxCount() < count) {
                matches = false;
                break;
            }
        }

        return matches;
    }

    public boolean isBlockIdAllowed(BlockType type) {
        if (type == null) return true;

        if (disallowedBlockIdPatterns != null) {
            for (String disallowedBlockIdPattern : disallowedBlockIdPatterns) {
                if (StringUtil.isGlobMatching(disallowedBlockIdPattern, type.getId())) {
                    return false;
                }
            }
        }

        if (disallowedBlockGroups != null && type.getGroup() != null) {
            for (String disallowedBlockGroup : disallowedBlockGroups) {
                if (disallowedBlockGroup.equals(type.getGroup())) {
                    return false;
                }
            }
        }

        return true;
    }
}
