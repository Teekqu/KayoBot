package utils;

import java.awt.*;

public class Get {

    public static Color getColor() {
        return Color.decode("#7700ff");
    }

    public static Color getColor(boolean success) {
        if(success) return Color.decode("#00ff00");
        return Color.decode("#ff0000");
    }

}
