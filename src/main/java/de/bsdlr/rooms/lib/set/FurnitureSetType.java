//package de.bsdlr.rooms.lib.set;
//
//import com.hypixel.hytale.assetstore.AssetExtraInfo;
//import com.hypixel.hytale.assetstore.AssetKeyValidator;
//import com.hypixel.hytale.assetstore.AssetRegistry;
//import com.hypixel.hytale.assetstore.AssetStore;
//import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
//import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
//import com.hypixel.hytale.codec.Codec;
//import com.hypixel.hytale.codec.KeyedCodec;
//import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
//import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
//import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
//import com.hypixel.hytale.codec.validation.ValidatorCache;
//import com.hypixel.hytale.codec.validation.Validators;
//import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
//import de.bsdlr.rooms.config.PluginConfig;
//import de.bsdlr.rooms.lib.asset.AssetMapWithGroup;
//import de.bsdlr.rooms.lib.asset.quality.Quality;
//import de.bsdlr.rooms.lib.set.block.FurnitureSetBlockType;
//import de.bsdlr.rooms.lib.set.block.FurnitureSetBlockTypesValidator;
//import de.bsdlr.rooms.utils.ChunkManager;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.Objects;
//
//public class FurnitureSetType implements JsonAssetWithMap<String, AssetMapWithGroup<String, FurnitureSetType>> {
//    public static final AssetBuilderCodec<String, FurnitureSetType> CODEC = AssetBuilderCodec.builder(
//                    FurnitureSetType.class, FurnitureSetType::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
//            )
//            .appendInherited(new KeyedCodec<>("TranslationProperties", FurnitureSetTranslationProperties.CODEC),
//                    ((furnitureSetType, s) -> furnitureSetType.translationProperties = s),
//                    (furnitureSetType -> furnitureSetType.translationProperties),
//                    ((furnitureSetType, parent) -> furnitureSetType.translationProperties = parent.translationProperties))
//            .documentation("The translation properties for this furniture set.")
//            .add()
//            .appendInherited(new KeyedCodec<>("FurnitureSetBlocks", new ArrayCodec<>(FurnitureSetBlockType.CODEC, FurnitureSetBlockType[]::new)),
//                    ((furnitureSetType, s) -> furnitureSetType.blocks = s),
//                    (furnitureSetType -> furnitureSetType.blocks),
//                    ((furnitureSetType, parent) -> furnitureSetType.blocks = parent.blocks)
//            )
//            .addValidator(new FurnitureSetBlockTypesValidator(2, 10))
//            .add()
//            .appendInherited(new KeyedCodec<>("Icon", Codec.STRING),
//                    (item, s) -> item.icon = s,
//                    item -> item.icon,
//                    (item, parent) -> item.icon = parent.icon)
//            .addValidator(PluginConfig.ICON_VALIDATOR)
//            .metadata(new UIEditor(new UIEditor.Icon("Icons/ItemCategories/Build-Furnitures.png", 32, 32)))
//            .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.ITEM_ICONS))
//            .add()
//            .appendInherited(new KeyedCodec<>("Quality", Codec.STRING),
//                    (roomType, s) -> roomType.qualityId = s,
//                    roomType -> roomType.qualityId,
//                    (roomType, parent) -> roomType.qualityId = parent.qualityId)
//            .addValidator(Validators.nonNull())
//            .addValidator(Quality.VALIDATOR_CACHE.getValidator())
////            .documentation("Ignore the error... don't know how to prevent it...")
//            .add()
//            .appendInherited(new KeyedCodec<>("Score", Codec.INTEGER),
//                    (roomType, s) -> roomType.score = s,
//                    roomType -> roomType.score,
//                    (roomType, parent) -> roomType.score = parent.score)
//            .add()
//            .appendInherited(new KeyedCodec<>("Group", Codec.STRING),
//                    ((furnitureSetType, s) -> furnitureSetType.group = s),
//                    (furnitureSetType -> furnitureSetType.group),
//                    ((furnitureSetType, parent) -> furnitureSetType.group = parent.group)
//            )
////            .documentation("")
//            .add()
//            .build();
//    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(FurnitureSetType::getAssetStore));
//    private static AssetStore<String, FurnitureSetType, AssetMapWithGroup<String, FurnitureSetType>> ASSET_STORE;
//    public static final String UNKNOWN_KEY = "Unknown";
//    public static final FurnitureSetType UNKNOWN = new FurnitureSetType(UNKNOWN_KEY) {
//        {
//            this.unknown = true;
//        }
//    };
//    protected String id;
//    protected boolean unknown;
//    protected AssetExtraInfo.Data data;
//    protected FurnitureSetTranslationProperties translationProperties;
//    protected String icon = "Icons/ItemCategories/Build-Furnitures.png";
//    protected FurnitureSetBlockType[] blocks;
//    protected String qualityId = Quality.COMMON_ID;
//    protected int score = 0;
//    protected String group;
//
//    @Nullable
//    public static FurnitureSetType fromString(@Nonnull String input) {
//        return getAssetMap().getAsset(input);
//    }
//
//    public static AssetStore<String, FurnitureSetType, AssetMapWithGroup<String, FurnitureSetType>> getAssetStore() {
//        if (ASSET_STORE == null) {
//            ASSET_STORE = AssetRegistry.getAssetStore(FurnitureSetType.class);
//        }
//
//        return ASSET_STORE;
//    }
//
//    public static AssetMapWithGroup<String, FurnitureSetType> getAssetMap() {
//        return (AssetMapWithGroup<String, FurnitureSetType>) getAssetStore().getAssetMap();
//    }
//
//    public FurnitureSetType() {
//    }
//
//    public FurnitureSetType(String id) {
//        this.id = id;
//    }
//
//    public FurnitureSetType(@Nonnull FurnitureSetType other) {
//        this.id = other.id;
//        this.unknown = other.unknown;
//        this.data = other.data;
//        this.translationProperties = other.translationProperties;
//        this.blocks = other.blocks;
//        this.icon = other.icon;
//        this.group = other.group;
//        this.qualityId = other.qualityId;
//        this.score = other.score;
//    }
//
//    public boolean isValidAt(ChunkManager chunkManager, int x, int y, int z) {
//        for (FurnitureSetBlockType furnitureSetBlockType : blocks) {
//            int bx = x + furnitureSetBlockType.getXOffset();
//            int by = y + furnitureSetBlockType.getYOffset();
//            int bz = z + furnitureSetBlockType.getZOffset();
//            BlockType type = chunkManager.getBlockTypeAt(bx, by, bz);
//            if (type == null) return false;
//            if (!Objects.equals(type.getId(), furnitureSetBlockType.getBlockId())) return false;
//        }
//        return true;
//    }
//
//    @Override
//    public String getId() {
//        return this.id;
//    }
//
//    public boolean isUnknown() {
//        return unknown;
//    }
//
//    public AssetExtraInfo.Data getData() {
//        return data;
//    }
//
//    public FurnitureSetTranslationProperties getTranslationProperties() {
//        return translationProperties;
//    }
//
//    public String getIcon() {
//        return icon;
//    }
//
//    public String getGroup() {
//        return group;
//    }
//
//    public FurnitureSetBlockType[] getBlocks() {
//        return blocks;
//    }
//
//    public String getQualityId() {
//        return qualityId;
//    }
//
//    public int getScore() {
//        return score;
//    }
//
//    @Nonnull
//    public static FurnitureSetType getUnknownFor(String setTypeKey) {
//        return UNKNOWN.clone(setTypeKey);
//    }
//
//
//    public FurnitureSetType clone(@Nonnull String newKey) {
//        if (this.id != null && this.id.equals(newKey)) {
//            return this;
//        } else {
//            FurnitureSetType cloned = new FurnitureSetType(this);
//            this.id = newKey;
//            return cloned;
//        }
//    }
//}
