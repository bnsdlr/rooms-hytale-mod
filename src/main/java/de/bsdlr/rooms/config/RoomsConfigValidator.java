package de.bsdlr.rooms.config;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public class RoomsConfigValidator implements Validator<RoomsConfig> {
    @Override
    public void accept(RoomsConfig roomsConfig, ValidationResults results) {
        if (roomsConfig.getMinRoomHeight() < 2) results.fail("MinRoomHeight needs to be at least 2.");
        if (roomsConfig.getMaxRoomHeight() < roomsConfig.getMinRoomHeight()) results.fail("MaxRoomHeight needs to be at least MinRoomHeight.");
        if (roomsConfig.getBoundScanRadius().x <= 0) results.fail("BoundScanRadius x should at least be 1.");
        if (roomsConfig.getBoundScanRadius().y <= 0) results.fail("BoundScanRadius y should at least be 1.");
        if (roomsConfig.getBoundScanRadius().z <= 0) results.fail("BoundScanRadius z should at least be 1.");
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema schema) {
    }
}
