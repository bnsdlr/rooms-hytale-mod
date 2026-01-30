package de.bsdlr.rooms.commands;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class TestCommand extends AbstractCommand {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public TestCommand() {
        super("test", "...");
    }

    @Override
    protected @Nullable CompletableFuture<Void> execute(@Nonnull CommandContext ctx) {
//        LOGGER.atInfo().log("room types: " + Arrays.toString(RoomType.getAssetMap()));
        return null;
    }
}
