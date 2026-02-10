//package de.bsdlr.rooms.lib.room;
//
//import com.hypixel.hytale.assetstore.AssetExtraInfo;
//import com.hypixel.hytale.assetstore.AssetRegistry;
//import com.hypixel.hytale.assetstore.AssetStore;
//import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
//import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
//import com.hypixel.hytale.codec.Codec;
//import com.hypixel.hytale.codec.KeyedCodec;
//import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
//import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
//import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
//import com.hypixel.hytale.codec.validation.Validators;
//import de.bsdlr.rooms.config.PluginConfig;
//import de.bsdlr.rooms.lib.asset.AssetMapWithGroup;
//import de.bsdlr.rooms.lib.asset.quality.Quality;
//import de.bsdlr.rooms.lib.asset.score.Score;
//import de.bsdlr.rooms.lib.room.block.PreferredBlockType;
//import de.bsdlr.rooms.lib.room.block.RoomBlockType;
//import de.bsdlr.rooms.lib.set.FurnitureSetType;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//public class RoomGroup implements JsonAssetWithMap<String, AssetMapWithGroup<String, RoomGroup>> {
//    public static final AssetBuilderCodec<String, RoomGroup> CODEC = AssetBuilderCodec.builder(
//                    RoomGroup.class, RoomGroup::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
//            )
//            .appendInherited(new KeyedCodec<>("RoomBlocks", new ArrayCodec<>(RoomBlockType.CODEC, RoomBlockType[]::new)),
//                    ((roomGroup, s) -> roomGroup.roomBlocks = s),
//                    (roomGroup -> roomGroup.roomBlocks),
//                    ((roomGroup, parent) -> roomGroup.roomBlocks = parent.roomBlocks)
//            )
//            .documentation("Required Blocks in the room.")
//            .addValidator(RoomBlockType.VALIDATOR_CACHE.getArrayValidator())
//            .add()
//            .appendInherited(new KeyedCodec<>("PreferredWallBlocks", new ArrayCodec<>(PreferredBlockType.CODEC, PreferredBlockType[]::new)),
//                    ((roomGroup, s) -> roomGroup.preferredWallBlocks = s),
//                    (roomGroup -> roomGroup.preferredWallBlocks),
//                    ((roomGroup, parent) -> roomGroup.preferredWallBlocks = parent.preferredWallBlocks)
//            )
//            .documentation("Preferred wall blocks.")
//            .add()
//            .appendInherited(new KeyedCodec<>("PreferredFloorBlocks", new ArrayCodec<>(PreferredBlockType.CODEC, PreferredBlockType[]::new)),
//                    ((roomGroup, s) -> roomGroup.preferredFloorBlocks = s),
//                    (roomGroup -> roomGroup.preferredFloorBlocks),
//                    ((roomGroup, parent) -> roomGroup.preferredFloorBlocks = parent.preferredFloorBlocks)
//            )
//            .documentation("Preferred floor blocks.")
//            .add()
//            .appendInherited(new KeyedCodec<>("Icon", Codec.STRING), (item, s) -> item.icon = s, item -> item.icon, (item, parent) -> item.icon = parent.icon)
//            .documentation("Default icon for each room part of the group.")
//            .addValidator(PluginConfig.ICON_VALIDATOR)
//            .metadata(new UIEditor(new UIEditor.Icon("Icons/ItemCategories/Build-Roofs.png", 64, 64)))
//            .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.ITEM_ICONS))
//            .add()
//            .appendInherited(new KeyedCodec<>("Quality", Codec.STRING),
//                    (roomGroup, s) -> roomGroup.qualityId = s,
//                    roomGroup -> roomGroup.qualityId,
//                    (roomGroup, parent) -> roomGroup.qualityId = parent.qualityId)
//            .addValidator(Quality.VALIDATOR_CACHE.getValidator())
//            .documentation("Default quality for rooms part of this group.")
//            .add()
//            .appendInherited(new KeyedCodec<>("Score", Score.CODEC),
//                    (roomGroup, s) -> roomGroup.score = s,
//                    roomGroup -> roomGroup.score,
//                    (roomGroup, parent) -> roomGroup.score = parent.score)
//            .add()
//            .appendInherited(new KeyedCodec<>("MinArea", Codec.INTEGER),
//                    (roomGroup, s) -> roomGroup.minArea = s,
//                    roomGroup -> roomGroup.minArea,
//                    (roomGroup, parent) -> roomGroup.minArea = parent.minArea)
//            .addValidator(Validators.min(1))
//            .add()
//            .build();
//    public static final String UNKNOWN_KEY = "Unknown";
//    public static final RoomGroup UNKNOWN = new RoomGroup(UNKNOWN_KEY) {
//        {
//            this.unknown = true;
//        }
//    };
//    private static AssetStore<String, RoomGroup, AssetMapWithGroup<String, RoomGroup>> ASSET_STORE;
//    protected String id;
//    protected boolean unknown = false;
//    protected AssetExtraInfo.Data data;
//    protected RoomBlockType[] roomBlocks = new RoomBlockType[0];
//    protected PreferredBlockType[] preferredWallBlocks = new PreferredBlockType[0];
//    protected PreferredBlockType[] preferredFloorBlocks = new PreferredBlockType[0];
//    protected String icon = "Icons/ItemCategories/Build-Roofs.png";
//    protected String qualityId = Quality.DEFAULT_ID;
//    protected Score score = new Score();
//    protected int minArea = 1;
//
//    @Nullable
//    public static RoomGroup fromString(@Nonnull String input) {
//        return getAssetMap().getAsset(input);
//    }
//
//    public static AssetStore<String, RoomGroup, AssetMapWithGroup<String, RoomGroup>> getAssetStore() {
//        if (ASSET_STORE == null) {
//            ASSET_STORE = AssetRegistry.getAssetStore(RoomGroup.class);
//        }
//
//        return ASSET_STORE;
//    }
//
//    public static AssetMapWithGroup<String, RoomGroup> getAssetMap() {
//        return (AssetMapWithGroup<String, RoomGroup>) getAssetStore().getAssetMap();
//    }
//
//    public RoomGroup() {
//    }
//
//    public RoomGroup(@Nonnull String id) {
//        this.id = id;
//    }
//
//    @Override
//    public String getId() {
//        return id;
//    }
//}
