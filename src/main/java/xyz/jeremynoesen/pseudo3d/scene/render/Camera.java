package xyz.jeremynoesen.pseudo3d.scene.render;

import xyz.jeremynoesen.pseudo3d.scene.util.Vector;

import java.util.Objects;

/**
 * Camera used to determine where and how to render a Scene
 *
 * @author Jeremy Noesen
 */
public class Camera {

    /**
     * Field of view of the Camera in degrees
     */
    private float fieldOfView;

    /**
     * How far away the Camera is able to render in grid units
     */
    private float viewDistance;

    /**
     * Camera sensor size
     * <p>
     * Rendered Scenes will scale to fit a square with side length of sensorSize pixels
     */
    private float sensorSize;

    /**
     * Camera zoom
     * <p>
     * Scales the rendered image by this amount
     */
    private float zoom;

    /**
     * Rotation of the Camera in degrees counter-clock-wise
     */
    private float rotation;

    /**
     * 3D position of the Camera in the Scene in grid units
     */
    private Vector position;

    /**
     * 2D position offset of the Camera from the center of the window in pixels
     */
    private Vector offset;

    /**
     * Create a new default Camera
     */
    public Camera() {
        position = new Vector();
        fieldOfView = 90;
        sensorSize = 500;
        viewDistance = 32;
        zoom = 1;
        rotation = 0;
        offset = new Vector();
    }

    /**
     * Create a new Camera with set position, sensor size, field of view, view distance, zoom, rotation, and offset
     *
     * @param position     Camera position in Scene
     * @param sensorSize   Camera sensor size
     * @param fieldOfView  Field of view in degrees
     * @param viewDistance View distance in grid units
     * @param zoom         Camera zoom
     * @param rotation     Rotation of Camera in degrees
     * @param offset       Position of Camera in window
     */
    public Camera(Vector position, Vector offset, float sensorSize, float fieldOfView,
                  float viewDistance, float zoom, float rotation) {
        this.position = position;
        this.fieldOfView = fieldOfView;
        this.sensorSize = sensorSize;
        this.viewDistance = viewDistance;
        this.zoom = zoom;
        this.offset = offset;
        this.rotation = rotation;
    }

    /**
     * Copy constructor for Camera
     *
     * @param camera Camera to copy
     */
    public Camera(Camera camera) {
        position = camera.position;
        fieldOfView = camera.fieldOfView;
        sensorSize = camera.sensorSize;
        viewDistance = camera.viewDistance;
        zoom = camera.zoom;
        offset = camera.offset;
        rotation = camera.rotation;
    }

    /**
     * Get the field of view in degrees
     *
     * @return Field of view in degrees
     */
    public float getFieldOfView() {
        return fieldOfView;
    }

    /**
     * Set the Camera's field of view
     *
     * @param fieldOfView Field of view in degrees
     * @return This Camera
     */
    public Camera setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
        return this;
    }

    /**
     * Get the position of the Camera in the Scene
     *
     * @return Position of the Camera in the Scene
     */
    public Vector getPosition() {
        return position;
    }

    /**
     * Set the Camera's position in the Scene
     *
     * @param position Position Vector
     * @return This Camera
     */
    public Camera setPosition(Vector position) {
        this.position = position;
        return this;
    }

    /**
     * Get the offset of the render from the center of the window
     *
     * @return Render offset
     */
    public Vector getOffset() {
        return offset;
    }

    /**
     * Set the offset of the render from the center of the window
     *
     * @param offset Render offset in pixels
     * @return This Camera
     */
    public Camera setOffset(Vector offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Get the sensor size of the Camera in pixels
     *
     * @return Sensor size in pixels
     */
    public float getSensorSize() {
        return sensorSize;
    }

    /**
     * Set the Camera sensor size in pixels
     *
     * @param sensorSize New sensor size
     * @return This Camera
     */
    public Camera setSensorSize(float sensorSize) {
        this.sensorSize = sensorSize;
        return this;
    }

    /**
     * Get the view distance for the Camera
     *
     * @return View distance
     */
    public float getViewDistance() {
        return viewDistance;
    }

    /**
     * Set the view distance for the Camera
     *
     * @param viewDistance New view distance
     * @return This Camera
     */
    public Camera setViewDistance(float viewDistance) {
        this.viewDistance = viewDistance;
        return this;
    }

    /**
     * Get the zoom for the Camera
     *
     * @return Zoom
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Set the zoom for the Camera
     *
     * @param zoom New zoom
     * @return This Camera
     */
    public Camera setZoom(float zoom) {
        this.zoom = zoom;
        return this;
    }

    /**
     * Get the rotation of the Camera
     *
     * @return Rotation of Camera in degrees counter-clock-wise
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Set the rotation of the Camera
     *
     * @param rotation Rotation of Camera in degrees counter-clock-wise
     * @return This Camera
     */
    public Camera setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Check if a Camera is equal to this one
     *
     * @param o Camera to check
     * @return True if the Camera is equal to this Camera
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Camera camera = (Camera) o;
        return Float.compare(camera.fieldOfView, fieldOfView) == 0 &&
                Float.compare(camera.viewDistance, viewDistance) == 0 &&
                Float.compare(camera.sensorSize, sensorSize) == 0 &&
                Float.compare(camera.zoom, zoom) == 0 &&
                Float.compare(camera.rotation, rotation) == 0 &&
                Objects.equals(position, camera.position) &&
                Objects.equals(offset, camera.offset);
    }
}
