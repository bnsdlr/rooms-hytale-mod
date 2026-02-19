package de.bsdlr.rooms.lib.asset.validators;

import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import java.util.Set;
import java.util.stream.Collectors;

public class OtherValidators {
    public static final AssetKeyValidator<String> BLOCK_ASSET_KEY;
    public static final IncludesValidator<String> BLOCK_CUSTOM_MODEL;
    public static final IncludesValidator<String> BLOCK_HITBOX_TYPE;

    static {
        BLOCK_ASSET_KEY = new AssetKeyValidator<>(BlockType::getAssetStore);

        Set<String> customModels = BlockType.getAssetMap().getAssetMap().values().stream().map(BlockType::getCustomModel).collect(Collectors.toSet());
        BLOCK_CUSTOM_MODEL = new IncludesValidator<>(customModels, "Couldn't match any existing custom models used for a block.");

        Set<String> hitboxTypes = BlockType.getAssetMap().getAssetMap().values().stream().map(BlockType::getHitboxType).collect(Collectors.toSet());
        BLOCK_HITBOX_TYPE = new IncludesValidator<>(hitboxTypes, "Couldn't match any existing hitbox types used for a block.");
    }
}
