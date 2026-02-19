package de.bsdlr.rooms.lib.asset;

import com.hypixel.hytale.protocol.ColorLight;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LightTest {
    @ParameterizedTest
    @MethodSource("lightMatchCases")
    void testLightMatch(Light light, ColorLight colorLight, boolean shouldMatch) {
        assertEquals(light.matches(colorLight), shouldMatch, shouldMatch ? "ColorLight doesn't match Light but it should" : "ColorLight does match Light but it shouldn't.");
    }

    static Stream<Arguments> lightMatchCases() {
        return Stream.of(
                Arguments.of(new Light(), new ColorLight(), true),
                Arguments.of(new Light(true), new ColorLight((byte) 0, (byte) 0, (byte) 0, (byte) 0), true),
                Arguments.of(new Light(true), new ColorLight((byte) 10, (byte) -92, (byte) -122, (byte) 73), true),
                Arguments.of(new Light((byte) 100, (byte) 100, (byte) 100, (byte) 100, (byte) 100, (byte) 100, (byte) 1, (byte) 127), new ColorLight((byte) 10, (byte) -92, (byte) -122, (byte) 73), false),
                Arguments.of(new Light(), null, true)
        );
    }
}
