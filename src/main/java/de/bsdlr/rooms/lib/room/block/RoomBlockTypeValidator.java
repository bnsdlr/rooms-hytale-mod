package de.bsdlr.rooms.lib.room.block;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class RoomBlockTypeValidator implements Validator<RoomBlockType> {
    public RoomBlockTypeValidator() {
    }

    @Override
    public void accept(RoomBlockType roomBlockType, ValidationResults results) {
        if (roomBlockType == null) return;
        if (roomBlockType.getMinCount() > roomBlockType.getMaxCount()) {
            results.fail("MinCount has to be smaller than MaxCount.");
        }
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema schema) {
    }
}
