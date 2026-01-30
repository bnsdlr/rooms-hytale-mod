package de.bsdlr.rooms.set;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import de.bsdlr.rooms.config.PluginConfig;
import de.bsdlr.rooms.room.TranslationProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetType implements JsonAssetWithMap<String, SetTypeAssetMap<String, SetType>> {
    public static final AssetBuilderCodec<String, SetType> CODEC = AssetBuilderCodec.builder(
                    SetType.class, SetType::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
            )
            .appendInherited(new KeyedCodec<>("TranslationProperties", TranslationProperties.CODEC),
                    ((setType, s) -> setType.translationProperties = s),
                    (setType -> setType.translationProperties),
                    ((setType, parent) -> setType.translationProperties = parent.translationProperties))
            .documentation("The translation properties for this room set.")
            .add()
            .appendInherited(new KeyedCodec<>("SetBlockTypes", new ArrayCodec<>(SetBlockType.CODEC, SetBlockType[]::new)),
                    ((setType, s) -> setType.setBlockTypes = s),
                    (setType -> setType.setBlockTypes),
                    ((setType, parent) -> setType.setBlockTypes = parent.setBlockTypes)
            )
            .addValidator(new SetBlockTypesValidator(2))
            .add()
            .appendInherited(new KeyedCodec<>("Icon", Codec.STRING), (item, s) -> item.icon = s, item -> item.icon, (item, parent) -> item.icon = parent.icon)
            .addValidator(PluginConfig.ICON_VALIDATOR)
            .metadata(new UIEditor(new UIEditor.Icon("Icons/ItemCategories/Build-Furnitures.png", 64, 64)))
            .metadata(new UIRebuildCaches(UIRebuildCaches.ClientCache.ITEM_ICONS))
            .add()
            .appendInherited(new KeyedCodec<>("Group", Codec.STRING),
                    ((setType, s) -> setType.group = s),
                    (setType -> setType.group),
                    ((setType, parent) -> setType.group = parent.group)
            )
            .documentation("test documentation")
            .add()
            .build();
    private static AssetStore<String, SetType, SetTypeAssetMap<String, SetType>> ASSET_STORE;
    public static final String UNKNOWN_KEY = "Unknown";
    public static final SetType UNKNOWN = new SetType(UNKNOWN_KEY) {
        {
            this.unknown = true;
        }
    };
    protected String id;
    protected boolean unknown;
    protected AssetExtraInfo.Data data;
    protected TranslationProperties translationProperties;
    protected String icon = "Icons/ItemCategories/Build-Furnitures.png";
    protected String group;
    protected SetBlockType[] setBlockTypes;

    @Nullable
    public static SetType fromString(@Nonnull String input) {
        return getAssetMap().getAsset(input);
    }

    public static AssetStore<String, SetType, SetTypeAssetMap<String, SetType>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(SetType.class);
        }

        return ASSET_STORE;
    }

    public static SetTypeAssetMap<String, SetType> getAssetMap() {
        return (SetTypeAssetMap<String, SetType>) getAssetStore().getAssetMap();
    }

    public SetType() {
    }

    public SetType(String id) {
        this.id = id;
    }

    public SetType(@Nonnull SetType other) {
        this.id = other.id;
        this.unknown = other.unknown;
        this.data = other.data;
        this.translationProperties = other.translationProperties;
        this.icon = other.icon;
        this.group = other.group;
        this.setBlockTypes = other.setBlockTypes;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public AssetExtraInfo.Data getData() {
        return data;
    }

    public TranslationProperties getTranslationProperties() {
        return translationProperties;
    }

    public String getIcon() {
        return icon;
    }

    public String getGroup() {
        return group;
    }

    public SetBlockType[] getSetBlockTypes() {
        return setBlockTypes;
    }

    @Nonnull
    public static SetType getUnknownFor(String setTypeKey) {
        return UNKNOWN.clone(setTypeKey);
    }


    public SetType clone(@Nonnull String newKey) {
        if (this.id != null && this.id.equals(newKey)) {
            return this;
        } else {
            SetType cloned = new SetType(this);
            this.id = newKey;
            return cloned;
        }
    }
}
