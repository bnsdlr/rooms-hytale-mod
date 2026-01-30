package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class RotateCommand extends AbstractPlayerCommand {
    private RequiredArg<Vector3i> toArg;
    private FlagArg isRotationFlag;

    public RotateCommand() {
        super("rotatetoblock", "...");
        this.addAliases("rtb");
        this.toArg = this.withRequiredArg("to", "block to rotate to", ArgTypes.VECTOR3I);
        this.isRotationFlag = this.withFlagArg("isrotation", ".");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Vector3i target = ctx.get(toArg);
        boolean isRotation = ctx.get(isRotationFlag);

        Vector3f normalized = new Vector3f(target.x, target.y, target.z);

        if (isRotation) {
            Vector3d position = playerRef.getTransform().getPosition();

            int dx = (int) (target.x - position.x);
            int dy = (int) (target.y - position.y);
            int dz = (int) (target.z - position.z);

            float magnitude = (float) Math.sqrt(Math.powExact(dx, 2) + Math.powExact(dy, 2) + Math.powExact(dz, 2));

            normalized = new Vector3f(dx / magnitude, dy / magnitude, dz / magnitude);
        }

        HeadRotation headRotation = new HeadRotation(normalized);

        store.addComponent(ref, HeadRotation.getComponentType(), headRotation);
    }
}
