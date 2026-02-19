package de.bsdlr.rooms.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PackedBoxTest {
    @ParameterizedTest
    @CsvSource({
            "3, 3, 0, 0, 0, 0",
            "3, 3, 2, 0, 0, 2",
            "3, 3, 0, 1, 0, 9",
            "3, 3, 2, 2, 2, 26",
            "3, 3, 1, 1, 1, 13",
            "5, 5, 0, 0, 0, 0",
            "5, 5, 4, 0, 0, 4",
            "5, 5, 0, 1, 0, 25",
            "5, 5, 2, 1, 3, 42",
            "4, 2, 0, 0, 0, 0",
            "4, 2, 3, 0, 0, 3",
            "4, 2, 0, 1, 0, 8",
            "4, 2, 2, 1, 1, 14",
            "3, 3, 0, 0, 1, 3",
            "3, 3, 1, 0, 2, 7",
            "1, 1, 0, 0, 0, 0",
            "2, 2, 1, 1, 1, 7",
    })
    void testGetPosIndex(int xDiff, int zDiff, int x, int y, int z, int solutionIndex) {
        assertEquals(solutionIndex, PackedBox.getPosIndex(xDiff, zDiff, x, y, z), "solutionIndex mismatch");
    }

    @ParameterizedTest
    @MethodSource("posEqualityTestCases")
    void testPosEquality(PackedBox p1, PackedBox p2, boolean solution) {
//        System.out.println(p1);
//        System.out.println(p2);
//        System.out.println(p1.equals(p2));
        assertEquals(solution, p1.equals(p2));
    }

    static Stream<Arguments> posEqualityTestCases() {
        return Stream.of(
                Arguments.of(new PackedBox(0,0,0), new PackedBox(0,0,0,0,0,0), true),
                Arguments.of(new PackedBox(0,0,0,0,0,0), new PackedBox(0,0,0), true),
                Arguments.of(new PackedBox(1,0,0), new PackedBox(1,0,0), true),
                Arguments.of(new PackedBox(1,0,0), new PackedBox(0,0,0), false),
                Arguments.of(new PackedBox(1,0,0,1,0,0), new PackedBox(1,0,0,1,0,0), true),
                Arguments.of(new PackedBox(1,0,0,0,0,0), new PackedBox(1,0,0,1,0,0), false)
        );
    }
}
