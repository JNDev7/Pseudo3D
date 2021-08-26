package xyz.jeremynoesen.pseudo3d.scene.render;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import xyz.jeremynoesen.pseudo3d.scene.util.Axis;
import xyz.jeremynoesen.pseudo3d.scene.util.Box;
import xyz.jeremynoesen.pseudo3d.scene.util.Vector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Sprites to be rendered in game
 *
 * @author Jeremy Noesen
 */
public class Sprite {

    /**
     * Image used for the Sprite
     */
    private Image image;

    /**
     * Width of the Sprite in grid units
     */
    private float width;

    /**
     * Height of the Sprite in grid units
     */
    private float height;

    /**
     * Counter-clock-wise rotation of the Sprite in degrees
     */
    private float rotation;

    /**
     * Current frame of animation
     */
    private float currentFrame;

    /**
     * All images of the animation
     */
    private ArrayList<Image> images;

    /**
     * Time between frames
     */
    private float frameStep;

    /**
     * Whether the Sprite animation should loop or not
     */
    private boolean loop;

    /**
     * Whether the Sprite animation is paused or not
     */
    private boolean paused;

    /**
     * Create a new image Sprite
     *
     * @param width  Sprite width in grid units
     * @param height Sprite height in grid units
     * @param src    Path to image to use for Sprite
     */
    public Sprite(float width, float height, String src) throws FileNotFoundException {
        this.image = new Image(new FileInputStream(src));
        this.width = width;
        this.height = height;
        this.rotation = 0;
    }

    /**
     * Create a new color Sprite with specified dimensions and color
     *
     * @param width  Sprite width in grid units
     * @param height Sprite height in grid units
     * @param color  Sprite Color
     */
    public Sprite(float width, float height, Color color) {
        this.image = new WritableImage(1, 1);
        this.width = width;
        this.height = height;
        this.rotation = 0;
        ((WritableImage) image).getPixelWriter().setColor(0, 0, color);
    }

    /**
     * Create a new animated Sprite from a list of images and specified frame rate, width, height, and loop status
     *
     * @param src       Path to all images of the animated Sprite
     * @param frameRate Frames per second of the Sprite
     * @param width     Sprite width in grid units
     * @param height    Sprite height in grid units
     * @param loop      True to allow Sprite to loop
     */
    public Sprite(float width, float height, float frameRate, boolean loop, String[] src) throws FileNotFoundException {
        ArrayList<Image> images = new ArrayList<>();
        for (String s : src) {
            images.add(new Image(new FileInputStream(s)));
        }
        this.images = images;
        this.image = images.get(0);
        this.width = width;
        this.height = height;
        this.rotation = 0;
        this.frameStep = 1 / frameRate;
        this.currentFrame = 0;
        this.loop = loop;
    }

    /**
     * Copy constructor for Sprites
     *
     * @param sprite Sprite to copy
     */
    public Sprite(Sprite sprite) {
        image = sprite.image;
        width = sprite.width;
        height = sprite.height;
        rotation = sprite.rotation;
        if (sprite.images != null) images = new ArrayList<>(sprite.images);
        frameStep = sprite.frameStep;
        currentFrame = sprite.currentFrame;
        loop = sprite.loop;
        paused = sprite.paused;
    }

    /**
     * Get the Sprite Image
     *
     * @return Sprite Image
     */
    public Image getImage() {
        return image;
    }

    /**
     * Set the dimensions of the Sprite
     *
     * @param dimensions 2D Vector for dimensions
     * @return This Sprite
     */
    public Sprite setDimensions(Vector dimensions) {
        setWidth(dimensions.getX());
        setHeight(dimensions.getY());
        return this;
    }

    /**
     * Set the dimensions of the Sprite for a specific Axis
     *
     * @param axis      Axis to set dimension for
     * @param dimension Dimension in grid units
     * @return This Box
     */
    public Sprite setDimensions(Axis axis, float dimension) {
        switch (axis) {
            case X -> setWidth(dimension);
            case Y -> setHeight(dimension);
        }
        return this;
    }

    /**
     * Get the dimensions of the Sprite for a specific Axis
     *
     * @param axis Axis to get dimension for
     * @return Dimension of the specified Axis
     */
    public float getDimensions(Axis axis) {
        return switch (axis) {
            case X -> getWidth();
            case Y -> getHeight();
            case Z -> 0;
        };
    }

