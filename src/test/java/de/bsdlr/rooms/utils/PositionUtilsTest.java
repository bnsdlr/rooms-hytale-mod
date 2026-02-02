//package de.bsdlr.rooms.utils;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class PositionUtilsTest {
//
//    @Test
//    void encodeDecode_positiveValues() {
//        int x = 12345;
//        int y = 64;
//        int z = 9876;
//
//        long key = PositionUtils.encodePosition(x, y, z);
//
//        assertEquals(x, PositionUtils.decodeX(key));
//        assertEquals(y, PositionUtils.decodeY(key));
//        assertEquals(z, PositionUtils.decodeZ(key));
//    }
//
//    @Test
//    void encodeDecode_negativeValues() {
//        int x = -100000;
//        int y = -10;
//        int z = -42;
//
//        long key = PositionUtils.encodePosition(x, y, z);
//
//        assertEquals(x, PositionUtils.decodeX(key));
//        assertEquals(y, PositionUtils.decodeY(key));
//        assertEquals(z, PositionUtils.decodeZ(key));
//    }
//
//    @Test
//    void yOutOfRange_wrapsCorrectly() {
//        int y = -2048;
//        long key = PositionUtils.encodePosition(0, y, 0);
//        assertEquals(y, PositionUtils.decodeY(key));
//    }
//}
