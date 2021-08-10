package xyz.jeremynoesen.pseudo3d.input;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to listen for key strokes, allowing multiple key presses
 *
 * @author Jeremy Noesen
 */
public class Keyboard {

    /**
     * Set of all keys currently pressed
     */
    private static final Set<KeyCode> pressed = new HashSet<>();

    /**
     * Get a Set of all keys currently pressed by their key codes
     *
     * @return Set of KeyCodes currently pressed
     */
    public static Set<KeyCode> getPressed() {
        return pressed;
    }

    /**
     * Check if a key is pressed
     *
     * @param key KeyCode to check if pressed
     * @return True if the key is pressed
     */
    public static boolean isPressed(KeyCode key) {
        return pressed.contains(key);
    }

    /**
     * Add the event listeners to the main Canvas of the program
     *
     * @param canvas Main Canvas of the program
     */
    public static void initialize(Canvas canvas) {
        canvas.setOnKeyPressed(e -> pressed.add(e.getCode()));
        canvas.setOnKeyReleased(e -> pressed.remove(e.getCode()));
    }
}
