package de.bsdlr.rooms.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PackedBox {
    public static final long UNUSED_HI = Long.MAX_VALUE;
    public static final Vector3i UNUSED_HI_VEC;
    public static final BuilderCodec<PackedBox> CODEC = BuilderCodec.builder(PackedBox.class, PackedBox::new)
            .appendInherited(new KeyedCodec<>("L", Codec.LONG),
                    (packedBox, s) -> packedBox.lo = s,
                    packedBox -> packedBox.lo,
                    (packedBox, parent) -> packedBox.lo = parent.lo)
            .add()
            .appendInherited(new KeyedCodec<>("H", Codec.LONG),
                    (packedBox, s) -> packedBox.hi = s,
                    packedBox -> packedBox.hi == UNUSED_HI ? null : packedBox.hi,
                    (packedBox, parent) -> packedBox.hi = parent.hi)
            .add()
            .build();
    protected long lo;
    protected long hi = UNUSED_HI;

    PackedBox() {
    }

    public PackedBox(long lo) {
        this.lo = lo;
    }

    public PackedBox(long lo, long hi) {
        this.lo = lo;
        this.hi = hi;
    }

    public PackedBox(int lx, int ly, int lz) {
        this.lo = PositionUtils.pack3dPos(lx, ly, lz);
    }

    public PackedBox(int lx, int ly, int lz, int hx, int hy, int hz) {
        this.lo = PositionUtils.pack3dPos(lx, ly, lz);
        this.hi = PositionUtils.pack3dPos(hx, hy, hz);
    }

    public PackedBox(@Nonnull Vector3i lv) {
        this.lo = PositionUtils.pack3dPos(lv.x, lv.y, lv.z);
    }

    public PackedBox(@Nonnull Vector3i lv, @Nonnull Vector3i hv) {
        this.lo = PositionUtils.pack3dPos(lv.x, lv.y, lv.z);
        this.hi = PositionUtils.pack3dPos(hv.x, hv.y, hv.z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!getClass().equals(obj.getClass())) return false;
        PackedBox o = (PackedBox) obj;
        // Both representations of single points should be equal:
        // (lo, UNUSED_HI) is equivalent to (lo, lo)
        return lo == o.lo && (hi == o.hi ||
                (hi == UNUSED_HI && o.hi == lo) ||
                (o.hi == UNUSED_HI && hi == o.lo));
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + Objects.hashCode(lo);
        if (hi != UNUSED_HI && hi != lo) result = result * 31 + Objects.hashCode(hi);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PackedBox{");
        builder.append("lo=");
        builder.append(unpackLo());
        if (hi == UNUSED_HI) {
            builder.append(",hi=UNUSED}");
        } else {
            builder.append(",hi=");
            builder.append(unpackHi());
            builder.append("}");
        }
        return builder.toString();
    }

    public Vector3i unpackLo() {
        int x = PositionUtils.unpack3dX(lo);
        int y = PositionUtils.unpack3dY(lo);
        int z = PositionUtils.unpack3dZ(lo);
        return new Vector3i(x, y, z);
    }

    public Vector3i unpackHi() {
        int x = PositionUtils.unpack3dX(hi);
        int y = PositionUtils.unpack3dY(hi);
        int z = PositionUtils.unpack3dZ(hi);
        return new Vector3i(x, y, z);
    }

    public boolean containsPos(long pos) {
        int x = PositionUtils.unpack3dX(pos);
        int y = PositionUtils.unpack3dY(pos);
        int z = PositionUtils.unpack3dZ(pos);
        return containsPos(x, y, z);
    }

    public boolean containsPos(Vector3i v) {
        return containsPos(v.x, v.y, v.z);
    }

    private static int diff(int l, int h) {
        return Math.abs(l - h) + 1;
    }

    public boolean containsPos(int x, int y, int z) {
        int lx = PositionUtils.unpack3dX(lo);
        int ly = PositionUtils.unpack3dY(lo);
        int lz = PositionUtils.unpack3dZ(lo);

        if (hi == UNUSED_HI) {
            return x == lx && y == ly && z == lz;
        } else {
            int hx = PositionUtils.unpack3dX(hi);
            int hy = PositionUtils.unpack3dY(hi);
            int hz = PositionUtils.unpack3dZ(hi);

            int minX = Math.min(lx, hx);
            int minY = Math.min(ly, hy);
            int minZ = Math.min(lz, hz);
            int maxX = Math.max(lx, hx);
            int maxY = Math.max(ly, hy);
            int maxZ = Math.max(lz, hz);

            return !(x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ);
        }
    }

    public int blockCount() {
        if (hi == UNUSED_HI) {
            return 1;
        } else {
            int lx = PositionUtils.unpack3dX(lo);
            int ly = PositionUtils.unpack3dY(lo);
            int lz = PositionUtils.unpack3dZ(lo);
            int hx = PositionUtils.unpack3dX(hi);
            int hy = PositionUtils.unpack3dY(hi);
            int hz = PositionUtils.unpack3dZ(hi);

            return diff(lx, hx) * diff(ly, hy) * diff(lz, hz);
        }
    }

    public LongStream getAllPositionsStream() {
        if (hi == UNUSED_HI || lo == hi) {
            return LongStream.of(lo);
        }

        int lx = PositionUtils.unpack3dX(lo);
        int ly = PositionUtils.unpack3dY(lo);
        int lz = PositionUtils.unpack3dZ(lo);
        int hx = PositionUtils.unpack3dX(hi);
        int hy = PositionUtils.unpack3dY(hi);
        int hz = PositionUtils.unpack3dZ(hi);

        int xDiff = diff(lx, hx);
        int yDiff = diff(ly, hy);
        int zDiff = diff(lz, hz);

        int minX = Math.min(lx, hx);
        int minY = Math.min(ly, hy);
        int minZ = Math.min(lz, hz);

        long size = (long) xDiff * yDiff * zDiff;

        Spliterator.OfLong spliterator = new Spliterators.AbstractLongSpliterator(
                size,
                Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE
        ) {
            int dx = 0, dy = 0, dz = 0;

            @Override
            public boolean tryAdvance(LongConsumer action) {
                if (dy >= yDiff) {
                    return false;
                }

                long key = PositionUtils.pack3dPos(minX + dx, minY + dy, minZ + dz);
                action.accept(key);

                dz++;
                if (dz >= zDiff) {
                    dz = 0;
                    dx++;
                    if (dx >= xDiff) {
                        dx = 0;
                        dy++;
                    }
                }

                return true;
            }
        };

        return StreamSupport.longStream(spliterator, false);
    }

    public long[] getAllPositions() {
        if (hi == UNUSED_HI || lo == hi) {
            return new long[]{lo};
        }

        int lx = PositionUtils.unpack3dX(lo);
        int ly = PositionUtils.unpack3dY(lo);
        int lz = PositionUtils.unpack3dZ(lo);
        int hx = PositionUtils.unpack3dX(hi);
        int hy = PositionUtils.unpack3dY(hi);
        int hz = PositionUtils.unpack3dZ(hi);

        int xDiff = diff(lx, hx);
        int yDiff = diff(ly, hy);
        int zDiff = diff(lz, hz);
        int size = xDiff * yDiff * zDiff;

        int minX = Math.min(lx, hx);
        int minY = Math.min(ly, hy);
        int minZ = Math.min(lz, hz);

        long[] positions = new long[size];

        for (int dx = 0; dx < xDiff; dx++) {
            for (int dy = 0; dy < yDiff; dy++) {
                for (int dz = 0; dz < zDiff; dz++) {
                    int i = getPosIndex(xDiff, zDiff, dx, dy, dz);
                    positions[i] = PositionUtils.pack3dPos(minX + dx, minY + dy, minZ + dz);
                }
            }
        }

        return positions;
    }

    public static int getPosIndex(int xDiff, int zDiff, int x, int y, int z) {
        return y * (zDiff * xDiff) + z * xDiff + x;
    }

    static {
        int x = PositionUtils.unpack3dX(UNUSED_HI);
        int y = PositionUtils.unpack3dY(UNUSED_HI);
        int z = PositionUtils.unpack3dZ(UNUSED_HI);
        UNUSED_HI_VEC = new Vector3i(x, y, z);
    }

    public static class Builder {
        private final Vector3i lo;
        private final Vector3i hi;

        public Builder(Vector3i vec) {
            this.lo = vec;
            this.hi = vec;
        }

        public Builder(Vector3i lo, Vector3i hi) {
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (!getClass().equals(obj.getClass())) return false;
            Builder o = (Builder) obj;
            return lo.equals(o.lo) && hi.equals(o.hi);
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = result * 31 + Objects.hashCode(lo);
            result = result * 31 + Objects.hashCode(hi);
            return result;
        }

        @Override
        public String toString() {
            return "PackedBox{" +
                    "lo=" +
                    lo +
                    ",hi=" +
                    hi +
                    "}";
        }

        public boolean expandZWithSegment(Vector2i seg) {
            if ((seg.x == lo.x && seg.y == hi.x) || (seg.y == lo.x && seg.x == hi.x)) {
                hi.z++;
                return true;
            }
            return false;
        }

        public boolean expandYWithBuilder(Builder builder) {
            if (builder.lo.x == lo.x && builder.hi.x == hi.x && builder.lo.z == lo.z && builder.hi.z == hi.z) {
                hi.y++;
                return true;
            }
            return false;
        }

        public PackedBox build() {
            return new PackedBox(lo, hi);
        }
    }
}
