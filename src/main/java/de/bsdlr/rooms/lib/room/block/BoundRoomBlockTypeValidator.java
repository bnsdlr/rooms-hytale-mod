package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

import java.util.function.BiConsumer;

public class BoundRoomBlockTypeValidator implements Validator<BoundRoomBlockType> {
    public BoundRoomBlockTypeValidator() {
    }

    @Override
    public void accept(BoundRoomBlockType boundRoomBlockType, ValidationResults results) {
        if (boundRoomBlockType == null) return;
        if (boundRoomBlockType.getMinPercentage() > boundRoomBlockType.getMaxPercentage()) {
            results.fail("MinPercentage has to be smaller than MaxPercentage.");
        }
        if (boundRoomBlockType.getMinCount() > boundRoomBlockType.getMaxCount()) {
            results.fail("MinCount has to be smaller than MaxCount.");
        }
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema schema) {
    }
}
