package de.bsdlr.rooms.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.bsdlr.rooms.ui.RoomUI;

import javax.annotation.Nonnull;

public class TestUICommand extends AbstractPlayerCommand {
    public TestUICommand() {
        super("testui", "...");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        if (ctx.sender() instanceof Player) {
            Player player = ctx.senderAs(Player.class);

            if (player.getHudManager().getCustomHud() == null) {
                player.getHudManager().setCustomHud(playerRef, new RoomUI(playerRef));
                ctx.sendMessage(Message.raw("Room ui visible"));
            } else {
                player.getHudManager().resetHud(playerRef);
                ctx.sendMessage(Message.raw("Room ui hidden"));
            }
        }
    }
}
