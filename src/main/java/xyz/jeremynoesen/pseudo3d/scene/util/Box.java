package xyz.jeremynoesen.pseudo3d.scene.util;

/**
 * Box and related mathematical operators
 *
 * @author Jeremy Noesen
 */
public class Box {

    /**
     * Width of the Box
     */
    private float width;

    /**
     * Height of the Box
     */
    private float height;

    /**
     * Depth of the Box
     */
    private float depth;

    /**
     * Minimum point of the Box
     */
    private Vector min;

    /**
     * Maximum point of the Box
     */
    private Vector max;

    /**
     * Position of the center of the Box
     */
    private Vector position;

    /**
     * Create a new default Box
     */
    public Box() {
        width = 0;
        height = 0;
        depth = 0;
        position = new Vector();
        min = new Vector();
        max = new Vector();
    }

    /**
     * Create a new 3D Box with specified width, height, depth, and location
     *
     * @param width    Width of the Box
     * @param height   Height of the Box
     * @param depth    Depth of the Box
     * @param position Location of the Box's center
     */
    public Box(float width, float height, float depth, Vector position) {
        this.width = Math.abs(width);
        this.height = Math.abs(height);
        this.depth = Math.abs(depth);
        this.position = position;
        min = new Vector(position.getX() - (width / 2.0f),
                position.getY() - (height / 2.0f), position.getZ() - (depth / 2.0f));
        max = new Vector(position.getX() + (width / 2.0f),
                position.getY() + (height / 2.0f), position.getZ() + (depth / 2.0f));
    }

    /**
     * Create a new 2D Box with specified width, height, and location
     *
     * @param width    Width of the Box
     * @param height   Height of the Box
     * @param position Location of the Box's center
     */
    public Box(float width, float height, Vector position) {
        this.width = Math.abs(width);
        this.height = Math.abs(height);
        this.depth = 0;
        this.position = position;
        min = new Vector(position.getX() - (width / 2.0f),
                position.getY() - (height / 2.0f), position.getZ() - (depth / 2.0f));
        max = new Vector(position.getX() + (width / 2.0f),
                position.getY() + (height / 2.0f), position.getZ() + (depth / 2.0f));
    }

    /**
     * Copy constructor for Box
     *
     * @param box Box to copy
     */
    public Box(Box box) {
        this.width = box.width;
        this.height = box.height;
        this.depth = box.depth;
        this.position = box.position;
        this.min = box.min;
        this.max = box.max;
    }

    /**
     * Set the dimensions of the Box
     *
     * @param dimensions Dimensions of the Box
     * @return This Box
     */
    public Box setDimensions(Vector dimensions) {
        setWidth(dimensions.getX());
        setHeight(dimensions.getY());
        setDepth(dimensions.getZ());
        return this;
    }

    /**
     * Set the dimensions of the Box for a specific Axis
     *
     * @param axis      Axis to set dimension for
     * @param dimension Dimension in grid units
     * @return This Box
     */
    public Box setDimensions(Axis axis, float dimension) {
        switch (axis) {
            case X -> setWidth(dimension);
            case Y -> setHeight(dimension);
            case Z -> setDepth(dimension);
        }
        return this;
    }

    /**
     * Get the dimensions of the Box for a specific Axis
     *
     * @param axis Axis to get dimension for
     * @return Dimension of the specified Axis
     */
    public float getDimensions(Axis axis) {
        return switch (axis) {
            case X -> getWidth();
            case Y -> getHeight();
            case Z -> getDepth();
        };
    }

    /**
     * Get the width of the Box
     *
     * @return Width of the Box
     */
    public float getWidth() {
        return width;
    }

    /**
     * Set a new width of the Box and recalculate its min and max in the x axis
     *
     * @param width New width of Box
     * @return This Box
     */
    public Box setWidth(float width) {
        this.width = Math.abs(width);
        max = max.setX(position.getX() + (width / 2.0f));
        min = min.setX(position.getX() - (width / 2.0f));
        return this;
    }

    /**
     * Get the height of the Box
     *
     * @return Height of the Box
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set a new height of the Box and recalculate its min and max in the y axis
     *
     * @param height New height of the Box
     * @return This Box
     */
    public Box setHeight(float height) {
        this.height = Math.abs(height);
        max = max.setY(position.getY() + (height / 2.0f));
        min = min.setY(position.getY() - (height / 2.0f));
        return this;
    }

