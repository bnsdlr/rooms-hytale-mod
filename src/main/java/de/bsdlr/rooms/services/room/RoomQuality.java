package de.bsdlr.rooms.services.room;

import java.awt.*;

public enum RoomQuality {
    Common(new Color(201, 210, 221)),
    Unkcommon(new Color(62, 144, 73)),
    Rare(new Color(0, 0, 0)),
    Epic(new Color(0, 0, 0)),
    Legendary(new Color(0, 0, 0));

    private Color color;

    RoomQuality(Color color) {
        this.color = color;
    }
}
