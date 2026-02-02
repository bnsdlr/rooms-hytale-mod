package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;

import javax.annotation.Nonnull;
import java.util.Map;

public class RoomsConfigCommand extends AbstractPlayerCommand {
    private RequiredArg<Vector3i> scanRadiusArg;

    public RoomsConfigCommand() {
        super("roomsset", "Allows you to change the scan radius");
        this.addAliases("rs");
        this.scanRadiusArg = withRequiredArg("scanradius", "desc", ArgTypes.VECTOR3I);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Vector3i newScanRadius = ctx.get(scanRadiusArg);
        RoomsPlugin.get().getConfig().get().setScanRadius(newScanRadius);
        ctx.sendMessage(Message.raw("new scan radius: " + newScanRadius));
    }
}
