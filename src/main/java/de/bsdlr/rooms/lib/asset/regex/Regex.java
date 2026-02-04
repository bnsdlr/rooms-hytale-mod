package de.bsdlr.rooms.lib.asset.regex;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Regex {
    public static final BuilderCodec<Regex> CODEC = BuilderCodec.builder(Regex.class, Regex::new)
            .appendInherited(new KeyedCodec<>("Pattern", Codec.STRING),
                    ((regex, s) -> regex.pattern = s),
                    (regex -> regex.pattern),
                    ((regex, parent) -> regex.pattern = parent.pattern))
            .add()
            .appendInherited(new KeyedCodec<>("Flags", new SetCodec<>(RegexFlag.CODEC, HashSet::new, true)),
                    ((regex, s) -> regex.flags = s),
                    (regex -> regex.flags),
                    ((regex, parent) -> regex.flags = parent.flags))
            .add()
            .build();
    protected String pattern;
    protected Set<RegexFlag> flags;

    private Pattern compiledPattern;

    Regex() {
    }

    public Regex(String pattern, Set<RegexFlag> flags) {
        this.pattern = pattern;
        this.flags = flags;
    }

    public int getFlagMask() {
        if (flags == null) return 0;
        int mask = 0;
        for (RegexFlag flag : flags) {
            mask |= flag.mode;
        }
        return mask;
    }

    public Set<RegexFlag> getFlags() {
        return flags;
    }

    public String getPattern() {
        return pattern;
    }

    public Pattern getCompiledPattern() {
        if (compiledPattern == null) compiledPattern = Pattern.compile(pattern, getFlagMask());
        return compiledPattern;
    }
}
