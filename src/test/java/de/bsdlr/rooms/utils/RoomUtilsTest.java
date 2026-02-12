package de.bsdlr.rooms.utils;

import com.hypixel.hytale.math.vector.Vector3i;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoomUtilsTest {
    Set<Long> helperVector2Long(Set<Vector3i> blocks) {
        Set<Long> positions = new HashSet<>();

        for (Vector3i blockPos : blocks) {
            positions.add(PositionUtils.pack3dPos(blockPos));
        }

        return positions;
    }

    Set<PackedBox> helper2dCompression(Set<Long> blocks) {
        return RoomUtils.compress2d(blocks).stream().map(PackedBox.Builder::build).collect(Collectors.toSet());
    }

    Set<PackedBox> testCompressionHelper(Set<Vector3i> blocks) {
        Set<Long> positions = new HashSet<>();

        for (Vector3i blockPos : blocks) {
            positions.add(PositionUtils.pack3dPos(blockPos));
        }

        return RoomUtils.compress(positions);
    }

    @Test
    void test2dCompression() {
        // Test 1: Single block
        Set<Vector3i> b1 = new HashSet<>();
        b1.add(new Vector3i(0, 0, 0));
        Set<PackedBox> s1 = new HashSet<>();
        s1.add(new PackedBox(new Vector3i(0, 0, 0)));
        Set<PackedBox> r1 = helper2dCompression(helperVector2Long(b1));

        // Test 2: Horizontal line (should merge into one box)
        Set<Vector3i> b2 = new HashSet<>();
        b2.add(new Vector3i(0, 0, 0));
        b2.add(new Vector3i(1, 0, 0));
        b2.add(new Vector3i(2, 0, 0));
        b2.add(new Vector3i(3, 0, 0));
        Set<PackedBox> s2 = new HashSet<>();
        s2.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(3, 0, 0)));
        Set<PackedBox> r2 = helper2dCompression(helperVector2Long(b2));

        // Test 3: Vertical stack in z (should merge into one box)
        Set<Vector3i> b3 = new HashSet<>();
        b3.add(new Vector3i(0, 0, 0));
        b3.add(new Vector3i(0, 0, 1));
        b3.add(new Vector3i(0, 0, 2));
        Set<PackedBox> s3 = new HashSet<>();
        s3.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(0, 0, 2)));
        Set<PackedBox> r3 = helper2dCompression(helperVector2Long(b3));

        // Test 4: Gap in z (should create two separate boxes)
        Set<Vector3i> b4 = new HashSet<>();
        b4.add(new Vector3i(0, 0, 0));
        b4.add(new Vector3i(1, 0, 0));
        b4.add(new Vector3i(0, 0, 3));
        b4.add(new Vector3i(1, 0, 3));
        Set<PackedBox> s4 = new HashSet<>();
        s4.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(1, 0, 0)));
        s4.add(new PackedBox(new Vector3i(0, 0, 3), new Vector3i(1, 0, 3)));
        Set<PackedBox> r4 = helper2dCompression(helperVector2Long(b4));

        // Test 5: Two separate horizontal segments (should create two boxes)
        Set<Vector3i> b5 = new HashSet<>();
        b5.add(new Vector3i(0, 0, 0));
        b5.add(new Vector3i(1, 0, 0));
        b5.add(new Vector3i(5, 0, 0));
        b5.add(new Vector3i(6, 0, 0));
        Set<PackedBox> s5 = new HashSet<>();
        s5.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(1, 0, 0)));
        s5.add(new PackedBox(new Vector3i(5, 0, 0), new Vector3i(6, 0, 0)));
        Set<PackedBox> r5 = helper2dCompression(helperVector2Long(b5));

        assertEquals(r1, s1);
        assertEquals(r2, s2);
        assertEquals(r3, s3);
        assertEquals(r4, s4);
        assertEquals(r5, s5);
    }

    @Test
    void testCompression() {
        // Test 1: Single block
        Set<Vector3i> b1 = new HashSet<>();
        b1.add(new Vector3i(0, 0, 0));

        Set<PackedBox> s1 = new HashSet<>();
        s1.add(new PackedBox(new Vector3i(0, 0, 0)));

        Set<PackedBox> r1 = testCompressionHelper(b1);
        Set<Long> l1 = helperVector2Long(b1);
        Set<Long> rl1 = RoomUtils.uncompress(r1);

        // Test 2: Vertical stack in Y (should merge into one box)
        Set<Vector3i> b2 = new HashSet<>();
        b2.add(new Vector3i(0, 0, 0));
        b2.add(new Vector3i(0, 1, 0));
        b2.add(new Vector3i(0, 2, 0));

        Set<PackedBox> s2 = new HashSet<>();
        s2.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(0, 2, 0)));

        Set<PackedBox> r2 = testCompressionHelper(b2);
        Set<Long> l2 = helperVector2Long(b2);
        Set<Long> rl2 = RoomUtils.uncompress(r2);

        // Test 3: Cube 2x2x2 (should merge into one box)
        Set<Vector3i> b3 = new HashSet<>();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    b3.add(new Vector3i(x, y, z));
                }
            }
        }

        Set<PackedBox> s3 = new HashSet<>();
        s3.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(1, 1, 1)));

        Set<PackedBox> r3 = testCompressionHelper(b3);
        Set<Long> l3 = helperVector2Long(b3);
        Set<Long> rl3 = RoomUtils.uncompress(r3);

        // Test 4: Two layers with same XZ footprint (should merge)
        Set<Vector3i> b4 = new HashSet<>();
        b4.add(new Vector3i(0, 0, 0));
        b4.add(new Vector3i(1, 0, 0));
        b4.add(new Vector3i(0, 1, 0));
        b4.add(new Vector3i(1, 1, 0));

        Set<PackedBox> s4 = new HashSet<>();
        s4.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(1, 1, 0)));

        Set<PackedBox> r4 = testCompressionHelper(b4);
        Set<Long> l4 = helperVector2Long(b4);
        Set<Long> rl4 = RoomUtils.uncompress(r4);

        // Test 5: Two layers with different XZ footprint (should NOT merge)
        Set<Vector3i> b5 = new HashSet<>();
        b5.add(new Vector3i(0, 0, 0));
        b5.add(new Vector3i(1, 0, 0));
        b5.add(new Vector3i(0, 1, 0));
        b5.add(new Vector3i(2, 1, 0));

        Set<PackedBox> s5 = new HashSet<>();
        s5.add(new PackedBox(new Vector3i(0, 0, 0), new Vector3i(1, 0, 0)));
        s5.add(new PackedBox(new Vector3i(0, 1, 0)));
        s5.add(new PackedBox(new Vector3i(2, 1, 0)));

        Set<PackedBox> r5 = testCompressionHelper(b5);
        Set<Long> l5 = helperVector2Long(b5);
        Set<Long> rl5 = RoomUtils.uncompress(r5);

        // Test 6: Gap in Y (should create two separate boxes)
        Set<Vector3i> b6 = new HashSet<>();
        b6.add(new Vector3i(0, 0, 0));
        b6.add(new Vector3i(0, 3, 0));

        Set<PackedBox> s6 = new HashSet<>();
        s6.add(new PackedBox(new Vector3i(0, 0, 0)));
        s6.add(new PackedBox(new Vector3i(0, 3, 0)));

        Set<PackedBox> r6 = testCompressionHelper(b6);
        Set<Long> l6 = helperVector2Long(b6);
        Set<Long> rl6 = RoomUtils.uncompress(r6);

        assertEquals(s1, r1);
        assertEquals(l1, rl1);

        assertEquals(s2, r2);
        assertEquals(l2, rl2);

        assertEquals(s3, r3);
        assertEquals(l3, rl3);

        assertEquals(s4, r4);
        assertEquals(l4, rl4);

        assertEquals(s5, r5);
        assertEquals(l5, rl5);

        assertEquals(s6, r6);
        assertEquals(l6, rl6);
    }
}
