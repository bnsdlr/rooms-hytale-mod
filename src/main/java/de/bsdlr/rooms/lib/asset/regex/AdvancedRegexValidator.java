package de.bsdlr.rooms.lib.asset.regex;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

import javax.annotation.Nonnull;
import java.util.Set;

public class AdvancedRegexValidator implements Validator<AdvancedRegex> {
    @Nonnull
    private RegexValidator regexValidator;

    public AdvancedRegexValidator() {
        this.regexValidator = new RegexValidator();
    }

    public AdvancedRegexValidator(Set<String> options) {
        this.regexValidator = new RegexValidator(options);
    }

    @Override
    public void accept(AdvancedRegex advancedRegex, ValidationResults results) {
        regexValidator.accept(advancedRegex.include, results);
        regexValidator.accept(advancedRegex.exclude, results);
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {

    }
}
