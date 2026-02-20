package de.bsdlr.rooms.lib.effects.room;

import de.bsdlr.rooms.lib.effects.Effect;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SleepEffect implements Effect {
    public SleepEffect() {}

    @NonNullDecl
    @Override
    public Effect clone() {
        return new SleepEffect();
    }
}
