package de.bsdlr.rooms.utils;

import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.*;
import java.util.stream.Stream;

public class RoomUtils {
    public static Set<PackedBox> compress(Set<Long> blocks) {
        Set<PackedBox> boxes = new HashSet<>();
        Map<Integer, Map<Integer, List<Integer>>> y2z2x = new HashMap<>();

        for (Long block : blocks) {
            int x = PositionUtils.unpack3dX(block);
            int y = PositionUtils.unpack3dY(block);
            int z = PositionUtils.unpack3dZ(block);

            y2z2x.computeIfAbsent(y, (k) -> new HashMap<>())
                    .computeIfAbsent(z, (k) -> new ArrayList<>())
                    .add(x);
        }

        Map<Integer, Set<PackedBox.Builder>> y2Builder = new HashMap<>();

        for (Map.Entry<Integer, Map<Integer, List<Integer>>> entry : y2z2x.entrySet()) {
            Set<PackedBox.Builder> compressed = compress2d(entry.getValue(), entry.getKey());
//            System.out.println(entry.getKey() + " (raw): " + entry.getValue());
//            System.out.println(entry.getKey() + " (compressed): " + compressed);
            y2Builder.put(entry.getKey(), compressed);
        }

        List<Integer> keys = new ArrayList<>(y2Builder.keySet());
        Collections.sort(keys);

        List<PackedBox.Builder> growingBoxes = new ArrayList<>();
        int prevY = keys.isEmpty() ? 0 : keys.getFirst();

        for (int y : keys) {
            if (y - 1 != prevY) {
                for (PackedBox.Builder builder : growingBoxes) {
                    boxes.add(builder.build());
                }
                growingBoxes.clear();
            }

            List<PackedBox.Builder> newGrowingBoxes = new ArrayList<>();

            for (PackedBox.Builder boxBuilder : y2Builder.get(y)) {
                boolean extendedBuilder = false;

//                System.out.println(y + ": " + boxBuilder + "; growing boxes: " + growingBoxes.size());

                Iterator<PackedBox.Builder> iterator = growingBoxes.iterator();
                while (iterator.hasNext()) {
                    PackedBox.Builder builder = iterator.next();
                    if (builder.expandYWithBuilder(boxBuilder)) {
                        extendedBuilder = true;
                        newGrowingBoxes.add(builder);
                        iterator.remove();
                        break;
                    }
                }

                if (!extendedBuilder) {
                    newGrowingBoxes.add(boxBuilder);
                }
            }

            for (PackedBox.Builder builder : growingBoxes) {
                boxes.add(builder.build());
            }

            growingBoxes = newGrowingBoxes;
            prevY = y;
        }

        for (PackedBox.Builder builder : growingBoxes) {
            boxes.add(builder.build());
        }

        return boxes;
    }

    public static Set<PackedBox.Builder> compress2d(Set<Long> blocks) {
        return compress2d(blocks, 0);
    }

    public static Set<PackedBox.Builder> compress2d(Set<Long> blocks, int y) {
        Map<Integer, List<Integer>> z2x = new HashMap<>();

        for (Long block : blocks) {
            int x = PositionUtils.unpack3dX(block);
            int z = PositionUtils.unpack3dZ(block);

            z2x.computeIfAbsent(z, (k) -> new ArrayList<>()).add(x);
        }

        return compress2d(z2x, y);
    }

    public static Set<PackedBox.Builder> compress2d(Map<Integer, List<Integer>> z2x, int y) {
        Set<PackedBox.Builder> boxes = new HashSet<>();

        Map<Integer, List<Vector2i>> z2Segment = new HashMap<>();

        for (Map.Entry<Integer, List<Integer>> entry : z2x.entrySet()) {
            List<Integer> xs = entry.getValue();
            Collections.sort(xs);
            List<Vector2i> segments = new ArrayList<>();
            Integer minX = null;
            Integer lastX = null;

            for (Integer x : xs) {
                if (lastX == null) {
                    lastX = x;
                    minX = x;
                    continue;
                }

                if (x - lastX > 1) {
                    segments.add(new Vector2i(minX, lastX));
                    minX = x;
                }

                lastX = x;
            }

            if (minX != null) segments.add(new Vector2i(minX, lastX));

            z2Segment.put(entry.getKey(), segments);
        }

        List<Integer> keys = new ArrayList<>(z2Segment.keySet());
        Collections.sort(keys);

//        System.out.println(y + " (keys sorted): " + keys);

        List<PackedBox.Builder> growingBoxes = new ArrayList<>();
        int prevZ = keys.isEmpty() ? 0 : keys.getFirst();

        for (int z : keys) {
            if (z - 1 != prevZ) {
                boxes.addAll(growingBoxes);
                growingBoxes.clear();
            }

            List<PackedBox.Builder> newGrowingBoxes = new ArrayList<>();

            for (Vector2i segment : z2Segment.get(z)) {
                boolean extendedBuilder = false;

                Iterator<PackedBox.Builder> iterator = growingBoxes.iterator();
                while (iterator.hasNext()) {
                    PackedBox.Builder builder = iterator.next();
                    if (builder.expandZWithSegment(segment)) {
                        extendedBuilder = true;
                        newGrowingBoxes.add(builder);
                        iterator.remove();
                        break;
                    }
                }

                if (!extendedBuilder) {
                    newGrowingBoxes.add(new PackedBox.Builder(new Vector3i(segment.x, y, z), new Vector3i(segment.y, y, z)));
                }
            }

            boxes.addAll(growingBoxes);

            growingBoxes = newGrowingBoxes;
            prevZ = z;
        }

        boxes.addAll(growingBoxes);

        return boxes;
    }

    public static Set<Long> uncompress(Set<PackedBox> boxes) {
        Set<Long> blocks = new HashSet<>();

        for (PackedBox box : boxes) {
            box.getAllPositionsStream().forEach(blocks::add);
        }

        return blocks;
    }
}
