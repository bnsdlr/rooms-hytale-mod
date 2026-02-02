package de.bsdlr.rooms.utils;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R accept(T a, U b, V c);
}
