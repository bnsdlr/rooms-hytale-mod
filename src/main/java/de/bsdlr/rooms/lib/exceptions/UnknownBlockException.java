package de.bsdlr.rooms.lib.exceptions;

public class UnknownBlockException extends RuntimeException {
    public UnknownBlockException(int blockId) {
        super("Couldn't find a block type for block with id: " + blockId);
    }
    public UnknownBlockException(String message) {
        super(message);
    }
}
