package de.bsdlr.rooms.lib.effects;

import javax.annotation.Nonnull;

public interface Effect {
    @Nonnull
    Effect clone();
}
