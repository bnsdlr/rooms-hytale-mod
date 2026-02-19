package de.bsdlr.rooms.lib.blocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.protocol.DrawType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.bsdlr.rooms.lib.asset.Light;
import de.bsdlr.rooms.lib.asset.validators.OtherValidators;
import de.bsdlr.rooms.lib.asset.validators.PatternValidator;

import javax.annotation.Nonnull;

public class BlockPattern {
    public static final BuilderCodec<BlockPattern> CODEC = BuilderCodec.builder(BlockPattern.class, BlockPattern::new)
            .appendInherited(new KeyedCodec<>("BlockIdPattern", Codec.STRING),
                    ((blockPattern, s) -> blockPattern.blockIdPattern = s),
                    (blockPattern -> blockPattern.blockIdPattern),
                    ((blockPattern, parent) -> blockPattern.blockIdPattern = parent.blockIdPattern)
            )
            .addValidator(Validators.nonNull())
            .addValidator(PatternValidator.BLOCK_IDS)
            .add()
            .appendInherited(new KeyedCodec<>("Group", Codec.STRING),
                    ((blockPattern, s) -> blockPattern.group = s),
                    (blockPattern -> blockPattern.group),
                    ((blockPattern, parent) -> blockPattern.group = parent.group)
            )
            .addValidator(OtherValidators.BLOCK_GROUPS)
            .add()
            .appendInherited(new KeyedCodec<>("Light", Light.CODEC),
                    ((blockPattern, s) -> blockPattern.light = s),
                    (blockPattern -> blockPattern.light),
                    ((blockPattern, parent) -> blockPattern.light = parent.light)
            )
            .add()
            .appendInherited(new KeyedCodec<>("CustomModel", Codec.STRING),
                    ((blockPattern, s) -> blockPattern.customModel = s),
                    (blockPattern -> blockPattern.customModel),
                    ((blockPattern, parent) -> blockPattern.customModel = parent.customModel)
            )
            .addValidator(OtherValidators.BLOCK_CUSTOM_MODEL)
            .add()
            .appendInherited(new KeyedCodec<>("DrawType", new EnumCodec<>(DrawType.class)),
                    ((blockPattern, s) -> blockPattern.drawType = s),
                    (blockPattern -> blockPattern.drawType),
                    ((blockPattern, parent) -> blockPattern.drawType = parent.drawType)
            )
            .add()
            .appendInherited(new KeyedCodec<>("Opacity", new EnumCodec<>(Opacity.class)),
                    ((blockPattern, s) -> blockPattern.opacity = s),
                    (blockPattern -> blockPattern.opacity),
                    ((blockPattern, parent) -> blockPattern.opacity = parent.opacity)
            )
            .add()
            .appendInherited(new KeyedCodec<>("HitboxType", Codec.STRING),
                    ((blockPattern, s) -> blockPattern.hitboxType = s),
                    (blockPattern -> blockPattern.hitboxType),
                    ((blockPattern, parent) -> blockPattern.hitboxType = parent.hitboxType)
            )
            .addValidator(OtherValidators.BLOCK_HITBOX_TYPE)
            .add()
            .afterDecode(blockPattern -> {
                if (blockPattern.group != null && blockPattern.group.isBlank()) blockPattern.group = null;
                if (blockPattern.customModel != null && blockPattern.customModel.isBlank())
                    blockPattern.customModel = null;
                if (blockPattern.hitboxType != null && blockPattern.hitboxType.isBlank())
                    blockPattern.hitboxType = null;
            })
            .build();
    @Nonnull
    protected String blockIdPattern = "*";
    protected String group;
    @Nonnull
    protected Light light = new Light();
    protected String customModel;
    protected DrawType drawType;
    protected Opacity opacity;
    protected String hitboxType;

    public BlockPattern() {
    }

    public BlockPattern(@Nonnull BlockPattern other) {
        this.blockIdPattern = other.blockIdPattern;
        this.group = other.group;
        this.light = other.light;
        this.customModel = other.customModel;
        this.drawType = other.drawType;
        this.opacity = other.opacity;
        this.hitboxType = other.hitboxType;
    }

    @Nonnull
    public String getBlockIdPattern() {
        return blockIdPattern;
    }

    public String getGroup() {
        return group;
    }

    @Nonnull
    public Light getLight() {
        return light;
    }

    public String getCustomModel() {
        return customModel;
    }

    public DrawType getDrawType() {
        return drawType;
    }

    public Opacity getOpacity() {
        return opacity;
    }

    public String getHitboxType() {
        return hitboxType;
    }

    public boolean matches(BlockType type) {
        if (!StringUtil.isGlobMatching(blockIdPattern, type.getId())) return false;
        if (group != null && !group.equals(type.getGroup())) return false;
        if (light.isEnabled() && !light.matches(type.getLight())) return false;
        if (customModel != null && !customModel.equals(type.getCustomModel())) return false;
        if (drawType != null && !drawType.equals(type.getDrawType())) return false;
        if (opacity != null && !opacity.equals(type.getOpacity())) return false;
        return hitboxType == null || hitboxType.equals(type.getHitboxType());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("BlockPattern{blockIdPattern=");
        builder.append(blockIdPattern);
        builder.append(",light=");
        builder.append(light);

        if (group != null) {
            builder.append(",group=");
            builder.append(group);
        }
        if (customModel != null) {
            builder.append(",customModel=");
            builder.append(customModel);
        }
        if (drawType != null) {
            builder.append(",drawType=");
            builder.append(drawType);
        }
        if (opacity != null) {
            builder.append(",opacity=");
            builder.append(opacity);
        }
        if (hitboxType != null) {
            builder.append(",hitboxType=");
            builder.append(hitboxType);
        }

        builder.append("}");

        return builder.toString();
    }
}
