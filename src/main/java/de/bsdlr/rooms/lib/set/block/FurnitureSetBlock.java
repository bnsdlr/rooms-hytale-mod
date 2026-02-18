//package de.bsdlr.rooms.lib.set.block;
//
//import com.hypixel.hytale.codec.Codec;
//import com.hypixel.hytale.codec.KeyedCodec;
//import com.hypixel.hytale.codec.builder.BuilderCodec;
//import com.hypixel.hytale.math.vector.Vector3i;
//import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
//
//import javax.annotation.Nonnull;
//import java.util.Objects;
//
//public class FurnitureSetBlock {
//    public static final BuilderCodec<FurnitureSetBlock> CODEC = BuilderCodec.builder(FurnitureSetBlock.class, FurnitureSetBlock::new)
//            .append(new KeyedCodec<>("BlockId", Codec.INTEGER, true),
//                    ((furnitureSetBlock, s) -> furnitureSetBlock.blockId = s),
//                    (furnitureSetBlock -> furnitureSetBlock.blockId)).add()
//            .append(new KeyedCodec<>("Offset", Vector3i.CODEC, true),
//                    ((furnitureSetBlock, s) -> furnitureSetBlock.offset = s),
//                    (furnitureSetBlock -> furnitureSetBlock.offset)).add()
//            .build();
//
//    protected int blockId;
//    @Nonnull
//    protected Vector3i offset;
//
//    FurnitureSetBlock() {
//        this.blockId = 0;
//        this.offset = new Vector3i();
//    }
//
//    public FurnitureSetBlock(FurnitureSetBlockType furnitureSetBlockType) {
//        this.blockId = BlockType.getAssetMap().getIndex(furnitureSetBlockType.getBlockId());
//        this.offset = furnitureSetBlockType.getOffset();
//    }
//
//    public FurnitureSetBlock(int blockId, @Nonnull Vector3i offset) {
//        this.blockId = blockId;
//        this.offset = offset;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("FurnitureSetBlock{");
//        builder.append("blockId=");
//        builder.append(blockId);
//        builder.append(",offset=");
//        builder.append(offset);
//        builder.append("}");
//        return builder.toString();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) return false;
//        if (obj == this) return true;
//        if (getClass() != obj.getClass()) return false;
//
//        FurnitureSetBlock o = (FurnitureSetBlock) obj;
//
//        return this.blockId == o.blockId
//                && this.offset.equals(o.offset);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = 1;
//        result = 31 * result + Objects.hashCode(this.blockId);
//        result = 31 * result + Objects.hashCode(this.offset);
//        return result;
//    }
//    public int getBlockId() {
//        return blockId;
//    }
//
//    public @Nonnull Vector3i getOffset() {
//        return offset;
//    }
//
//    public int getOffsetX() {
//        return offset.x;
//    }
//
//    public int getOffsetY() {
//        return offset.y;
//    }
//
//    public int getOffsetZ() {
//        return offset.z;
//    }
//}
