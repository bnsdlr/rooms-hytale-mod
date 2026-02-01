//package de.bsdlr.rooms.utils;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class UtilsTest {
//
//    @Test
//    void encodeDecode_positiveValues() {
//        int x = 12345;
//        int y = 64;
//        int z = 9876;
//
//        long key = Utils.encodePosition(x, y, z);
//
//        assertEquals(x, Utils.decodeX(key));
//        assertEquals(y, Utils.decodeY(key));
//        assertEquals(z, Utils.decodeZ(key));
//    }
//
//    @Test
//    void encodeDecode_negativeValues() {
//        int x = -100000;
//        int y = -10;
//        int z = -42;
//
//        long key = Utils.encodePosition(x, y, z);
//
//        assertEquals(x, Utils.decodeX(key));
//        assertEquals(y, Utils.decodeY(key));
//        assertEquals(z, Utils.decodeZ(key));
//    }
//
//    @Test
//    void yOutOfRange_wrapsCorrectly() {
//        int y = -2048;
//        long key = Utils.encodePosition(0, y, 0);
//        assertEquals(y, Utils.decodeY(key));
//    }
//}
