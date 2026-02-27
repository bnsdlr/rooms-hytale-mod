package de.bsdlr.rooms.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.Collection;
import java.util.function.Supplier;

public class PositionUtils {
    public static final int TWO_TO_THE_POWER_OF_ELEVEN = 2048;
    public static final int TWO_TO_THE_POWER_OF_TWELVE = 4096;
    public static final int TWO_TO_THE_POWER_OF_TWENTY_FIVE = 33554432;
    public static final int TWO_TO_THE_POWER_OF_TWENTY_SIX = 67108864;

    public static void forBlockInRadius(Vector3i radius, TriFunction<Integer, Integer, Integer, Void> f) {
        forBlockInRadius(radius, 0, 0, 0, f);
    }

    public static void forBlockInRadius(Vector3i radius, Vector3i offset, TriFunction<Integer, Integer, Integer, Void> f) {
        forBlockInRadius(radius, offset.x, offset.y, offset.z, f);
    }

    public static void forBlockInRadius(Vector3i radius, int offsetX, int offsetY, int offsetZ, TriFunction<Integer, Integer, Integer, Void> f) {
        int minRX = offsetX - radius.x + 1;
        int minRY = offsetY - radius.y + 1;
        int minRZ = offsetZ - radius.z + 1;
        int maxRX = offsetX + radius.x;
        int maxRY = offsetY + radius.y;
        int maxRZ = offsetZ + radius.z;

        for (int dx = minRX; dx < maxRX; dx++) {
            for (int dy = minRY; dy < maxRY; dy++) {
                for (int dz = minRZ; dz < maxRZ; dz++) {
                    f.accept(dx, dy, dz);
                }
            }
        }
    }

    public static <R, C extends Collection<R>> C forBlockInRadius(Vector3i radius, TriFunction<Integer, Integer, Integer, R> f, Supplier<C> supplier) {
        return forBlockInRadius(radius, 0, 0, 0, f, supplier);
    }

    public static <R, C extends Collection<R>> C forBlockInRadius(Vector3i radius, Vector3i offset, TriFunction<Integer, Integer, Integer, R> f, Supplier<C> supplier) {
        return forBlockInRadius(radius, offset.x, offset.y, offset.z, f, supplier);
    }

    public static <R, C extends Collection<R>> C forBlockInRadius(Vector3i radius, int offsetX, int offsetY, int offsetZ, TriFunction<Integer, Integer, Integer, R> f, Supplier<C> supplier) {
        C results = supplier.get();

        int minRX = offsetX - radius.x + 1;
        int minRY = offsetY - radius.y + 1;
        int minRZ = offsetZ - radius.z + 1;
        int maxRX = offsetX + radius.x;
        int maxRY = offsetY + radius.y;
        int maxRZ = offsetZ + radius.z;

        for (int dx = minRX; dx < maxRX; dx++) {
            for (int dy = minRY; dy < maxRY; dy++) {
                for (int dz = minRZ; dz < maxRZ; dz++) {
                    R value = f.accept(dx, dy, dz);
                    if (value != null) {
                        results.add(value);
                    }
                }
            }
        }

        return results;
    }

    public static Vector3i positionToVector3i(Vector3d position) {
        int posX = (position.x < 0) ? (int) Math.floor(position.x) : (int) position.x;
        int posY = (int) position.y;
        int posZ = (position.z < 0) ? (int) Math.floor(position.z) : (int) position.z;
        return new Vector3i(posX, posY, posZ);
    }

    public static void applyOffset(Vector3i pos, int ox, int oy, int oz) {
        pos.x += ox;
        pos.y += oy;
        pos.z += oz;
    }

    public static Vector3i toDiff(Vector3d min, Vector3d max) {
        return toDiff(toFullBlockPosition(min, true), toFullBlockPosition(max, false));
    }

    public static Vector3i toDiff(Vector3i min, Vector3i max) {
        return new Vector3i(max.x - min.x, max.y - min.y, max.z - min.z);
    }

    public static Vector3i toFullBlockPosition(Vector3d pos, boolean isMin) {
        int x = (int) (pos.x > 0 ? (isMin ? Math.floor(pos.x) : Math.ceil(pos.x)) : (isMin ? Math.ceil(pos.x) : Math.floor(pos.x)));
        int y = (int) (pos.y > 0 ? (isMin ? Math.floor(pos.y) : Math.ceil(pos.y)) : (isMin ? Math.ceil(pos.y) : Math.floor(pos.y)));
        int z = (int) (pos.z > 0 ? (isMin ? Math.floor(pos.z) : Math.ceil(pos.z)) : (isMin ? Math.ceil(pos.z) : Math.floor(pos.z)));
        return new Vector3i(x, y, z);
    }

    public static long update(long pos, int changeInX, int changeInY, int changeInZ) {
        return pack3dPos(unpack3dX(pos) + changeInX, unpack3dY(pos) + changeInY, unpack3dZ(pos) + changeInZ);
    }

    public static long pack3dPos(Vector3i vec) {
        return pack3dPos(vec.x, vec.y, vec.z);
    }

    public static long pack3dPos(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) |
                ((long) (z & 0x3FFFFFF) << 12) |
                ((long) (y & 0xFFF));
    }

    public static int unpack3dX(long key) {
        int x = (int) (key >> 38) & 0x3FFFFFF;
        return x >= TWO_TO_THE_POWER_OF_TWENTY_FIVE ? x - TWO_TO_THE_POWER_OF_TWENTY_SIX : x;
    }

    public static int unpack3dY(long key) {
        int y = (int) (key & 0xFFF);
        return y >= TWO_TO_THE_POWER_OF_ELEVEN ? y - TWO_TO_THE_POWER_OF_TWELVE : y;
    }

    public static int unpack3dZ(long key) {
        int z = (int) ((key >> 12) & 0x3FFFFFF);
        return z >= TWO_TO_THE_POWER_OF_TWENTY_FIVE ? z - TWO_TO_THE_POWER_OF_TWENTY_SIX : z;
    }

    public static Vector3i unpack3d(long key) {
        int x = unpack3dX(key);
        int y = unpack3dY(key);
        int z = unpack3dZ(key);
        return new Vector3i(x, y, z);
    }

    public static long pack2dPos(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public static int unpack2dX(long key) {
        return (int) (key >>> 32);
    }

    public static int unpack2dZ(long key) {
        return (int) key;
    }
}
