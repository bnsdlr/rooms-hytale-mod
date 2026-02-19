package de.bsdlr.rooms.lib.asset.validators;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Predicate;

public class IncludesValidator<T> implements Validator<T> {
    @Nonnull
    private final Set<T> options;
    private final String noMatchError;
    private final Predicate<T> isValidAnyWay;

    public IncludesValidator(@Nonnull Set<T> options) {
        this.options = options;
        this.noMatchError = null;
        this.isValidAnyWay = null;
    }

    public IncludesValidator(@Nonnull Set<T> options, String noMatchError) {
        this.options = options;
        this.noMatchError = noMatchError;
        this.isValidAnyWay = null;
    }

    public IncludesValidator(@Nonnull Set<T> options, String noMatchError, Predicate<T> isValidAnyWay) {
        this.options = options;
        this.noMatchError = noMatchError;
        this.isValidAnyWay = isValidAnyWay;
    }

    @Override
    public void accept(T value, ValidationResults results) {
        if (value != null && (isValidAnyWay == null || isValidAnyWay.test(value))) {
            boolean matchFound = false;

            for (T option : options) {
                if (option.equals(value)) {
                    matchFound = true;
                    break;
                }
            }

            if (!matchFound && noMatchError != null) {
                results.fail(noMatchError);
            }
        } else {
            if (noMatchError != null) results.fail(noMatchError);
        }
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {

    }
}
