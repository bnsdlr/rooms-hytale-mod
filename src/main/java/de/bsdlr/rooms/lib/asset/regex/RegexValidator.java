package de.bsdlr.rooms.lib.asset.regex;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import de.bsdlr.rooms.RoomsPlugin;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidator implements Validator<Regex> {
    private final Set<String> options;

    public RegexValidator(Set<String> options) {
        this.options = options;
    }

    @Override
    public void accept(Regex regex, ValidationResults results) {
        int flags = regex.getFlagMask();
        if (regex.pattern.isBlank()) results.fail("Pattern is blank (empty or only whitespace).");

        try {
            Pattern pattern = Pattern.compile(regex.pattern, flags);

            if (options != null) {
                boolean anyMatch = false;

                for (String option : options) {
                    Matcher matcher = pattern.matcher(option);
                    if (matcher.matches()) {
                        RoomsPlugin.LOGGER.atInfo().log("Match found.");
                        anyMatch = true;
                        break;
                    }
                }

                if (!anyMatch) {
                    results.fail("Regex doesn't match any existing block id.");
                }
            }
        } catch (PatternSyntaxException e) {
            results.fail(e.getMessage() + "\n" + e.getDescription());
        } catch (IllegalArgumentException e) {
            results.fail(e.getMessage());
        }
    }

    @Override
    public void updateSchema(SchemaContext ctx, Schema schema) {
        // Regex is fundamentally a string
        schema.setTypes(new String[] { "string" });

        schema.setTitle("Regular Expression");

        schema.setDescription(
                "A Java regular expression pattern. " +
                        "The pattern must be syntactically valid. " +
                        (options != null
                                ? "It must match at least one existing block id."
                                : "")
        );

        // Optional: improve editor / UI behavior
        Schema.HytaleMetadata hytale = schema.getHytale(true);
        hytale.setUiPropertyTitle("Regex Pattern");
    }
}
