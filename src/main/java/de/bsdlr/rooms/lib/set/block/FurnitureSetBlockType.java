//package de.bsdlr.rooms.lib.set.block;
//
//import com.hypixel.hytale.codec.Codec;
//import com.hypixel.hytale.codec.KeyedCodec;
//import com.hypixel.hytale.codec.builder.BuilderCodec;
//import com.hypixel.hytale.math.vector.Vector3i;
//import de.bsdlr.rooms.utils.BlockUtils;
//
//import javax.annotation.Nonnull;
//
//public class FurnitureSetBlockType {
//    public static final BuilderCodec<FurnitureSetBlockType> CODEC = BuilderCodec.builder(FurnitureSetBlockType.class, FurnitureSetBlockType::new)
//            .appendInherited(new KeyedCodec<>("BlockId", Codec.STRING),
//                    ((setBlock, s) -> setBlock.blockId = s),
//                    (furnitureSetBlockType -> furnitureSetBlockType.blockId),
//                    ((furnitureSetBlockType, parent) -> furnitureSetBlockType.blockId = parent.blockId)
//            )
//            .addValidator(BlockUtils.BLOCK_ASSET_KEY_VALIDATOR)
//            .add()
//            .appendInherited(new KeyedCodec<>("Offset", Vector3i.CODEC),
//                    ((furnitureSetBlockType, s) -> furnitureSetBlockType.offset = s),
//                    (furnitureSetBlockType -> furnitureSetBlockType.offset),
//                    ((furnitureSetBlockType, parent) -> furnitureSetBlockType.offset = parent.offset)
//            )
//            .add()
//            .build();
//    @Nonnull
//    protected String blockId = "Empty";
//    @Nonnull
//    protected Vector3i offset = new Vector3i();
//
//    public FurnitureSetBlockType() {
//    }
//
//    public FurnitureSetBlockType(@Nonnull FurnitureSetBlockType other) {
//        this.blockId = other.blockId;
//        this.offset = other.offset;
////        this.rotation = other.rotation;
//    }
//
//    @Nonnull
//    public String getBlockId() {
//        return blockId;
//    }
//
//    @Nonnull
//    public Vector3i getOffset() {
//        return offset;
//    }
//
//    public int getXOffset() {
//        return offset.x;
//    }
//
//    public int getYOffset() {
//        return offset.y;
//    }
//
//    public int getZOffset() {
//        return offset.z;
//    }
//
////    public Rotation getRotation() {
////        return rotation;
////    }
//}
