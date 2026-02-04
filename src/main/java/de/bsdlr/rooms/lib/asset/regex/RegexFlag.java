package de.bsdlr.rooms.lib.asset.regex;

import com.hypixel.hytale.codec.codecs.EnumCodec;

import java.util.regex.Pattern;

public enum RegexFlag {
    UNIX_LINES(Pattern.UNIX_LINES),
    CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),
    COMMENTS(Pattern.COMMENTS),
    MULTILINE(Pattern.MULTILINE),
    LITERAL(Pattern.LITERAL),
    DOTALL(Pattern.DOTALL),
    UNICODE_CASE(Pattern.UNICODE_CASE),
    CANON_EQ(Pattern.CANON_EQ),
    UNICODE_CHARACTER_CLASS(Pattern.UNICODE_CHARACTER_CLASS);

    public static final EnumCodec<RegexFlag> CODEC = new EnumCodec<>(RegexFlag.class)
            .documentKey(
                    RegexFlag.UNIX_LINES,
                    """
                    Enables Unix lines mode.
                    
                    Only `\\n` is recognized as a line terminator for `.`, `^`, and `$`.
                    Embedded flag: `(?d)`"""
            )
            .documentKey(
                    RegexFlag.CASE_INSENSITIVE,
                    """
                    Enables case-insensitive matching.
                    
                    By default, matching is ASCII-only. Use with `UNICODE_CASE` for
                    Unicode-aware matching.
                    Embedded flag: `(?i)`"""
            )
            .documentKey(
                    RegexFlag.COMMENTS,
                    """
                    Permits whitespace and comments in the pattern.
                    
                    Whitespace is ignored and `#` starts a comment until end of line.
                    Embedded flag: `(?x)`"""
            )
            .documentKey(
                    RegexFlag.MULTILINE,
                    """
                    Enables multiline mode.
                    
                    `^` and `$` match line boundaries instead of only the input boundaries.
                    Embedded flag: `(?m)`"""
            )
            .documentKey(
                    RegexFlag.LITERAL,
                    """
                    Enables literal parsing of the pattern.
                    
                    All characters are treated literally; metacharacters lose their meaning.
                    No embedded flag exists."""
            )
            .documentKey(
                    RegexFlag.DOTALL,
                    """
                    Enables dotall mode.
                    
                    The `.` character matches line terminators as well.
                    Embedded flag: `(?s)`"""
            )
            .documentKey(
                    RegexFlag.UNICODE_CASE,
                    """
                    Enables Unicode-aware case folding.
                    
                    Makes case-insensitive matching conform to the Unicode standard.
                    Embedded flag: `(?u)`"""
            )
            .documentKey(
                    RegexFlag.CANON_EQ,
                    """
                    Enables canonical equivalence.
                    
                    Characters match if their canonical decompositions are equal.
                    No embedded flag exists."""
            )
            .documentKey(
                    RegexFlag.UNICODE_CHARACTER_CLASS,
                    """
                    Enables Unicode character classes.
                    
                    Predefined and POSIX character classes follow Unicode TR18.
                    Implies `UNICODE_CASE`.
                    Embedded flag: `(?U)`"""
            );

    public final int mode;

    RegexFlag(int mode) {
        this.mode = mode;
    }
}
