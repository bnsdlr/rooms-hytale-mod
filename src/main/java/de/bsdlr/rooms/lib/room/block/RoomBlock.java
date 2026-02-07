package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import de.bsdlr.rooms.utils.BlockUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

public class RoomBlock {
//    public static final BuilderCodec<RoomBlock> CODEC = BuilderCodec.builder(RoomBlock.class, RoomBlock::new)
//            .append(new KeyedCodec<>("BlockId", , true),
//                    RoomBlock::setBlockId,
//                    RoomBlock::getBlockId).add()
//            .append(new KeyedCodec<>("Position", Vector3i.CODEC, true),
//                    RoomBlock::setPos,
//                    RoomBlock::getPos).add()
//            .append(new KeyedCodec<>("Role", new EnumCodec<>(RoomBlockRole.class), true),
//                    RoomBlock::setRole,
//                    RoomBlock::getRole).add()
//            .build();

    @Nonnull
    private BlockType type;
    @Nonnull
    private Vector3i pos;
    @Nonnull
    private RoomBlockRole role;
    private final boolean filler;

    RoomBlock() {
        this.type = BlockType.EMPTY;
        this.pos = new Vector3i();
        this.role = RoomBlockRole.UNKNOWN;
        this.filler = false;
    }

    public RoomBlock(BlockType type, @Nonnull Vector3i pos, ChunkStore chunkStore) {
        this.type = type == null ? BlockType.UNKNOWN : type;
        this.pos = pos;
        this.role = RoomBlockRole.getRole(type);
        this.filler = BlockUtils.isFiller(chunkStore, pos.x, pos.y, pos.z);
    }

    public RoomBlock(@Nonnull BlockType type, @Nonnull Vector3i pos, @Nonnull RoomBlockRole role, boolean filler) {
        this.type = type;
        this.pos = pos;
        this.role = role;
        this.filler = filler;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (getClass() != obj.getClass()) return false;

        RoomBlock o = (RoomBlock) obj;

        return this.type.equals(o.type)
                && this.pos.equals(o.pos)
                && this.role.equals(o.role);

    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(this.type);
        result = 31 * result + Objects.hashCode(this.pos);
        result = 31 * result + Objects.hashCode(this.role);
        return result;
    }

    private void setType(@Nonnull BlockType type) {
        this.type = type;
    }

    @Nonnull
    public BlockType getType() {
        return type;
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

    private void setRole(@Nonnull RoomBlockRole role) {
        this.role = role;
    }

    public RoomBlockRole getRole() {
        return role;
    }

    public boolean isFiller() {
        return filler;
    }

    public static class Builder {
        private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
        @Nonnull
        private final BlockType type;
        @Nonnull
        private final Vector3i pos;
        @Nonnull
        private final RoomBlockRole role;
        private Vector3i filler = null;

        public Builder(@Nonnull BlockType type, @Nonnull Vector3i vec) {
            this.type = type;
            this.pos = vec;
            this.role = RoomBlockRole.getRole(type);
        }

        public Builder setFiller(ChunkStore chunkStore) {
            this.filler = BlockUtils.getFiller(chunkStore, pos.x, pos.y, pos.z);
            return this;
        }

        public RoomBlock build() {
            return new RoomBlock(type, pos, role, BlockUtils.isFiller(filler));
        }
    }
}
