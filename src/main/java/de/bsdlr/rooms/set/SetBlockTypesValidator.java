package de.bsdlr.rooms.set;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.HashSet;
import java.util.Set;

public class SetBlockTypesValidator implements Validator<SetBlockType[]> {
    private final int minBlocks;

    public SetBlockTypesValidator(int minBlocks) {
        this.minBlocks = minBlocks;
    }

    @Override
    public void accept(SetBlockType[] blockTypes, ValidationResults results) {
        if (blockTypes.length < minBlocks) {
            results.fail("There need to be at least " + minBlocks + " blocks in a Set.");
        }

        Set<Vector3i> offsets = new HashSet<>();

        for (SetBlockType blockType : blockTypes) {
            if (!offsets.add(blockType.getOffset())) {
                results.fail("Blocks in a Set need to have unique offsets.");
                return;
            }
        }
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema target) {
        ArraySchema arr = (ArraySchema)target;
        arr.setMinItems(this.minBlocks);
        arr.setMaxItems(this.minBlocks);
    }
}
