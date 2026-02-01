package de.bsdlr.rooms.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;

public class Utils {
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
}
