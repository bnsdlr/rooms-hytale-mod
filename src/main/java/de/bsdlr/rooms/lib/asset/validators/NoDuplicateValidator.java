package de.bsdlr.rooms.lib.asset.validators;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.BiPredicate;

public class NoDuplicateValidator<T> extends ArrayValidator<T> {
    @Nonnull
    private final BiPredicate<T, T> isDuplicate;
    @Nonnull
    private final String errorMessage;

    public NoDuplicateValidator(@Nonnull BiPredicate<T, T> isDuplicate, @Nonnull String errorMessage) {
        super(Validators.nonNull());
        this.isDuplicate = isDuplicate;
        this.errorMessage = errorMessage;
    }

    public void accept(Collection<T> tCollection, ValidationResults validationResults) {
        for (T t1 : tCollection) {
            for (T t2 : tCollection) {
                if (t1 == t2) continue;

                if (isDuplicate.test(t1, t2)) {
                    validationResults.fail(errorMessage);
                }
            }
        }
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {

    }
}
