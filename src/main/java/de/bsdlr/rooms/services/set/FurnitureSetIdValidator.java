package de.bsdlr.rooms.services.set;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public class FurnitureSetIdValidator implements Validator<String[]> {
    @Override
    public void accept(String[] ids, ValidationResults results) {
        for (String id : ids) {
            if (FurnitureSetType.getAssetMap().getIndex(id) == Integer.MIN_VALUE) {
                results.fail("Invalid SetId: \"" + id + "\"");
            }
        }
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema template) {

    }
}
