package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;
import de.bsdlr.rooms.utils.PositionUtils;

import javax.annotation.Nonnull;
import java.util.Map;

public class RoomsConfigCommand extends AbstractPlayerCommand {
    private OptionalArg<Vector3i> scanRadiusArg;
    private OptionalArg<String> placeBlockArg;

    public RoomsConfigCommand() {
        super("roomsset", "Allows you to change the scan radius");
        this.addAliases("rs");
        this.scanRadiusArg = withOptionalArg("r", "desc", ArgTypes.VECTOR3I);
        this.placeBlockArg = withOptionalArg("pb", "places blocks, if provided", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Vector3i newScanRadius = ctx.get(scanRadiusArg);
        if (newScanRadius != null) {
            RoomsPlugin.get().getConfig().get().setScanRadius(newScanRadius);
            ctx.sendMessage(Message.raw("new scan radius: " + newScanRadius));
        } else {
            newScanRadius = RoomsPlugin.get().getConfig().get().getScanRadius();
        }

        Vector3i pos = PositionUtils.positionToVector3i(playerRef.getTransform().getPosition());

        String blockId = ctx.get(placeBlockArg);

        if (blockId != null) {
            PositionUtils.forOffsetInRadius(newScanRadius, (dx, dy, dz) -> {
                int bx = dx + pos.x;
                int by = dy + pos.y;
                int bz = dz + pos.z;
                world.setBlock(bx, by, bz, blockId);
                return null;
            });
        }
    }
}
