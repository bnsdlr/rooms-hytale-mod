package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.lib.asset.regex.Regex;
import de.bsdlr.rooms.lib.asset.regex.RegexValidator;
import de.bsdlr.rooms.lib.asset.score.Score;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreferredBlockType {
    protected static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final BuilderCodec<PreferredBlockType> CODEC = BuilderCodec.builder(PreferredBlockType.class, PreferredBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockIdPattern", Regex.CODEC),
                    ((preferredBlockType, s) -> preferredBlockType.blockIdPattern = s),
                    (preferredBlockType -> preferredBlockType.blockIdPattern),
                    ((preferredBlockType, parent) -> preferredBlockType.blockIdPattern = parent.blockIdPattern)
            )
            .addValidator(Validators.nonNull())
            .addValidator(new RegexValidator(BlockType.getAssetMap().getAssetMap().keySet()))
            .add()
            .appendInherited(new KeyedCodec<>("Score", Score.CODEC),
                    ((preferredBlockType, s) -> preferredBlockType.score = s),
                    (preferredBlockType -> preferredBlockType.score),
                    ((preferredBlockType, parent) -> preferredBlockType.score = parent.score)
            )
            .add()
            .afterDecode(PreferredBlockType::addMatchingBlockIds)
            .build();
    @Nonnull
    protected Regex blockIdPattern = new Regex(".*", null);
    @Nonnull
    protected Score score = new Score();

    private String[] blockIds;

    public PreferredBlockType() {
    }

    public PreferredBlockType(@Nonnull PreferredBlockType other) {
        this.blockIdPattern = other.blockIdPattern;
        this.score = other.score;
        this.blockIds = other.blockIds;
    }

    @Nonnull
    public static List<String> getMatchingBlockIds(@Nonnull Regex blockIdPattern) {
        List<String> matchingBlockIds = new ArrayList<>();
        Pattern pattern = blockIdPattern.getCompiledPattern();

        for (String blockId : BlockType.getAssetMap().getAssetMap().keySet()) {
            Matcher matcher = pattern.matcher(blockId);
            if (matcher.find()) {
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

    public String[] getMatchingBlockIds() {
        if (this.blockIds == null) {
            addMatchingBlockIds(this);
        }
        return this.blockIds;
    }

    @Nonnull
    public Regex getBlockIdPattern() {
        return blockIdPattern;
    }

    @Nonnull
    public Score getScore() {
        return score;
    }
}
