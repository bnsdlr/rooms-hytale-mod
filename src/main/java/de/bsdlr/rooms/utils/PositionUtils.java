package de.bsdlr.rooms.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.Collection;
import java.util.function.Supplier;

public class PositionUtils {
    public static void forOffsetInRadius(Vector3i radius, TriFunction<Integer, Integer, Integer, Void> f) {
        for (int dx = -radius.x + 1; dx < radius.x; dx++) {
            for (int dy = -radius.y + 1; dy < radius.y; dy++) {
                for (int dz = -radius.z + 1; dz < radius.z; dz++) {
                    f.accept(dx, dy, dz);
                }
            }
        }
    }

    public static <R, C extends Collection<R>> C forOffsetInRadius(Vector3i radius, TriFunction<Integer, Integer, Integer, R> f, Supplier<C> supplier) {
        C results = supplier.get();

        for (int dx = -radius.x + 1; dx < radius.x; dx++) {
            for (int dy = -radius.y + 1; dy < radius.y; dy++) {
                for (int dz = -radius.z + 1; dz < radius.z; dz++) {
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

    public static long encodePosition(Vector3i vec) {
        return encodePosition(vec.x, vec.y, vec.z);
    }

    public static long encodePosition(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) |
                ((long) (z & 0x3FFFFFF) << 12) |
                ((long) (y & 0xFFF));
    }

    public static int decodeX(long encoded) {
        return (int) (encoded >> 38);
    }

    public static int decodeY(long encoded) {
        int y = (int) (encoded & 0xFFF);
        return y >= 2048 ? y - 4096 : y;
    }

    public static int decodeZ(long encoded) {
        return (int) ((encoded >> 12) & 0x3FFFFFF);
    }

//    public static Vector3i positionToVector3i(Vector3d position) {
//        int posX = (position.x < 0) ? (int) Math.floor(position.x) : (int) position.x;
//        int posY = (int) position.y;
//        int posZ = (position.z < 0) ? (int) Math.floor(position.z) : (int) position.z;
//        return new Vector3i(posX, posY, posZ);
//    }
//
////    public static long encodePosition(Vector3i vec) {
////        return encodePosition(vec.x, vec.y, vec.z);
////    }
////
////    public static long encodePosition(int x, int y, int z) {
////        return ((long) (x & 0x3FFFFFF) << 38) |
////                ((long) (z & 0x3FFFFFF) << 12) |
////                ((long) (y & 0xFFF));
////    }
//
//    public static long encodePosition(Vector3d vec) {
//        return encodePosition(vec.x, vec.y, vec.z);
//    }
//
//    public static long encodePosition(double x, double y, double z) {
//        return ((long) ((int) x & 0x3FFFFFF) << 38) |
//                ((long) ((int) z & 0x3FFFFFF) << 12) |
//                ((long) ((int) y & 0x1FF) << 3) |
//                ((long) ((int) (x * 2) & 1) << 2) |
//                ((long) ((int) (z * 2) & 1) << 1) |
//                ((long) ((int) (y * 2) & 1));
//    }
//
//    public static double decodeX(long key) {
//        int decoded = (int) ((key >> 38) & 0x3FFFFFF);
//        boolean isNeg = (decoded & 0x2000000) != 0;
//
//        if (isNeg) decoded |= 0xFC000000;
//
//        if (((key >> 2) & 1) == 1) {
//            return isNeg ? (double)decoded - 0.5 : (double)decoded + 0.5;
//        }
//
//        return decoded;
//    }
//
//    public static double decodeY(long key) {
//        int decoded = (int) ((key >> 3) & 0x1FF);
//
//        if ((key & 1) == 1) {
//            return decoded + 0.5;
//        }
//
//        return decoded;
//    }
//
//    public static double decodeZ(long key) {
//        int decoded = (int) ((key >> 12) & 0x3FFFFFF);
//        boolean isNeg = (decoded & 0x2000000) != 0;
//
//        if (isNeg) decoded |= 0xFC000000;
//
//        if (((key >> 1) & 1) == 1) {
//            return isNeg ? (double) decoded - 0.5 : (double) decoded + 0.5;
//        }
//
//        return decoded;
//    }
}
