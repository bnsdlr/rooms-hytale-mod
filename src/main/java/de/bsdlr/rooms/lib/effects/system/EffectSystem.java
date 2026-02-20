package de.bsdlr.rooms.lib.effects.system;

public abstract class EffectSystem<EffectType> {
    public abstract void handle(
            EffectType effectType
    );
}
