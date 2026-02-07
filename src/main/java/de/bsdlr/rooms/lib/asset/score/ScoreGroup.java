package de.bsdlr.rooms.lib.asset.score;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.lib.asset.regex.AdvancedRegex;
import de.bsdlr.rooms.lib.asset.regex.AdvancedRegexValidator;
import de.bsdlr.rooms.lib.asset.regex.Regex;
import de.bsdlr.rooms.lib.asset.regex.RegexValidator;

import javax.annotation.Nonnull;

public class ScoreGroup implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, ScoreGroup>> {
    public static final AssetBuilderCodec<String, ScoreGroup> CODEC = AssetBuilderCodec.builder(
                    ScoreGroup.class,
                    ScoreGroup::new,
                    Codec.STRING,
                    (scoreGroup, s) -> scoreGroup.id = s,
                    ScoreGroup::getId,
                    (scoreGroup, data) -> scoreGroup.data = data,
                    scoreGroup -> scoreGroup.data
            )
            .appendInherited(new KeyedCodec<>("Score", Score.CODEC),
                    (scoreGroup, s) -> scoreGroup.score = s,
                    scoreGroup -> scoreGroup.score,
                    (scoreGroup, parent) -> scoreGroup.score = parent.score
            )
            .documentation("Defines the score for included block types, score may stack.")
            .add()
            .appendInherited(new KeyedCodec<>("IncludeAll", Codec.BOOLEAN),
                    (scoreGroup, s) -> scoreGroup.includeAll = s,
                    scoreGroup -> scoreGroup.includeAll,
                    (scoreGroup, parent) -> scoreGroup.includeAll = parent.includeAll
            )
            .documentation("If enabled ignores includes and only uses excludes.")
            .add()
            .appendInherited(new KeyedCodec<>("IncludeBlockTypes", new ArrayCodec<>(Regex.CODEC, Regex[]::new)),
                    (scoreGroup, s) -> scoreGroup.includeBlockTypes = s,
                    scoreGroup -> scoreGroup.includeBlockTypes,
                    (scoreGroup, parent) -> scoreGroup.includeBlockTypes = parent.includeBlockTypes
            )
            .documentation("Defines blocks that should be in the group.")
            .addValidator(new ArrayValidator<>(RegexValidator.BLOCK_TYPE_KEYS_VALIDATOR))
            .add()
            .appendInherited(new KeyedCodec<>("ExcludeBlockTypes", new ArrayCodec<>(Regex.CODEC, Regex[]::new)),
                    (scoreGroup, s) -> scoreGroup.excludeBlockTypes = s,
                    scoreGroup -> scoreGroup.excludeBlockTypes,
                    (scoreGroup, parent) -> scoreGroup.excludeBlockTypes = parent.excludeBlockTypes
            )
            .documentation("Defines blocks that should NOT be in the group.")
            .addValidator(new ArrayValidator<>(RegexValidator.BLOCK_TYPE_KEYS_VALIDATOR))
            .add()
            .build();
    private static AssetStore<String, ScoreGroup, IndexedLookupTableAssetMap<String, ScoreGroup>> ASSET_STORE;
    protected String id;
    protected AssetExtraInfo.Data data;
    protected Score score;
    protected boolean includeAll;
    protected Regex[] includeBlockTypes;
    protected Regex[] excludeBlockTypes;

    @Nonnull
    public static AssetStore<String, ScoreGroup, IndexedLookupTableAssetMap<String, ScoreGroup>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(ScoreGroup.class);
        }

        return ASSET_STORE;
    }

    @Nonnull
    public static IndexedLookupTableAssetMap<String, ScoreGroup> getAssetMap() {
        return (IndexedLookupTableAssetMap<String, ScoreGroup>) getAssetStore().getAssetMap();
    }

    protected ScoreGroup() {
    }

    public ScoreGroup(@Nonnull String id) {
        this.id = id;
    }

    public ScoreGroup(@Nonnull ScoreGroup other) {
        this.id = other.id;
        this.data = other.data;
        this.score = other.score;
        this.includeBlockTypes = other.includeBlockTypes;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    public Score getScore() {
        return score;
    }

    @Nonnull
    public Regex[] getIncludeBlockTypes() {
        return includeBlockTypes;
    }
}
