package xyz.jeremynoesen.pseudo3d.input;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import xyz.jeremynoesen.pseudo3d.scene.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to keep track of mouse clicks and movement
 *
 * @author Jeremy Noesen
 */
public class Mouse {

    /**
     * Set of all buttons currently pressed
     */
    private static final Set<MouseButton> pressed = new HashSet<>();

    /**
     * Absolute position of the pointer on the screen
     */
    private static Vector position = new Vector();

    /**
     * Get the rotation of the scroll wheel
     * <p>
     * Positive values indicate scrolling up, while negative values indicate scrolling down
     */
    private static float wheelRotation = 0;

    /**
     * Get a Set of all buttons pressed as MouseButtons
     *
     * @return Set of MouseButtons pressed
     */
    public static Set<MouseButton> getPressed() {
        return pressed;
    }

    /**
     * Check if a MouseButton is pressed
     *
     * @param button MouseButton to check if pressed
     * @return True if the MouseButton is pressed
     */
    public static boolean isPressed(MouseButton button) {
        return pressed.contains(button);
    }

    /**
     * Get the rotation of the scroll wheel with speed and direction
     *
     * @return Rotation of the scroll wheel
     */
    public static float getWheelRotation() {
        return wheelRotation;
    }

    /**
     * Get the absolute position of the pointer on the screen
     *
     * @return Absolute position of pointer on the screen
     */
    public static Vector getPosition() {
        return position;
    }

    /**
     * Add the event listeners to the main Canvas of the program
     *
     * @param canvas Main Canvas of the program
     */
    public static void initialize(Canvas canvas) {
        canvas.setOnMouseClicked(e -> pressed.add(e.getButton()));
        canvas.setOnMouseReleased(e -> pressed.remove(e.getButton()));
        canvas.setOnScroll(e -> wheelRotation = (float) e.getDeltaY());
        canvas.setOnMouseMoved(e -> position = new Vector((float) e.getSceneX(), (float) e.getSceneY()));
    }
}
