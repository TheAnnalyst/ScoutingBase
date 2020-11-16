package base

import groovy.transform.CompileStatic
import javafx.scene.paint.Color

/**
 * A class representing a custom color; can be constructed from a Color, String, or int.
 */
@CompileStatic
class CustColor {
    private static final int COLOR_TO_HEX = 255
    String hexVal

    CustColor(Color color) {
        this.hexVal = String.format(
            '#%02x%02X%02X',
            (color.red * COLOR_TO_HEX),
            (color.green * COLOR_TO_HEX),
            (color.blue * COLOR_TO_HEX)
        )
    }

    CustColor(String color) {
        this.hexVal = color
    }
}
