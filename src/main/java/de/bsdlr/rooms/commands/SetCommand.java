package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.RoomsPlugin;

import javax.annotation.Nonnull;

public class SetCommand extends AbstractPlayerCommand {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public SetCommand() {
        super("sets", "...");
        this.addAliases("set");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Vector3d position = playerRef.getTransform().getPosition();
        int x = (position.x < 0) ? (int) Math.floor(position.x) : (int) position.x;
        int y = (int) position.y;
        int z = (int) position.z;

        ctx.sendMessage(Message.raw("at pos: " + x + " " + y + " " + z));

        Vector3i scanRadius = RoomsPlugin.get().getConfig().get().getScanRadius();

//        Set<FurnitureSet> furnitureSets = FurnitureSetDetector.detectFurnitureSetsAround(world, x, y, z, scanRadius);
//        ctx.sendMessage(Message.raw("found " + furnitureSets.size() + " sets."));
//        for (FurnitureSet furnitureSet : furnitureSets) {
//            LOGGER.atInfo().log(furnitureSet.toString());
//        }
    }
}
