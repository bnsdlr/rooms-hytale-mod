package de.bsdlr.rooms.services.set.block;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import java.util.*;

public class FurnitureSetBlockTypesValidator implements Validator<FurnitureSetBlockType[]> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final int minBlocks;
    private final int maxSpaceBetween;

    public FurnitureSetBlockTypesValidator(int minBlocks, int maxSpaceBetween) {
        this.minBlocks = minBlocks;
        this.maxSpaceBetween = maxSpaceBetween;
    }

    @Override
    public void accept(FurnitureSetBlockType[] blockTypes, ValidationResults results) {
        if (blockTypes.length < minBlocks) {
            results.fail("There need to be at least " + minBlocks + " blocks in a FurnitureSet.");
        }

        Set<Vector3i> offsets = new HashSet<>();

        for (FurnitureSetBlockType blockType : blockTypes) {
            if (!offsets.add(blockType.getOffset())) {
                results.fail("Blocks in a FurnitureSet need to have unique offsets.");
            }
            int index = BlockType.getAssetMap().getIndex(blockType.blockId);
            if (index == Integer.MIN_VALUE) {
                results.fail("Unknown block id: \"" + blockType.blockId + "\"");
            }
        }

        if (maxSpaceBetween(offsets) > maxSpaceBetween) {
            results.fail("Blocks should not be further apart then " + maxSpaceBetween + " blocks.");
        }
    }

    private int maxSpaceBetween(Collection<Vector3i> offsets) {
        Set<Integer> xs = new HashSet<>();
        Set<Integer> ys = new HashSet<>();
        Set<Integer> zs = new HashSet<>();

        LOGGER.atInfo().log("offsets: " + offsets);

        for (Vector3i offset : offsets) {
            xs.add(offset.x);
            ys.add(offset.y);
            zs.add(offset.z);
        }

        int xDiff = 0;
        int yDiff = 0;
        int zDiff = 0;

        if (!xs.isEmpty()) {
            Integer min = Collections.min(xs);
            Integer max = Collections.max(xs);

            xDiff = max - min;
        }

        if (!ys.isEmpty()) {
            Integer min = Collections.min(ys);
            Integer max = Collections.max(ys);

            yDiff = max - min;
        }

        if (!zs.isEmpty()) {
            Integer min = Collections.min(zs);
            Integer max = Collections.max(zs);

            zDiff = max - min;
        }

        return Math.max(Math.max(xDiff, yDiff), zDiff);
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema target) {
        ArraySchema arr = (ArraySchema)target;
        arr.setMinItems(this.minBlocks);
        arr.setMaxItems(this.minBlocks);
    }
}
