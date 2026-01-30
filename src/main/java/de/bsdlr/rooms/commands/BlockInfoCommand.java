package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import de.bsdlr.rooms.room.block.BlockRole;

import javax.annotation.Nonnull;

public class BlockInfoCommand extends AbstractPlayerCommand {

    public BlockInfoCommand() {
        super("blockinfo", "Prints out block info for the target block");
        this.addAliases("bi", "info", "i");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Vector3i pos = TargetUtil.getTargetBlock(ref, 10.0, store);

        if (pos == null) {
            ctx.sendMessage(Message.raw("no block in reach"));
            return;
        }

        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));

        if (chunk == null) {
            ctx.sendMessage(Message.raw("chunk is null"));
            return;
        }

        int blockId = chunk.getBlock(pos);

        BlockType type = BlockType.getAssetMap().getAsset(blockId);

        if (type == null) {
            ctx.sendMessage(Message.raw("block type is null"));
            return;
        }

        ctx.sendMessage(Message.raw("--------------------------------------------"));
        ctx.sendMessage(Message.raw("pos: " + pos.x + " " + pos.y + " " + pos.z));
        ctx.sendMessage(Message.raw("block id: " + blockId));
        ctx.sendMessage(Message.raw("id: " + type.getId()));
        ctx.sendMessage(Message.raw("material: " + type.getMaterial()));
        ctx.sendMessage(Message.raw("light: " + type.getLight()));
        ctx.sendMessage(Message.raw("fully supportive: " + type.isFullySupportive()));
        ctx.sendMessage(Message.raw("hitbox type: " + type.getHitboxType()));
        ctx.sendMessage(Message.raw("can be placed as deco: " + type.canBePlacedAsDeco()));
        ctx.sendMessage(Message.raw("opacity: " + type.getOpacity()));
        ctx.sendMessage(Message.raw("draw type: " + type.getDrawType()));
        ctx.sendMessage(Message.raw("custom model: " + type.getCustomModel()));
        ctx.sendMessage(Message.raw("is solid block: " + BlockRole.isSolidBlock(type)));
        ctx.sendMessage(Message.raw("is furniture: " + BlockRole.isFurniture(type)));
        ctx.sendMessage(Message.raw("is entrance: " + BlockRole.isEntrance(type)));
        ctx.sendMessage(Message.raw("is window: " + BlockRole.isWindow(type)));
        ctx.sendMessage(Message.raw("is room wall: " + BlockRole.isRoomWall(type)));
        ctx.sendMessage(Message.raw("block role: " + BlockRole.getRole(blockId, type)));

//        BlockBoundingBoxes bbb = BlockBoundingBoxes.getAssetMap().getAsset(type.getHitboxTypeIndex());
//        if (bbb == null) return;
//
//        Hitbox box = new Hitbox();
//
//        for (Hitbox b : bbb.toPacket()) {
//            ctx.sendMessage(Message.raw(String.format("hitbox: min: %f %f %f; max: %f %f %f", b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ)));
//            if (b.minX < box.minX) box.minX = (float) Math.ceil(b.minX);
//            if (b.minY < box.minY) box.minY = (float) Math.ceil(b.minY);
//            if (b.minZ < box.minZ) box.minZ = (float) Math.ceil(b.minZ);
//
//            if (b.maxX > box.maxX) box.maxX = (float) Math.ceil(b.maxX);
//            if (b.maxY > box.maxY) box.maxY = (float) Math.ceil(b.maxY);
//            if (b.maxZ > box.maxZ) box.maxZ = (float) Math.ceil(b.maxZ);
//            ctx.sendMessage(Message.raw(String.format("final hitbox: min: %f %f %f; max: %f %f %f", box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)).color(Color.CYAN));
//        }
    }
}
