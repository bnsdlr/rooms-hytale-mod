package de.bsdlr.rooms.lib.set;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import de.bsdlr.rooms.lib.set.block.FurnitureSetBlock;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

public class FurnitureSet {
    public static final BuilderCodec<FurnitureSet> CODEC = BuilderCodec.builder(FurnitureSet.class, FurnitureSet::new)
            .appendInherited(new KeyedCodec<>("FurnitureSetId", Codec.STRING),
                    ((furnitureSet, s) -> furnitureSet.furnitureSetId = s),
                    (furnitureSet -> furnitureSet.furnitureSetId),
                    ((furnitureSet, parent) -> furnitureSet.furnitureSetId = parent.furnitureSetId))
            .add()
            .appendInherited(new KeyedCodec<>("Position", Vector3i.CODEC),
                    ((furnitureSet, s) -> furnitureSet.pos = s),
                    (furnitureSet -> furnitureSet.pos),
                    ((furnitureSet, parent) -> furnitureSet.pos = parent.pos))
            .add()
            .appendInherited(new KeyedCodec<>("Blocks", new ArrayCodec<>(FurnitureSetBlock.CODEC, FurnitureSetBlock[]::new)),
                    ((furnitureSet, s) -> furnitureSet.blocks = s),
                    (furnitureSet -> furnitureSet.blocks),
                    ((furnitureSet, parent) -> furnitureSet.blocks = parent.blocks))
            .add()
            .build();
    protected String furnitureSetId;
    @Nonnull
    protected Vector3i pos = new Vector3i();
    protected FurnitureSetBlock[] blocks;

    FurnitureSet() {
    }

    public FurnitureSet(FurnitureSetType furnitureSetType, @Nonnull Vector3i pos) {
        this.furnitureSetId = furnitureSetType.id;
        this.pos = pos;
        this.blocks = Arrays.stream(furnitureSetType.getFurnitureSetBlockTypes()).map(FurnitureSetBlock::new).toArray(FurnitureSetBlock[]::new);
    }

    public FurnitureSet(String furnitureSetId, @Nonnull Vector3i pos, FurnitureSetBlock[] blocks) {
        this.furnitureSetId = furnitureSetId;
        this.pos = pos;
        this.blocks = blocks;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FurnitureSet{");
        builder.append("furnitureSetId=");
        builder.append(furnitureSetId);
        builder.append(",pos=");
        builder.append(pos);
        builder.append(",blocks=");
        builder.append(Arrays.toString(blocks));
        builder.append("}");
        return builder.toString();
    }

    public String getFurnitureSetId() {
        return furnitureSetId;
    }

    @Nonnull
    public Vector3i getPos() {
        return pos;
    }

    public FurnitureSetBlock[] getBlocks() {
        return blocks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!obj.getClass().equals(getClass())) return false;
        FurnitureSet other = (FurnitureSet) obj;

        return furnitureSetId.equals(other.furnitureSetId)
                && pos.equals(other.pos)
                && Arrays.equals(blocks, other.blocks);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(furnitureSetId);
        result = 31 * result + Objects.hashCode(pos);
        result = 31 * result + Arrays.hashCode(blocks);
        return result;
    }
}
