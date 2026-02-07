package de.bsdlr.rooms.lib.asset.score;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class Score {
    public static final BuilderCodec<Score> CODEC = BuilderCodec.builder(Score.class, Score::new)
            .appendInherited(new KeyedCodec<>("Multiplier", Codec.DOUBLE),
                    (score, s) -> score.multiplier = s,
                    score -> score.multiplier,
                    (score, parent) -> score.multiplier = parent.multiplier)
            .add()
            .appendInherited(new KeyedCodec<>("AddBefore", Codec.INTEGER),
                    (score, s) -> score.addBefore = s,
                    score -> score.addBefore,
                    (score, parent) -> score.addBefore = parent.addBefore)
            .documentation("Score to add before multiplying.")
            .add()
            .appendInherited(new KeyedCodec<>("AddAfter", Codec.INTEGER),
                    (score, s) -> score.addAfter = s,
                    score -> score.addAfter,
                    (score, parent) -> score.addAfter = parent.addAfter)
            .add()
            .documentation("Score to add after multiplying.")
            .build();
    public static final Score DEFAULT = new Score();
    protected double multiplier = 1.0;
    protected int addBefore = 0;
    protected int addAfter = 0;

    public Score() {}

    public int getAddAfter() {
        return addAfter;
    }

    public int getAddBefore() {
        return addBefore;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
