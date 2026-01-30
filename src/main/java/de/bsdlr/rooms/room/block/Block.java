package de.bsdlr.rooms.room.block;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Block {
    public static final BuilderCodec<Block> CODEC = BuilderCodec.builder(Block.class, Block::new)
            .append(new KeyedCodec<>("BlockId", Codec.INTEGER, true),
                    Block::setBlockId,
                    Block::getBlockId).add()
            .append(new KeyedCodec<>("Position", Vector3i.CODEC, true),
                    Block::setPos,
                    Block::getPos).add()
            .append(new KeyedCodec<>("Role", new EnumCodec<>(BlockRole.class), true),
                    Block::setRole,
                    Block::getRole).add()
            .append(new KeyedCodec<>("Light", ProtocolCodecs.COLOR_LIGHT),
                    Block::setLight,
                    Block::getLight).add()
            .build();

    private int blockId;
    @Nonnull
    private Vector3i pos;
    @Nonnull
    private BlockRole role;
    @Nullable
    private ColorLight light;

    Block() {
        this.blockId = 0;
        this.pos = new Vector3i();
        this.role = BlockRole.NONE;
        this.light = null;
    }

    public Block(int blockId, @Nonnull Vector3i pos, @Nonnull BlockRole role, @Nullable ColorLight light) {
        this.blockId = blockId;
        this.pos = pos;
        this.role = role;
        this.light = light;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;

        Block o = (Block) obj;

        return this.blockId == o.blockId
                && this.pos.equals(o.pos)
                && this.role.equals(o.role)
                && Objects.equals(this.light, o.light);

    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(this.blockId);
        result = 31 * result + Objects.hashCode(this.pos);
        result = 31 * result + Objects.hashCode(this.role);
        result = 31 * result + Objects.hashCode(this.light);
        return result;
    }

    private void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public int getBlockId() {
        return blockId;
    }

    private void setPos(@Nonnull Vector3i pos) {
        this.pos = pos;
    }

    public @Nonnull Vector3i getPos() {
        return pos;
    }

    public int getX() {
        return pos.x;
    }

    public int getY() {
        return pos.y;
    }

    public int getZ() {
        return pos.z;
    }

    private void setRole(@Nonnull BlockRole role) {
        this.role = role;
    }

    public BlockRole getRole() {
        return role;
    }

    private void setLight(ColorLight light) {
        this.light = light;
    }

    public ColorLight getLight() {
        return light;
    }

    public boolean isSolid() {
        return role == BlockRole.SOLID;
    }

    public boolean isEntrance() {
        return role == BlockRole.ENTRANCE;
    }

    public boolean isFurniture() {
        return role == BlockRole.FURNITURE;
    }

    public boolean isRoomWall() {
        return role.isRoomWall();
    }

    public boolean isEmpty() {
        return role == BlockRole.EMPTY;
    }

    public static class BlockBuilder {
        private final int blockId;
        @Nonnull
        private final Vector3i pos;
        @Nonnull
        private BlockRole role = BlockRole.NONE;
        @Nullable
        private ColorLight light = null;

        private boolean lightSet = false;
        private boolean roleSet = false;
        private boolean typeSet = false;
        private boolean findMissingBlockInfoOnBuildEnabled = true;
        private BlockType type = null;

        public BlockBuilder(int blockId, int x, int y, int z) {
            this.blockId = blockId;
            this.pos = new Vector3i(x, y, z);
        }

        public BlockBuilder(int blockId, @Nonnull Vector3i vec) {
            this.blockId = blockId;
            this.pos = vec;
        }

        public Vector3i getPos() {
            return pos;
        }

        public int getX() {
            return pos.x;
        }

        public int getY() {
            return pos.y;
        }

        public int getZ() {
            return pos.z;
        }

        public int getBlockId() {
            return blockId;
        }

        public BlockRole setAndGetRole() {
            BlockType type = this.getAndSetBlockTypeIfNull();
            this.setRole(BlockRole.getRole(blockId, type));
            return this.role;
        }

        public BlockBuilder setRole(@Nonnull BlockRole role) {
            this.roleSet = true;
            this.role = role;
            return this;
        }

        public BlockRole getRole() {
            return role;
        }

        public BlockBuilder setLight(ColorLight light) {
            this.lightSet = true;
            this.light = light;
            return this;
        }

        public ColorLight getLight() {
            return light;
        }

        public BlockBuilder disableFindBlockInfoOnBuild() {
            this.findMissingBlockInfoOnBuildEnabled = false;
            return this;
        }

        public boolean isFindMissingBlockInfoOnBuildEnabled() {
            return findMissingBlockInfoOnBuildEnabled;
        }

        public BlockType getAndSetBlockTypeIfNull() {
            if (type == null) return this.setAndGetBlockType();
            else return type;
        }

        public BlockType setAndGetBlockType() {
            this.setBlockType(BlockType.getAssetMap().getAsset(blockId));
            return this.type;
        }

        public BlockBuilder setBlockType(BlockType type) {
            this.typeSet = true;
            this.type = type;
            return this;
        }

        public BlockType getBlockType() {
            return type;
        }

        public Block build() {
            if (findMissingBlockInfoOnBuildEnabled) {
                if (type == null && !typeSet) this.type = BlockType.getAssetMap().getAsset(blockId);
                if (type != null) {
                    if (!roleSet) this.role = BlockRole.getRole(blockId, type);
                    if (!lightSet) this.light = type.getLight();
                }
            }
            return new Block(blockId, pos, role, light);
        }
    }
}
