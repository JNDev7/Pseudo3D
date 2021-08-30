package xyz.jeremynoesen.pseudo3d.scene.util;

/**
 * Axes of the grid
 *
 * @author Jeremy Noesen
 */
public enum Axis {
    X, Y, Z;

    /**
     * Get Axis based on Side
     *
     * @param side Side of object
     * @return Axis based on Side
     */
    public static Axis getAxis(Side side) {
        return switch (side) {
            case LEFT, RIGHT -> X;
            case BOTTOM, TOP -> Y;
            case BACK, FRONT -> Z;
        };
    }
}