package de.bsdlr.rooms.utils;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PositionUtilsTest {

    // Test 3D packing/unpacking with various coordinate values
    @ParameterizedTest
    @CsvSource({
            "0, 0, 0",
            "100, 50, 200",
            "-100, -50, -200",
            "33554431, 2047, 33554431",    // Max positive boundaries
            "-33554432, -2048, -33554432",  // Max negative boundaries
            "1000000, 1000, -1000000"       // Mixed signs
    })
    void testPack3dAndUnpack3d(int x, int y, int z) {
        // Pack the coordinates into a long
        long packed = PositionUtils.pack3dPos(x, y, z);

        // Unpack and verify each component matches original
        int unpackedX = PositionUtils.unpack3dX(packed);
        int unpackedY = PositionUtils.unpack3dY(packed);
        int unpackedZ = PositionUtils.unpack3dZ(packed);

        assertEquals(x, unpackedX, "X coordinate mismatch");
        assertEquals(y, unpackedY, "Y coordinate mismatch");
        assertEquals(z, unpackedZ, "Z coordinate mismatch");
    }

    // Test 3D packing with Vector3i input
    @Test
    void testPack3dWithVector() {
        Vector3i testVector = new Vector3i(123, 456, 789);

        long packed = PositionUtils.pack3dPos(testVector);

        assertEquals(123, PositionUtils.unpack3dX(packed));
        assertEquals(456, PositionUtils.unpack3dY(packed));
        assertEquals(789, PositionUtils.unpack3dZ(packed));
    }

    // Test 2D packing/unpacking
    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1000, 2000",
            "-1000, -2000",
            "2147483647, 2147483647",   // Max int
            "-2147483648, -2147483648"  // Min int
    })
    void testPack2dAndUnpack2d(int x, int z) {
        long packed = PositionUtils.pack2dPos(x, z);

        int unpackedX = PositionUtils.unpack2dX(packed);
        int unpackedZ = PositionUtils.unpack2dZ(packed);

        assertEquals(x, unpackedX, "X coordinate mismatch");
        assertEquals(z, unpackedZ, "Z coordinate mismatch");
    }

    // Test Vector3d to Vector3i conversion
    @Test
    void testPositionToVector3i_PositiveCoordinates() {
        Vector3d position = new Vector3d(10.7, 20.3, 30.9);
        Vector3i result = PositionUtils.positionToVector3i(position);

        assertEquals(10, result.x, "Positive X should truncate");
        assertEquals(20, result.y, "Y should truncate");
        assertEquals(30, result.z, "Positive Z should truncate");
    }

    @Test
    void testPositionToVector3i_NegativeCoordinates() {
        Vector3d position = new Vector3d(-10.7, -20.3, -30.9);
        Vector3i result = PositionUtils.positionToVector3i(position);

        assertEquals(-11, result.x, "Negative X should floor");
        assertEquals(-20, result.y, "Y should truncate");
        assertEquals(-31, result.z, "Negative Z should floor");
    }

    @Test
    void testPositionToVector3i_MixedSigns() {
        Vector3d position = new Vector3d(-5.5, 10.5, 15.5);
        Vector3i result = PositionUtils.positionToVector3i(position);

        assertEquals(-6, result.x);
        assertEquals(10, result.y);
        assertEquals(15, result.z);
    }

    // Test forOffsetInRadius iteration count
    @Test
    void testForOffsetInRadius_IterationCount() {
        Vector3i radius = new Vector3i(2, 2, 2); // Should iterate (2*2-1)^3 = 27 times
        AtomicInteger counter = new AtomicInteger(0);

        PositionUtils.forOffsetInRadius(radius, (x, y, z) -> {
            counter.incrementAndGet();
            return null;
        });

        int expected = (2 * radius.x - 1) * (2 * radius.y - 1) * (2 * radius.z - 1);
        assertEquals(expected, counter.get(), "Iteration count should match volume");
    }

    // Test forOffsetInRadius with collection
    @Test
    void testForOffsetInRadius_WithCollection() {
        Vector3i radius = new Vector3i(2, 2, 2);

        List<String> results = PositionUtils.forOffsetInRadius(
                radius,
                (x, y, z) -> x + "," + y + "," + z,
                ArrayList::new
        );

        int expectedSize = (2 * radius.x - 1) * (2 * radius.y - 1) * (2 * radius.z - 1);
        assertEquals(expectedSize, results.size());
        assertTrue(results.contains("0,0,0"), "Should contain center offset");
    }

    @Test
    void testForOffsetInRadius_NullFiltering() {
        Vector3i radius = new Vector3i(2, 2, 2);

        // Only add non-zero offsets
        List<Vector3i> results = PositionUtils.forOffsetInRadius(
                radius,
                (x, y, z) -> (x == 0 && y == 0 && z == 0) ? null : new Vector3i(x, y, z),
                ArrayList::new
        );

        int expectedSize = ((2 * radius.x - 1) * (2 * radius.y - 1) * (2 * radius.z - 1)) - 1;
        assertEquals(expectedSize, results.size(), "Should filter out null (center point)");
    }

    // Test boundary conditions for bit packing
    @Test
    void testBitPacking_YBoundaries() {
        // Y should work correctly at its boundaries (-2048 to 2047)
        long packedMin = PositionUtils.pack3dPos(0, -2048, 0);
        long packedMax = PositionUtils.pack3dPos(0, 2047, 0);

        assertEquals(-2048, PositionUtils.unpack3dY(packedMin));
        assertEquals(2047, PositionUtils.unpack3dY(packedMax));
    }

    @Test
    void testBitPacking_XZBoundaries() {
        // X and Z should work at 26-bit boundaries
        long packed = PositionUtils.pack3dPos(33554431, 0, -33554432);

        assertEquals(33554431, PositionUtils.unpack3dX(packed));
        assertEquals(-33554432, PositionUtils.unpack3dZ(packed));
    }
}