    /**
     * Get the width of the Sprite in grid units
     *
     * @return Width of the Sprite
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get the height of the Sprite in grid units
     *
     * @return Height of the Sprite
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set the width of the Sprite
     *
     * @param width New width in grid units
     * @return This Sprite
     */
    public Sprite setWidth(float width) {
        this.width = width;
        return this;
    }

    /**
     * Set the height of the Sprite
     *
     * @param height New height in grid units
     * @return This Sprite
     */
    public Sprite setHeight(float height) {
        this.height = height;
        return this;
    }

    /**
     * Set the rotation of the Sprite
     *
     * @param rotation Rotation in degrees counter-clock-wise
     * @return This Sprite
     */
    public Sprite setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Get the rotation of the Sprite
     *
     * @return Rotation in degrees counter-clock-wise
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Get the framerate of the Sprite
     *
     * @return Animation framerate
     */
    public float getFramerate() {
        return 1 / frameStep;
    }

    /**
     * Set the framerate for the Sprite
     *
     * @param framerate New framerate
     * @return This Sprite
     */
    public Sprite setFramerate(float framerate) {
        frameStep = 1 / framerate;
        return this;
    }

    /**
     * Set the Sprite animation to loop or not
     *
     * @param loop True to allow animation loop
     * @return This Sprite
     */
    public Sprite setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    /**
     * Check if the Sprite can loop animation
     *
     * @return True if the animation can loop
     */
    public boolean canLoop() {
        return loop;
    }

    /**
     * Set the animation to be paused
     *
     * @param paused True to pause the animation
     * @return This Sprite
     */
    public Sprite setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    /**
     * Check if the animation is paused
     *
     * @return True if paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Set the current frame to the next available frame based on elapsed time
     * <p>
     * This is usually called by the renderer
     *
     * @param deltaTime Time elapsed for the render frame
     */
    public void update(float deltaTime) {
        if (!paused && images != null && !images.isEmpty() && frameStep != 0) {
            try {
                currentFrame = currentFrame + (deltaTime / frameStep);
                image = images.get(getFrame());
            } catch (IndexOutOfBoundsException e) {
                if (loop) {
                    currentFrame = frameStep > 0 ? (currentFrame - images.size()) : (currentFrame + images.size());
                    image = images.get(getFrame());
                } else {
                    currentFrame = frameStep > 0 ? 0 : images.size();
                    paused = true;
                }
            }
        }
    }

    /**
     * Set the current frame to the next frame
     *
     * @return This Sprite
     */
    public Sprite nextFrame() {
        boolean paused = this.paused;
        setPaused(false);
        update(Math.abs(frameStep));
        setPaused(paused);
        return this;
    }

    /**
     * Set the current frame to the previous frame
     *
     * @return This Sprite
     */
    public Sprite previousFrame() {
        boolean paused = this.paused;
        float frameStep = this.frameStep;
        setPaused(false);
        this.frameStep = -this.frameStep;
        update(Math.abs(frameStep));
        setPaused(paused);
        this.frameStep = frameStep;
        return this;
    }

    /**
     * Get the current frame of the animation
     *
     * @return Current frame of the animation
     */
    public int getFrame() {
        return frameStep > 0 ? (int) Math.floor(currentFrame) : (int) Math.ceil(currentFrame);
    }

    /**
     * Set the current frame in the animation
     *
     * @param frame Frame number
     * @return This Sprite
     */
    public Sprite setFrame(int frame) {
        currentFrame = frame;
        image = images.get(frame);
        return this;
    }

    /**
     * Check if two Sprites are similar to each other
     *
     * @param o Sprite to check
     * @return True if the two Sprites are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sprite sprite = (Sprite) o;
        return Float.compare(sprite.width, width) == 0 &&
                Float.compare(sprite.height, height) == 0 &&
                Float.compare(sprite.rotation, rotation) == 0 &&
                Double.compare(sprite.currentFrame, currentFrame) == 0 &&
                Double.compare(sprite.frameStep, frameStep) == 0 &&
                Objects.equals(image, sprite.image) &&
                Objects.equals(images, sprite.images) &&
                Objects.equals(loop, sprite.loop) &&
                Objects.equals(paused, sprite.paused);
    }
}
