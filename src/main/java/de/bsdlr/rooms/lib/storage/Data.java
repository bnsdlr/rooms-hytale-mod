package de.bsdlr.rooms.lib.storage;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Data<T> {
    @Nonnull
    private final Path path;
    private final String name;
    private final BuilderCodec<T> codec;
    @Nullable
    private T data;
    @Nullable
    private CompletableFuture<T> loadingData;

    public Data(@Nonnull Path path, String name, BuilderCodec<T> codec) {
        this.path = path.resolve(name + ".json");
        this.name = name;
        this.codec = codec;
    }

    @Nonnull
    public static <T> Data<T> preloadedData(@Nonnull Path path, String name, BuilderCodec<T> codec, T data) {
        Data<T> c = new Data<>(path, name, codec);
        c.data = data;
        return c;
    }

    @Nonnull
    public CompletableFuture<T> load() {
        if (this.loadingData != null) {
            return this.loadingData;
        } else if (!Files.exists(this.path)) {
            this.data = this.codec.getDefaultValue();
            return CompletableFuture.completedFuture(this.data);
        } else {
            return this.loadingData = CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier(() -> {
                this.data = RawJsonReader.readSync(this.path, this.codec, HytaleLogger.getLogger());
                this.loadingData = null;
                return this.data;
            }));
        }
    }

    public T get() {
        if (this.data == null && this.loadingData == null) {
            throw new IllegalStateException("Data is not loaded");
        } else {
            return this.loadingData != null ? this.loadingData.join() : this.data;
        }
    }

    @Nonnull
    public CompletableFuture<Void> save() {
        if (this.data == null && this.loadingData == null) {
            throw new IllegalStateException("Data is not loaded");
        } else {
            return this.loadingData != null
                    ? CompletableFuture.completedFuture(null)
                    : BsonUtil.writeDocument(this.path, this.codec.encode(this.data, new ExtraInfo()));
        }
    }

    public boolean isLoaded() {
        return data != null;
    }
}
