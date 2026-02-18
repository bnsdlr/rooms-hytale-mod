package de.bsdlr.rooms.lib.asset.validators;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

public class PatternValidator implements Validator<String> {
    public static final PatternValidator BLOCK_IDS;
    public static final PatternValidator BLOCK_GROUPS;
    public static final PatternValidator HITBOX_TYPES;

    @Nonnull
    private final Set<String> options;
    private final String noMatchError;

    public PatternValidator(@Nonnull Set<String> options) {
        this.options = options;
        this.noMatchError = null;
    }

    public PatternValidator(@Nonnull Set<String> options, String noMatchError) {
        this.options = options;
        this.noMatchError = noMatchError;
    }

    @Override
    public void accept(String pattern, ValidationResults results) {
        if (pattern.isBlank()) results.fail("Pattern is blank (empty or only whitespace).");
        boolean anyMatch = false;

        for (String option : options) {
            if (StringUtil.isGlobMatching(pattern, option)) {
                anyMatch = true;
                break;
            }
        }

        if (!anyMatch) {
            if (noMatchError != null) results.fail(noMatchError);
            else results.fail("Pattern doesn't match any existing option.");
        }
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {

    }

    static {
        BLOCK_IDS = new PatternValidator(BlockType.getAssetMap().getAssetMap().keySet(), "Pattern doesn't match any existing block id.");
        Set<String> blockGroups = BlockType.getAssetMap().getAssetMap().values().stream().map(BlockType::getGroup).collect(Collectors.toSet());
        BLOCK_GROUPS = new PatternValidator(blockGroups, "String doesn't match any existing block group.");
        Set<String> hitboxTypes = BlockType.getAssetMap().getAssetMap().values().stream().map(BlockType::getHitboxType).collect(Collectors.toSet());
        HITBOX_TYPES = new PatternValidator(hitboxTypes, "String doesn't match any existing hitbox type.");
    }
}
