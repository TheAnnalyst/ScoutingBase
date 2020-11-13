package base

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
            (color.red * this.COLOR_TO_HEX),
            (color.green * this.COLOR_TO_HEX),
            (color.blue * this.COLOR_TO_HEX)
        )
    }

    CustColor(String color) {
        this.hexVal = color
    }

    CustColor(int intColor) {
        hexVal = String.format('#%06X', (0xFFFFFF & intColor))
        Lib.report(hexVal)
    }

}
