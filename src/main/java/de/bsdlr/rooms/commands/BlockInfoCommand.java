package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Hitbox;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.TargetUtil;
import de.bsdlr.rooms.lib.room.block.RoomBlockRole;
import de.bsdlr.rooms.lib.set.FurnitureSet;
import de.bsdlr.rooms.lib.set.FurnitureSetDetector;
import de.bsdlr.rooms.lib.set.FurnitureSetType;
import de.bsdlr.rooms.utils.ChunkManager;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;

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
        int rotationIndex = chunk.getChunkAccessor().getBlockRotationIndex(pos.x, pos.y, pos.z);


//        chunk.getChunkAccessor().get

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
        ctx.sendMessage(Message.raw("textures: " + Arrays.toString(type.getTextures())));
        ctx.sendMessage(Message.raw("is solid block: " + RoomBlockRole.isSolidBlock(type)));
        ctx.sendMessage(Message.raw("is furniture: " + RoomBlockRole.isFurniture(type)));
        ctx.sendMessage(Message.raw("is entrance: " + RoomBlockRole.isEntrance(type)));
        ctx.sendMessage(Message.raw("is window: " + RoomBlockRole.isWindow(type)));
        ctx.sendMessage(Message.raw("is room wall block: " + RoomBlockRole.isRoomWallBlock(type)));
        ctx.sendMessage(Message.raw("is room wall: " + RoomBlockRole.isRoomWall(type)));
        ctx.sendMessage(Message.raw("block role: " + RoomBlockRole.getRole(type)));

//        Holder<ChunkStore> holder = chunk.getBlockComponentHolder(pos.x, pos.y, pos.z);
//        if (holder != null) {
//            BlockSection section = holder.getComponent(BlockSection.getComponentType());
//            if (section != null) {
//                int fillerKey = section.getFiller(pos.x, pos.y, pos.z);
//                Vector3i filler = new Vector3i(FillerBlockUtil.unpackX(fillerKey), FillerBlockUtil.unpackY(fillerKey), FillerBlockUtil.unpackZ(fillerKey));
//                ctx.sendMessage(Message.raw(String.format("filler: %d %d %d", filler.x, filler.y, filler.z)));
//            } else {
//                ctx.sendMessage(Message.raw("holder: section is null").color(Color.RED));
//            }
//        } else {
//            ctx.sendMessage(Message.raw("holder is null").color(Color.RED));
//        }

        int chunkX = chunk.getX();
        int chunkY = ChunkUtil.indexSection(pos.y);
        int chunkZ = chunk.getZ();
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReference(chunkX, chunkY, chunkZ);
        if (sectionRef != null && sectionRef.isValid()) {
            Store<ChunkStore> cStore = chunkStore.getStore();
            BlockSection section = cStore.getComponent(sectionRef, BlockSection.getComponentType());
            if (section != null) {
                int fillerKey = section.getFiller(pos.x, pos.y, pos.z);
                Vector3i filler = new Vector3i(FillerBlockUtil.unpackX(fillerKey), FillerBlockUtil.unpackY(fillerKey), FillerBlockUtil.unpackZ(fillerKey));
                ctx.sendMessage(Message.raw(String.format("filler: %d %d %d", filler.x, filler.y, filler.z)));
            } else {
                ctx.sendMessage(Message.raw("section is null").color(Color.RED));
            }
        } else {
            ctx.sendMessage(Message.raw("section ref is null and or invalid").color(Color.RED));
        }

//        Ref<ChunkStore> blockRef = chunk.getBlockComponentEntity(pos.x, pos.y, pos.z);
//        if (blockRef != null && blockRef.isValid()) {
//            BlockComponentChunk blockComponentChunk = chunk.getBlockComponentChunk();
//            if (blockComponentChunk != null) {
//                BlockSection section = blockComponentChunk.getComponent(blockRef.getIndex(), BlockSection.getComponentType());
//                if (section != null) {
//                    int fillerKey = section.getFiller(pos.x, pos.y, pos.z);
//                    Vector3i filler = new Vector3i(FillerBlockUtil.unpackX(fillerKey), FillerBlockUtil.unpackY(fillerKey), FillerBlockUtil.unpackZ(fillerKey));
//                    ctx.sendMessage(Message.raw(String.format("filler: %d %d %d", filler.x, filler.y, filler.z)));
//                } else {
//                    ctx.sendMessage(Message.raw("section is null").color(Color.RED));
//                }
//            } else {
//                ctx.sendMessage(Message.raw("block component chunk is null").color(Color.RED));
//            }
//        } else {
//            ctx.sendMessage(Message.raw("block ref is null and or invalid").color(Color.RED));
//        }

        ChunkManager chunkManager = new ChunkManager(world);

        int counter = 0;

        for (FurnitureSetType furnitureSetType : FurnitureSetType.getAssetStore().getAssetMap().getAssetMap().values()) {
            FurnitureSet furnitureSet = FurnitureSetDetector.getFurnitureSetAt(chunkManager, furnitureSetType, pos.x, pos.y, pos.z);
            if (furnitureSet != null) {
                ctx.sendMessage(Message.raw("Block is part of furniture set: " + furnitureSetType.getId()));
                counter++;
            }
        }

        if (counter == 0) {
            ctx.sendMessage(Message.raw("Block is not part of a furniture set."));
        }

        BlockBoundingBoxes bbb = BlockBoundingBoxes.getAssetMap().getAsset(type.getHitboxTypeIndex());
        if (bbb == null) return;

        BlockBoundingBoxes.RotatedVariantBoxes rotatedHitbox = bbb.get(rotationIndex);
        Box bb = rotatedHitbox.getBoundingBox();
        Vector3d center = new Vector3d(bb.middleX(), bb.middleY(), bb.middleZ());

        ctx.sendMessage(Message.raw(String.format("box: min: %.2f %.2f %.2f; max: %.2f %.2f %.2f; contains: %s", bb.min.x, bb.min.y, bb.min.z, bb.max.x, bb.max.y, bb.max.z, bb.containsBlock(-1, 1, 0))));
        ctx.sendMessage(Message.raw(String.format("box center: %.2f %.2f %.2f", center.x, center.y, center.z)));

//        Hitbox box = new Hitbox();
//
//        for (Hitbox b : bbb.toPacket()) {
//            ctx.sendMessage(Message.raw(String.format("hitbox: min: %f %f %f; max: %f %f %f", b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ)));
//            if (b.minX < box.minX) box.minX = b.minX < 0.0 ? (float) Math.floor(b.minX) : (float) Math.ceil(b.minX);
//            if (b.minY < box.minY) box.minY = b.minY < 0.0 ? (float) Math.floor(b.minY) : (float) Math.ceil(b.minY);
//            if (b.minZ < box.minZ) box.minZ = b.minZ < 0.0 ? (float) Math.floor(b.minZ) : (float) Math.ceil(b.minZ);
//
//            if (b.maxX > box.maxX) box.maxX = b.maxX < 0.0 ? (float) Math.floor(b.maxX) : (float) Math.ceil(b.maxX);
//            if (b.maxY > box.maxY) box.maxY = b.maxY < 0.0 ? (float) Math.floor(b.maxY) : (float) Math.ceil(b.maxY);
//            if (b.maxZ > box.maxZ) box.maxZ = b.maxZ < 0.0 ? (float) Math.floor(b.maxZ) : (float) Math.ceil(b.maxZ);
//            ctx.sendMessage(Message.raw(String.format("final hitbox: min: %f %f %f; max: %f %f %f", box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)).color(Color.CYAN));
//        }
    }
}
