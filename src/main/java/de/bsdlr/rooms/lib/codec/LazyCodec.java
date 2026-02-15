package de.bsdlr.rooms.lib.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Supplier;

public class LazyCodec<T> implements Codec<T> {
    private final Supplier<Codec<T>> supplier;

    public LazyCodec(Supplier<Codec<T>> supplier) {
        this.supplier = supplier;
    }

    public Codec<T> get() {
        return supplier.get();
    }

    @Nullable
    @Override
    public T decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        return get().decode(bsonValue, extraInfo);
    }

    @Override
    public BsonValue encode(T t, ExtraInfo extraInfo) {
        return get().encode(t, extraInfo);
    }

    @Nullable
    @Override
    public T decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        return Codec.super.decodeJson(reader, extraInfo);
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext schemaContext) {
        return get().toSchema(schemaContext);
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext context, @Nullable T def) {
        return Codec.super.toSchema(context, def);
    }
}