    /**
     * Get the depth of the Box
     *
     * @return Depth of Box
     */
    public float getDepth() {
        return depth;
    }

    /**
     * Set a new depth of the Box and recalculate its min and max in the z axis
     *
     * @param depth New depth of the Box
     * @return This Box
     */
    public Box setDepth(float depth) {
        this.depth = depth;
        max = max.setZ(position.getZ() + (depth / 2.0f));
        min = min.setZ(position.getZ() - (depth / 2.0f));
        return this;
    }

    /**
     * Get the position of the Box
     *
     * @return Box position
     */
    public Vector getPosition() {
        return position;
    }

    /**
     * Set the position of the Box and recalculate its min and max
     *
     * @param position New position
     * @return This Box
     */
    public Box setPosition(Vector position) {
        this.position = position;
        max = new Vector(position.getX() + (width / 2.0f),
                position.getY() + (height / 2.0f), position.getZ() + (depth / 2.0f));
        min = new Vector(position.getX() - (width / 2.0f),
                position.getY() - (height / 2.0f), position.getZ() - (depth / 2.0f));
        return this;
    }

    /**
     * Get the minimum of the Box
     *
     * @return Minimum location
     */
    public Vector getMinimum() {
        return min;
    }

    /**
     * Get the maximum of the Box
     *
     * @return Maximum location
     */
    public Vector getMaximum() {
        return max;
    }

    /**
     * Check if a Box overlaps this Box
     *
     * @param box Box to check for overlap
     * @return True if the Box overlaps this Box
     */
    public boolean overlaps(Box box) {
        return min.getX() <= box.getMaximum().getX() && max.getX() >= box.getMinimum().getX() &&
                min.getY() <= box.getMaximum().getY() && max.getY() >= box.getMinimum().getY() &&
                min.getZ() <= box.getMaximum().getZ() && max.getZ() >= box.getMinimum().getZ();
    }

    /**
     * Check if a Box is inside this Box
     *
     * @param box Box to check
     * @return True if the Box is inside this Box
     */
    public boolean contains(Box box) {
        return min.getX() <= box.getMinimum().getX() && max.getX() >= box.getMaximum().getX() &&
                min.getY() <= box.getMinimum().getY() && max.getY() >= box.getMaximum().getY() &&
                min.getZ() <= box.getMinimum().getZ() && max.getZ() >= box.getMaximum().getZ();
    }

    /**
     * Check if this Box contains a position
     *
     * @param position Position to check
     * @return True of the Box contains this position
     */
    public boolean contains(Vector position) {
        return min.getX() <= position.getX() && max.getX() >= position.getX() &&
                min.getY() <= position.getY() && max.getY() >= position.getY() &&
                min.getZ() <= position.getZ() && max.getZ() >= position.getZ();
    }

    /**
     * Get the volume of the Box
     *
     * @return Volume of the Box
     */
    public float getVolume() {
        return width * height * depth;
    }

    /**
     * Get the surface area of the Box
     *
     * @return Surface area of the Box
     */
    public float getSurfaceArea() {
        return (2 * width * height) + (2 * height * depth) + (2 * width * depth);
    }

    /**
     * Get the face area for a side perpendicular to the specified Axis
     *
     * @param axis Axis to get face area for
     * @return Face area for a side perpendicular to the specified Axis
     */
    public float getFaceArea(Axis axis) {
        return switch (axis) {
            case X -> height * depth;
            case Y -> width * depth;
            case Z -> width * height;
        };
    }

    /**
     * Get the Box represented as a formatted String
     *
     * @return Box in string format
     */
    @Override
    public String toString() {
        return "[" + width + ", " + height + ", " + depth + ", " + position.toString() + "]";
    }

    /**
     * Check if another Box is equal to this Box
     *
     * @param box Box to check for equality
     * @return True if the two Boxes are equal
     */
    @Override
    public boolean equals(Object box) {
        if (this == box) return true;
        if (box == null || getClass() != box.getClass()) return false;
        Box that = (Box) box;
        return Float.compare(that.width, width) == 0 &&
                Float.compare(that.height, height) == 0 &&
                Float.compare(that.depth, depth) == 0 &&
                that.position.equals(position);
    }
}
