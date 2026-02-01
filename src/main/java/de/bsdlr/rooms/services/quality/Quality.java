package de.bsdlr.rooms.services.quality;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

import javax.annotation.Nonnull;

public class Quality implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, Quality>> {
    @Nonnull
    public static final AssetBuilderCodec<String, Quality> CODEC = AssetBuilderCodec.builder(
                    Quality.class,
                    Quality::new,
                    Codec.STRING,
                    (quality, s) -> quality.id = s,
                    Quality::getId,
                    (quality, data) -> quality.data = data,
                    quality -> quality.data
            )
            .append(
                    new KeyedCodec<>("QualityValue", Codec.INTEGER), (quality, integer) -> quality.qualityValue = integer, quality -> quality.qualityValue
            )
            .documentation("Define the value of the quality to order them, 0 being the lowest quality.")
            .add()
            .append(new KeyedCodec<>("Color", ProtocolCodecs.COLOR), (quality, s) -> quality.color = s, quality -> quality.color)
            .documentation("The color that'll be used to e.g. display the text.")
            .addValidator(Validators.nonNull())
            .add()
            .append(
                    new KeyedCodec<>("LocalizationKey", Codec.STRING), (quality, s) -> quality.localizationKey = s, quality -> quality.localizationKey
            )
            .documentation("The localization key for the quality name.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonEmptyString())
            .metadata(new UIEditor(new UIEditor.LocalizationKeyField("server.rooms.qualities.{assetId}", true)))
            .add()
            .build();
    public static final int DEFAULT_INDEX = 0;
    public static final String DEFAULT_ID = "Default";
    @Nonnull
    public static final Quality DEFAULT_QUALITY = new Quality("Default") {
        {
            this.qualityValue = -1;
            this.color = ColorParseUtil.hexStringToColor("#c9d2dd");
            this.localizationKey = "server.general.qualities.Default";
        }
    };
    @Nonnull
    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(Quality::getAssetStore));
    private static AssetStore<String, Quality, IndexedLookupTableAssetMap<String, Quality>> ASSET_STORE;
    protected AssetExtraInfo.Data data;
    protected String id;
    protected int qualityValue;
    protected Color color;
    protected String localizationKey;

    @Nonnull
    public static AssetStore<String, Quality, IndexedLookupTableAssetMap<String, Quality>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(Quality.class);
        }

        return ASSET_STORE;
    }

    @Nonnull
    public static IndexedLookupTableAssetMap<String, Quality> getAssetMap() {
        return (IndexedLookupTableAssetMap<String, Quality>) getAssetStore().getAssetMap();
    }

    protected Quality() {
    }

    public Quality(@Nonnull String id) {
        this.id = id;
    }

    public Quality(
            String id,
            int qualityValue,
            Color color,
            String localizationKey
    ) {
        this.id = id;
        this.qualityValue = qualityValue;
        this.color = color;
        this.localizationKey = localizationKey;
    }

    public String getId() {
        return this.id;
    }

    public int getQualityValue() {
        return this.qualityValue;
    }

    public Color getColor() {
        return this.color;
    }

    @Nonnull
    @Override
    public String toString() {
        return "Quality{id='"
                + this.id
                + "', qualityValue="
                + this.qualityValue
                + "', textColor='"
                + this.color
                + "', localizationKey='"
                + this.localizationKey
                + "}";
    }
}
