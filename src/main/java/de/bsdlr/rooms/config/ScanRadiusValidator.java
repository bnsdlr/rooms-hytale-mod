package de.bsdlr.rooms.config;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.math.vector.Vector3i;

public class ScanRadiusValidator implements Validator<Vector3i> {
    @Override
    public void accept(Vector3i scanRadius, ValidationResults results) {
        if (scanRadius.x <= 0) results.fail("ScanRadius.x needs to be at lease 1.");
        if (scanRadius.y <= 0) results.fail("ScanRadius.y needs to be at lease 1.");
        if (scanRadius.z <= 0) results.fail("ScanRadius.z needs to be at lease 1.");
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema schema) {
    }
}
