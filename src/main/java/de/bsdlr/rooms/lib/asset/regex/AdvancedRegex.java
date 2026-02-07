package de.bsdlr.rooms.lib.asset.regex;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.codec.validation.Validators;

import javax.annotation.Nonnull;
import javax.xml.validation.Validator;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedRegex {
    public static final BuilderCodec<AdvancedRegex> CODEC = BuilderCodec.builder(AdvancedRegex.class, AdvancedRegex::new)
            .appendInherited(new KeyedCodec<>("Include", Regex.CODEC),
                    ((advancedRegex, s) -> advancedRegex.include = s),
                    (advancedRegex -> advancedRegex.include),
                    ((advancedRegex, parent) -> advancedRegex.include = parent.include))
            .addValidator(Validators.nonNull())
            .addValidator(new RegexValidator())
            .add()
            .appendInherited(new KeyedCodec<>("Exclude", Regex.CODEC),
                    ((advancedRegex, s) -> advancedRegex.exclude = s),
                    (advancedRegex -> advancedRegex.exclude),
                    ((advancedRegex, parent) -> advancedRegex.exclude = parent.exclude))
            .add()
            .build();
    protected Regex include;
    protected Regex exclude;

    protected AdvancedRegex() {
    }

    public AdvancedRegex(@Nonnull AdvancedRegex other) {
        this.include = other.include;
        this.exclude = other.exclude;
    }

    public Regex getInclude() {
        return include;
    }

    public Regex getExclude() {
        return exclude;
    }

    public boolean matches(String s) {
        Pattern includePattern = include.getCompiledPattern();
        Pattern excludePattern = exclude.getCompiledPattern();

        Matcher includeMatcher = includePattern.matcher(s);
        Matcher excludeMatcher = excludePattern.matcher(s);

        return includeMatcher.matches() && !excludeMatcher.matches();
    }
}
