package de.bsdlr.rooms.set;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;

import javax.annotation.Nonnull;

public class SetBlockType {
    public static final BuilderCodec<SetBlockType> CODEC = BuilderCodec.builder(SetBlockType.class, SetBlockType::new)
            .appendInherited(new KeyedCodec<>("BlockId", Codec.STRING),
                    ((setBlock, s) -> setBlock.blockId = s),
                    (setBlockType -> setBlockType.blockId),
                    ((setBlockType, parent) -> setBlockType.blockId = parent.blockId)
            )
            .add()
            .appendInherited(new KeyedCodec<>("Offset", Vector3i.CODEC),
                    ((setBlockType, s) -> setBlockType.offset = s),
                    (setBlockType -> setBlockType.offset),
                    ((setBlockType, parent) -> setBlockType.offset = parent.offset)
            )
            .add()
            .appendInherited(
                    new KeyedCodec<>("RotationYawPlacementOffset", new EnumCodec<>(Rotation.class)),
                    (blockType, o) -> blockType.rotation = o,
                    blockType -> blockType.rotation,
                    (blockType, parent) -> blockType.rotation = parent.rotation
            )
            .addValidator(Validators.nonNull())
            .add()
            .build();
    protected String blockId;
    protected Vector3i offset;
    protected Rotation rotation = Rotation.None;

    public SetBlockType() {
    }

    public SetBlockType(@Nonnull SetBlockType other) {
        this.blockId = other.blockId;
        this.offset = other.offset;
    }

    public String getBlockId() {
        return blockId;
    }

    public Vector3i getOffset() {
        return offset;
    }
}
