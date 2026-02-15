package de.bsdlr.rooms.lib.blocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.lib.asset.pattern.PatternValidator;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PreferredBlockType {
    protected static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final BuilderCodec<PreferredBlockType> CODEC = BuilderCodec.builder(PreferredBlockType.class, PreferredBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockIdPattern", Codec.STRING),
                    ((preferredBlockType, s) -> preferredBlockType.blockIdPattern = s),
                    (preferredBlockType -> preferredBlockType.blockIdPattern),
                    ((preferredBlockType, parent) -> preferredBlockType.blockIdPattern = parent.blockIdPattern)
            )
            .addValidator(Validators.nonNull())
            .addValidator(PatternValidator.BLOCK_TYPE_KEYS_VALIDATOR)
            .add()
            .appendInherited(new KeyedCodec<>("Score", Codec.INTEGER),
                    ((preferredBlockType, s) -> preferredBlockType.score = s),
                    (preferredBlockType -> preferredBlockType.score),
                    ((preferredBlockType, parent) -> preferredBlockType.score = parent.score)
            )
            .add()
            .afterDecode(PreferredBlockType::addMatchingBlockIds)
            .build();
    @Nonnull
    protected String blockIdPattern = "*";
    protected int score = 0;

    private String[] blockIds;

    public PreferredBlockType() {
    }

    public PreferredBlockType(@Nonnull PreferredBlockType other) {
        this.blockIdPattern = other.blockIdPattern;
        this.score = other.score;
        this.blockIds = other.blockIds;
    }

    @Nonnull
    public static List<String> getMatchingBlockIds(@Nonnull String blockIdPattern) {
        List<String> matchingBlockIds = new ArrayList<>();

        for (String blockId : BlockType.getAssetMap().getAssetMap().keySet()) {
            if (StringUtil.isGlobMatching(blockIdPattern, blockId)) {
                matchingBlockIds.add(blockId);
            }
        }

        return matchingBlockIds;
    }

    public static void addMatchingBlockIds(PreferredBlockType preferredBlockType) {
        try {
            preferredBlockType.blockIds =
                    PreferredBlockType.getMatchingBlockIds(preferredBlockType.getBlockIdPattern()).toArray(new String[0]);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Could not find matching block ids.");
            throw e;
        }
    }

    public boolean matches(String blockId) {
        return StringUtil.isGlobMatching(blockIdPattern, blockId);
    }

    public String[] getMatchingBlockIds() {
        if (this.blockIds == null) {
            addMatchingBlockIds(this);
        }
        return this.blockIds;
    }

    @Nonnull
    public String getBlockIdPattern() {
        return blockIdPattern;
    }

    public int getScore() {
        return score;
    }
}
