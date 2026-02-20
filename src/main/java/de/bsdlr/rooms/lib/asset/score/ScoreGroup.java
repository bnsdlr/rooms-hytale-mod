package de.bsdlr.rooms.lib.asset.score;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.lib.asset.validators.OtherValidators;
import de.bsdlr.rooms.lib.asset.validators.PatternValidator;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            .appendInherited(new KeyedCodec<>("Score", Codec.INTEGER),
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
            .appendInherited(new KeyedCodec<>("IncludeBlockTypes", Codec.STRING_ARRAY),
                    (scoreGroup, s) -> scoreGroup.includeBlockTypes = s,
                    scoreGroup -> scoreGroup.includeBlockTypes,
                    (scoreGroup, parent) -> scoreGroup.includeBlockTypes = parent.includeBlockTypes
            )
            .documentation("Defines blocks (by id) that should be in the group.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonNullArrayElements())
            .addValidator(new ArrayValidator<>(PatternValidator.BLOCK_IDS))
            .add()
            .appendInherited(new KeyedCodec<>("ExcludeBlockTypes", Codec.STRING_ARRAY),
                    (scoreGroup, s) -> scoreGroup.excludeBlockTypes = s,
                    scoreGroup -> scoreGroup.excludeBlockTypes,
                    (scoreGroup, parent) -> scoreGroup.excludeBlockTypes = parent.excludeBlockTypes
            )
            .documentation("Defines blocks (by id) that should NOT be in the group.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonNullArrayElements())
            .addValidator(new ArrayValidator<>(PatternValidator.BLOCK_IDS))
            .add()

            .appendInherited(new KeyedCodec<>("IncludeBlockGroups", Codec.STRING_ARRAY),
                    (scoreGroup, s) -> scoreGroup.includeBlockGroups = s,
                    scoreGroup -> scoreGroup.includeBlockGroups,
                    (scoreGroup, parent) -> scoreGroup.includeBlockGroups = parent.includeBlockGroups
            )
            .documentation("Defines blocks (by group) that should be in the group.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonNullArrayElements())
            .addValidator(new ArrayValidator<>(OtherValidators.BLOCK_GROUPS))
            .add()
            .appendInherited(new KeyedCodec<>("ExcludeBlockGroups", Codec.STRING_ARRAY),
                    (scoreGroup, s) -> scoreGroup.excludeBlockGroups = s,
                    scoreGroup -> scoreGroup.excludeBlockGroups,
                    (scoreGroup, parent) -> scoreGroup.excludeBlockGroups = parent.excludeBlockGroups
            )
            .documentation("Defines blocks (by group) that should NOT be in the group.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonNullArrayElements())
            .addValidator(new ArrayValidator<>(OtherValidators.BLOCK_GROUPS))
            .add()

            .appendInherited(new KeyedCodec<>("IncludeHitboxTypes", Codec.STRING_ARRAY),
                    (scoreGroup, s) -> scoreGroup.includeHitboxTypes = s,
                    scoreGroup -> scoreGroup.includeHitboxTypes,
                    (scoreGroup, parent) -> scoreGroup.includeHitboxTypes = parent.includeHitboxTypes
            )
            .documentation("Defines blocks (by hitbox type) that should be in the group.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonNullArrayElements())
            .addValidator(new ArrayValidator<>(OtherValidators.BLOCK_HITBOX_TYPE))
            .add()
            .appendInherited(new KeyedCodec<>("ExcludeHitboxTypes", Codec.STRING_ARRAY),
                    (scoreGroup, s) -> scoreGroup.excludeHitboxTypes = s,
                    scoreGroup -> scoreGroup.excludeHitboxTypes,
                    (scoreGroup, parent) -> scoreGroup.excludeHitboxTypes = parent.excludeHitboxTypes
            )
            .documentation("Defines blocks (by hitbox type) that should NOT be in the group.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonNullArrayElements())
            .addValidator(new ArrayValidator<>(OtherValidators.BLOCK_HITBOX_TYPE))
            .add()
            .build();
    private static AssetStore<String, ScoreGroup, IndexedLookupTableAssetMap<String, ScoreGroup>> ASSET_STORE;
    protected String id;
    protected AssetExtraInfo.Data data;
    protected int score;
    protected boolean includeAll;
    @Nonnull
    protected String[] includeBlockTypes = new String[0];
    @Nonnull
    protected String[] excludeBlockTypes = new String[0];
    @Nonnull
    protected String[] includeBlockGroups = new String[0];
    @Nonnull
    protected String[] excludeBlockGroups = new String[0];
    @Nonnull
    protected String[] includeHitboxTypes = new String[0];
    @Nonnull
    protected String[] excludeHitboxTypes = new String[0];

    private String[] matchingBlockIds;

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
        this.includeAll = other.includeAll;
        this.includeBlockTypes = other.includeBlockTypes;
        this.excludeBlockTypes = other.excludeBlockTypes;
        this.includeBlockGroups = other.includeBlockGroups;
        this.excludeBlockGroups = other.excludeBlockGroups;
        this.includeHitboxTypes = other.includeHitboxTypes;
        this.excludeHitboxTypes = other.excludeHitboxTypes;

        this.matchingBlockIds = other.matchingBlockIds;
    }

    @Nonnull
    public static List<String> findMatchingBlockIds(ScoreGroup scoreGroup) {
        List<String> matchingBlockIds = new ArrayList<>();

        for (BlockType type : BlockType.getAssetMap().getAssetMap().values()) {
            if (ScoreGroup.matches(scoreGroup, type)) {
                matchingBlockIds.add(type.getId());
            }
        }

        return matchingBlockIds;
    }

    public static void addMatchingBlockIds(ScoreGroup scoreGroup) {
        scoreGroup.matchingBlockIds = findMatchingBlockIds(scoreGroup).toArray(String[]::new);
    }

    public static boolean matches(ScoreGroup scoreGroup, BlockType type) {
        for (String pattern : scoreGroup.excludeBlockTypes) {
            if (StringUtil.isGlobMatching(pattern, type.getId())) {
                return false;
            }
        }

        for (String group : scoreGroup.excludeBlockGroups) {
            if (group.equals(type.getGroup())) {
                return false;
            }
        }

        for (String hitbox : scoreGroup.excludeHitboxTypes) {
            if (hitbox.equals(type.getHitboxType())) {
                return false;
            }
        }

        if (scoreGroup.includeAll) return true;

        boolean typeMatches = scoreGroup.includeBlockTypes.length == 0;
        boolean groupMatches = scoreGroup.includeBlockGroups.length == 0;
        boolean hitboxMatches = scoreGroup.includeHitboxTypes.length == 0;

        for (String pattern : scoreGroup.includeBlockTypes) {
            if (StringUtil.isGlobMatching(pattern, type.getId())) {
                typeMatches = true;
                break;
            }
        }

        if (typeMatches) {
            for (String group : scoreGroup.includeBlockGroups) {
                if (group.equals(type.getGroup())) {
                    groupMatches = true;
                    break;
                }
            }
        }

        if (groupMatches) {
            for (String hitbox : scoreGroup.includeHitboxTypes) {
                if (hitbox.equals(type.getHitboxType())) {
                    hitboxMatches = true;
                    break;
                }
            }
        }

        return typeMatches && groupMatches && hitboxMatches;
    }

    public String[] getMatchingBlockIds() {
        if (matchingBlockIds == null) addMatchingBlockIds(this);
        return matchingBlockIds;
    }

    @Override
    public String getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public AssetExtraInfo.Data getData() {
        return data;
    }

    public boolean isIncludeAll() {
        return includeAll;
    }

    public String[] getIncludeBlockTypes() {
        return includeBlockTypes;
    }

    public String[] getExcludeBlockTypes() {
        return excludeBlockTypes;
    }

    @Nonnull
    public String[] getIncludeBlockGroups() {
        return includeBlockGroups;
    }

    @Nonnull
    public String[] getExcludeBlockGroups() {
        return excludeBlockGroups;
    }

    @Nonnull
    public String[] getIncludeHitboxTypes() {
        return includeHitboxTypes;
    }

    @Nonnull
    public String[] getExcludeHitboxTypes() {
        return excludeHitboxTypes;
    }
}
