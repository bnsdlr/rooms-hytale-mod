package de.bsdlr.rooms.lib.storage;

import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DataManager<K, T> {
    private final Path dir;
    private final Map<K, Data<T>> dataMap;
    private final BuilderCodec<T> codec;

    public DataManager(Path dataDir, String subDir, BuilderCodec<T> codec) {
        this.dir = dataDir.resolve(subDir);
        this.dataMap = new HashMap<>();
        this.codec = codec;
    }

    public DataManager(Path dataDir, Path subDir, BuilderCodec<T> codec) {
        this.dir = dataDir.resolve(subDir);
        this.dataMap = new HashMap<>();
        this.codec = codec;
    }

    @Nonnull
    public Data<T> addData(K key) {
        Data<T> newData = new Data<>(dir, key.toString(), codec);
        this.dataMap.putIfAbsent(key, newData);
        return newData;
    }

    public Data<T> getData(K key) {
        return this.dataMap.get(key);
    }

    @Nonnull
    public Data<T> getDataAndComputeIfAbsent(K key) {
        Data<T> data = this.dataMap.get(key);
        if (data == null) return addData(key);
        return data;
    }

    public void load(K key) {
        Data<T> data = this.getData(key);
        if (data == null) {
            data = addData(key);
        }
        data.load();
//        CompletableFuture<T> future = data.load();
//        future.wait();
    }

    public void loadAll() {
        for (K key : dataMap.keySet()) {
            this.load(key);
        }
    }

    public void save(K key) {
        Data<T> data = this.getData(key);
        if (data == null) return;
        data.save();
//        CompletableFuture<Void> future = data.save();
//        future.wait();
    }

    public void saveAll() {
        for (Data<T> data : dataMap.values()) {
            data.save();
//            CompletableFuture<Void> future = data.save();
//            future.wait();
        }
    }
}
