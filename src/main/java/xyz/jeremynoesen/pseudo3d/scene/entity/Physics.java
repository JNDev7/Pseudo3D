package xyz.jeremynoesen.pseudo3d.scene.entity;

import xyz.jeremynoesen.pseudo3d.scene.util.Axis;
import xyz.jeremynoesen.pseudo3d.scene.util.Box;
import xyz.jeremynoesen.pseudo3d.scene.util.Side;
import xyz.jeremynoesen.pseudo3d.scene.util.Vector;

import java.util.*;

/**
 * Axis-aligned bounding box physics object
 *
 * @author Jeremy Noesen
 */
public abstract class Physics extends Box {

    /**
     * Objects this object is in a Scene with
     */
    private LinkedList<Physics> sceneObjects;

    /**
     * Gravity applied to the object (meters / second ^ 2)
     */
    private Vector gravity;

    /**
     * Position of the object (meters)
     */
    private Vector position;

    /**
     * Velocity of the object (meters / second)
     */
    private Vector velocity;

    /**
     * Acceleration of the object (meters / second ^ 2)
     */
    private Vector acceleration;

    /**
     * Coefficient of drag per side
     */
    private HashMap<Side, Float> drag;

    /**
     * Roughness of the object per side, used for friction
     */
    private HashMap<Side, Float> roughness;

    /**
     * Set of collideable sides
     */
    private final HashSet<Side> collidableSides;

    /**
     * Set of objects colliding with this object per Side
     */
    private final HashMap<Side, HashSet<Physics>> collidingObjects;

    /**
     * Set of objects overlapping this one
     */
    private final HashSet<Physics> overlappingObjects;

    /**
     * Set of kinematic Axes
     */
    private final HashSet<Axis> kinematicAxes;

    /**
     * Set of pushable Axes
     */
    private final HashSet<Axis> pushableAxes;

    /**
     * Mass of the object
     */
    private float mass;

    /**
     * Whether the object can update or not
     */
    private boolean updatable;

    /**
     * Time elapsed in the previous tick
     */
    private float deltaTime;

    /**
     * Temporary Set used in special cases of momentum
     */
    private final HashSet<Axis> skipMomentum;

    /**
     * Temporary Set used in special cases of collisions
     */
    private final HashSet<Physics> specialCollisions;

    /**
     * Create new default Physics object
     */
    public Physics() {
        super();
        gravity = new Vector(0, -9.81f, 0);
        position = new Vector();
        velocity = new Vector();
        acceleration = new Vector();
        drag = new HashMap<>();
        roughness = new HashMap<>();
        sceneObjects = null;
        collidableSides = new HashSet<>(Arrays.asList(Side.values()));
        kinematicAxes = new HashSet<>(Arrays.asList(Axis.values()));
        pushableAxes = new HashSet<>(Arrays.asList(Axis.values()));
        skipMomentum = new HashSet<>();
        specialCollisions = new HashSet<>();
        updatable = true;
        deltaTime = 0;
        mass = 1;
        collidingObjects = new HashMap<>();
        overlappingObjects = new HashSet<>();
        for (Side s : Side.values()) {
            collidingObjects.put(s, new HashSet<>());
            drag.put(s, 0.5f);
            roughness.put(s, 5f);
        }
    }

    /**
     * Copy constructor for Physics objects
     *
     * @param physics Physics object to copy
     */
    public Physics(Physics physics) {
        super(physics);
        gravity = physics.gravity;
        position = physics.position;
        velocity = physics.velocity;
        acceleration = physics.acceleration;
        drag = new HashMap<>();
        roughness = new HashMap<>();
        mass = physics.mass;
        collidableSides = new HashSet<>(physics.collidableSides);
        kinematicAxes = new HashSet<>(physics.kinematicAxes);
        pushableAxes = new HashSet<>(physics.pushableAxes);
        updatable = physics.updatable;
        deltaTime = physics.deltaTime;
        skipMomentum = new HashSet<>();
        specialCollisions = new HashSet<>();
        collidingObjects = new HashMap<>();
        overlappingObjects = new HashSet<>(physics.overlappingObjects);
        sceneObjects = physics.sceneObjects;
        for (Side s : Side.values()) {
            collidingObjects.put(s, new HashSet<>(physics.collidingObjects.get(s)));
            drag.put(s, physics.drag.get(s));
            roughness.put(s, physics.roughness.get(s));
        }
    }

    /**
     * Update the motion of the object
     *
     * @param deltaTime Time elapsed in the previous tick
     */
    public void tickMotion(float deltaTime) {
        if (!updatable || !isKinematic()) return;
        this.deltaTime = deltaTime;
        applyAcceleration();
        applyFriction();
        applyDrag();
        applyMomentum();
        applyVelocity();
    }

    /**
     * Apply acceleration and gravity to the velocity
     */
    private void applyAcceleration() {
        for (Axis axis : kinematicAxes) {
            velocity = velocity.set(axis, velocity.get(axis) + (acceleration.get(axis) + gravity.get(axis) * deltaTime));
        }
    }

    /**
     * Apply the effect of friction to the velocity
     */
    private void applyFriction() {
        Vector friction = calculateFriction();

        float vx = velocity.getX(), vy = velocity.getY(), vz = velocity.getZ();
        float fx = friction.getX(), fy = friction.getY(), fz = friction.getZ();

        if (isKinematic(Axis.X)) {
            if (vx < 0) vx = Math.min(vx + fy + fz, 0);
            else if (vx > 0) vx = Math.max(vx - fy - fz, 0);
        }
        if (isKinematic(Axis.Y)) {
            if (vy < 0) vy = Math.min(vy + fx + fz, 0);
            else if (vy > 0) vy = Math.max(vy - fx - fz, 0);
        }
        if (isKinematic(Axis.Z)) {
            if (vz < 0) vz = Math.min(vz + fx + fy, 0);
            else if (vz > 0) vz = Math.max(vz - fx - fy, 0);
        }

        velocity = new Vector(vx, vy, vz);
    }

    /**
     * Get the friction Vector for the object
     *
     * @return Friction Vector
     */
    private Vector calculateFriction() {
        Vector output = new Vector();
        for (Side side : getCollidingSides()) {
            Axis axis = Side.getNormalAxis(side);

            if (isKinematic(axis)) {
                float f = 0;
                int count = 0;

                for (Physics physics : collidingObjects.get(side)) {
                    if (physics.updatable) {
                        f += physics.roughness.get(side) * Math.abs(velocity.get(axis) - physics.getVelocity().get(axis));
                        count++;
                    }
                }

                if (count > 0) {
                    f = ((f + roughness.get(side)) / (count + 1)) * calculateStackedMass(side) * deltaTime;
                    output = output.set(axis, output.get(axis) + f);
                }
            }
        }
        return output;
    }

    /**
     * Sum the masses of stacked objects
     *
     * @param side Side of object to sum stacked masses
     * @return Sum of stacked masses
     */
    private float calculateStackedMass(Side side) {
        float totalMass = 0;
        ArrayDeque<Physics> current = new ArrayDeque<>();
        HashSet<Physics> visited = new HashSet<>();
        current.add(this);

        while (!current.isEmpty()) {
            Physics physics = current.poll();

            if (!visited.contains(physics)) {
                totalMass += physics.mass;
                for (Physics colliding : physics.collidingObjects.get(Side.getOpposite(side)))
                    if (colliding.updatable && colliding.isKinematic(Side.getNormalAxis(side)))
                        current.add(colliding);
                visited.add(physics);
            }
        }
        return totalMass;
    }

    /**
     * Apply the effect of drag to the velocity
     */
    private void applyDrag() {
        for (Axis axis : kinematicAxes) {
            float v = velocity.get(axis);
            if (v != 0) {
                float d = drag.get(Side.getFromNormal(axis, v)) * getFaceArea(Side.getFromNormal(axis, 1))
                        * deltaTime * Math.abs(velocity.get(axis));

                if (v < 0)
                    velocity = velocity.set(axis, Math.min(v + d, 0));
                else if (v > 0)
                    velocity = velocity.set(axis, Math.max(v - d, 0));
            }
        }
    }

    /**
     * Apply the effects of momentum to the velocity
     */
    private void applyMomentum() {
        skipMomentum.clear();
        for (Axis axis : kinematicAxes) {
            float v = velocity.get(axis);
            Side side = Side.getFromNormal(axis, v);
            if (side != null) {
                for (Physics physics : collidingObjects.get(side)) {

                    if (physics.updatable) {
                        if (physics.isKinematic(axis) && physics.isPushable(axis)) {

                            float sum = mass + physics.mass;
                            float diff = mass - physics.mass;
                            float v1 = v;
                            float v2 = physics.velocity.get(axis);
                            v = ((diff / sum) * v1) + ((2 * physics.mass / sum) * v2);
                            if (!physics.skipMomentum.contains(axis) || velocity.get(axis) != 0)
                                physics.velocity =
                                        physics.velocity.set(axis, ((-diff / sum) * v2) + ((2 * mass / sum) * v1));

                        } else {
                            v = 0;
                            skipMomentum.add(axis);
                        }
                    }
                }
            }
            velocity = velocity.set(axis, v);
        }
    }

    /**
     * Update the position of the object based on the velocity
     */
    private void applyVelocity() {
        for (Axis axis : kinematicAxes) {
            setPosition(position.set(axis, position.get(axis) + velocity.multiply(deltaTime).get(axis)));
        }
    }

    /**
     * Check if an object has collided with this object
     */
    public void tickCollisions() {
        if (!updatable || sceneObjects == null) return;
        resetCollisions();
        for (Physics physics : sceneObjects) {
            if (physics != this && physics.updatable && super.overlaps(physics)) {
                if (isCollideable()) {
                    collide(physics);
                } else {
                    overlap(physics);
                }
            }
        }
    }

    /**
     * Reset all collision data
     */
    private void resetCollisions() {
        collidingObjects.values().forEach(HashSet::clear);
        overlappingObjects.clear();
        specialCollisions.clear();
    }

    /**
     * Fix the position of this object to make a collision occur
     *
     * @param physics Object colliding with this object
     */
    private void collide(Physics physics) {
        float[] overlaps = new float[6];
        overlaps[0] = Math.abs(getMinimum().getX() - physics.getMaximum().getX()); //left overlap
        overlaps[1] = Math.abs(getMaximum().getX() - physics.getMinimum().getX()); //right overlap
        overlaps[2] = Math.abs(getMinimum().getY() - physics.getMaximum().getY()); //bottom overlap
        overlaps[3] = Math.abs(getMaximum().getY() - physics.getMinimum().getY()); //top overlap
        overlaps[4] = Math.abs(getMinimum().getZ() - physics.getMaximum().getZ()); //back overlap
        overlaps[5] = Math.abs(getMaximum().getZ() - physics.getMinimum().getZ()); //front overlap

        float distance = overlaps[0];
        Side side = Side.LEFT;
        byte zeros = 0;

        for (int i = 0; i < 6; i++) {
            if (overlaps[i] < distance) {
                distance = overlaps[i];
                side = switch (i) {
                    case 1 -> Side.RIGHT;
                    case 2 -> Side.BOTTOM;
                    case 3 -> Side.TOP;
                    case 4 -> Side.BACK;
                    case 5 -> Side.FRONT;
                    default -> Side.LEFT;
                };
            }
            if (overlaps[i] == 0) zeros++;
        }
        if (zeros > 1) return;

        Axis axis = Side.getNormalAxis(side);

        if (isCollideable(side) && physics.isCollideable(Side.getOpposite(side))) {
            if (isKinematic(axis) && Math.signum(velocity.get(axis)) == Math.signum(Side.getNormalVector(side).get(axis))) {

                if (Math.signum(velocity.get(axis)) == -Math.signum(physics.velocity.get(axis))
                        && !physics.specialCollisions.contains(this)) {
                    distance *= velocity.get(axis) / (velocity.get(axis) - physics.velocity.get(axis));
                    specialCollisions.add(physics);
                }

                for (Axis axes : kinematicAxes) {
                    setPosition(position.set(axes,
                            position.get(axes) - (velocity.get(axes) * Math.abs(distance / velocity.get(axis)))));
                }

            }
            collidingObjects.get(side).add(physics);
        } else {
            overlap(physics);
        }
    }

    /**
     * Set this object as overlapping another
     *
     * @param physics Object to overlap with
     */
    private void overlap(Physics physics) {
        overlappingObjects.add(physics);
    }

    /**
     * Get the position of the object
     *
     * @return Position Vector of the object
     */
    public Vector getPosition() {
        return position;
    }

    /**
     * Set the position of the object
     *
     * @param position Position Vector
     * @return This Physics object
     */
    public Physics setPosition(Vector position) {
        this.position = position;
        super.setPosition(position);
        return this;
    }

    /**
     * Get the velocity of the object
     *
     * @return Velocity Vector of the object
     */
    public Vector getVelocity() {
        return velocity;
    }

    /**
     * Set the velocity of the object
     *
     * @param velocity Velocity Vector
     * @return This Physics object
     */
    public Physics setVelocity(Vector velocity) {
        this.velocity = velocity;
        return this;
    }

    /**
     * Get the acceleration of the object
     *
     * @return Acceleration Vector of the object
     */
    public Vector getAcceleration() {
        return acceleration;
    }

    /**
     * Set the acceleration of the object
     *
     * @param acceleration Acceleration Vector
     * @return This Physics object
     */
    public Physics setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    /**
     * Get the gravity applied to the object
     *
     * @return Gravity Vector
     */
    public Vector getGravity() {
        return gravity;
    }

    /**
     * Set the gravity applied to the object
     *
     * @param gravity Gravity Vector
     * @return This Physics object
     */
    public Physics setGravity(Vector gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * Get the drag coefficient of the object for a Side
     *
     * @param side Side to get drag coefficient of
     * @return Drag coefficient of the object Side
     */
    public float getDrag(Side side) {
        return drag.get(side);
    }

    /**
     * Set the drag coefficient for the object on specific Sides
     * <br>
     * Specify no Sides to apply to all Sides
     *
     * @param drag Drag coefficient
     * @param side Sides to set drag for
     * @return This Physics object
     */
    public Physics setDrag(float drag, Side... side) {
        for (Side sides : side.length > 0 ? side : Side.values()) this.drag.put(sides, drag);
        return this;
    }

    /**
     * Set the drag coefficients for all Sides
     *
     * @param left Drag coefficient for the left Side
     * @param right Drag coefficient for the right Side
     * @param bottom Drag coefficient for the bottom Side
     * @param top Drag coefficient for the top Side
     * @param back Drag coefficient for the back Side
     * @param front Drag coefficient for the front Side
     * @return This Physics object
     */
    public Physics setDrag(float left, float right, float bottom, float top, float back, float front) {
        drag.put(Side.LEFT, left);
        drag.put(Side.RIGHT, right);
        drag.put(Side.BOTTOM, bottom);
        drag.put(Side.TOP, top);
        drag.put(Side.BACK, back);
        drag.put(Side.FRONT, front);
        return this;
    }

    /**
     * Get the roughness of the object for a Side
     *
     * @param side Side to get roughness of
     * @return Roughness of the object Side
     */
    public float getRoughness(Side side) {
        return roughness.get(side);
    }

    /**
     * Set the roughness for the object on specific Sides
     * <br>
     * Specify no Sides to apply to all Sides
     *
     * @param roughness Roughness
     * @param side Sides to set roughness for
     * @return This Physics object
     */
    public Physics setRoughness(float roughness, Side... side) {
        for (Side sides : side.length > 0 ? side : Side.values()) this.roughness.put(sides, roughness);
        return this;
    }

    /**
     * Set the roughness for all Sides
     *
     * @param left Roughness for the left Side
     * @param right Roughness for the right Side
     * @param bottom Roughness for the bottom Side
     * @param top Roughness for the top Side
     * @param back Roughness for the back Side
     * @param front Roughness for the front Side
     * @return This Physics object
     */
    public Physics setRoughness(float left, float right, float bottom, float top, float back, float front) {
        roughness.put(Side.LEFT, left);
        roughness.put(Side.RIGHT, right);
        roughness.put(Side.BOTTOM, bottom);
        roughness.put(Side.TOP, top);
        roughness.put(Side.BACK, back);
        roughness.put(Side.FRONT, front);
        return this;
    }

    /**
     * Get the mass of the object
     *
     * @return Mass of the object
     */
    public float getMass() {
        return mass;
    }

    /**
     * Set the mass of the object
     *
     * @param mass New mass for the object
     * @return This Physics object
     */
    public Physics setMass(float mass) {
        this.mass = mass;
        return this;
    }

    /**
     * Check which Sides the object is collideable on
     * <br>
     * Specify no Sides to check if collideable in general
     *
     * @return True if collideable on all specified Sides
     */
    public boolean isCollideable(Side... side) {
        if (side.length == 0) return !collidableSides.isEmpty();
        return collidableSides.containsAll(Arrays.asList(side));
    }


    /**
     * Set the Sides the object can collide on
     * <br>
     * Specify no Sides to make non-collideable
     *
     * @param side Sides to set collidable
     * @return This Physics object
     */
    public Physics setCollideable(Side... side) {
        collidableSides.clear();
        collidableSides.addAll(Arrays.asList(side));
        return this;
    }

    /**
     * Get the Sides the object can collide on
     * <br>
     *
     * @return HashSet of Sides the Object can collide on
     */
    public HashSet<Side> getCollideableSides() {
        return collidableSides;
    }

    /**
     * Check which Axes the object is kinematic on
     * <br>
     * Specify no Axes to check if kinematic in general
     *
     * @return True if kinematic on all specified Axes
     */
    public boolean isKinematic(Axis... axis) {
        if (axis.length == 0) return !kinematicAxes.isEmpty();
        return kinematicAxes.containsAll(Arrays.asList(axis));
    }

    /**
     * Set the Axes the object can move on
     * <br>
     * Specify no Axes to make non-kinematic
     *
     * @param axis Axes to set as kinematic
     * @return This Physics object
     */
    public Physics setKinematic(Axis... axis) {
        kinematicAxes.clear();
        kinematicAxes.addAll(Arrays.asList(axis));
        return this;
    }

    /**
     * Get the Axes the object is kinematic on
     *
     * @return HashSet of Axes the object is kinematic on
     */
    public HashSet<Axis> getKinematicAxes() {
        return kinematicAxes;
    }

    /**
     * Check which Axes the object is pushable on
     * <br>
     * Specify no Axes to check if pushable in general
     *
     * @return True if pushable on all specified Axes
     */
    public boolean isPushable(Axis... axis) {
        if (axis.length == 0) return !pushableAxes.isEmpty();
        return pushableAxes.containsAll(Arrays.asList(axis));
    }

    /**
     * Set the Axes the object can be pushed on
     * <br>
     * Specify no Axes to make non-pushable
     *
     * @param axis Axes to set as pushable
     * @return This Physics object
     */
    public Physics setPushable(Axis... axis) {
        pushableAxes.clear();
        pushableAxes.addAll(Arrays.asList(axis));
        return this;
    }

    /**
     * Get the Axes the object is pushable on
     *
     * @return HashSet of Axes the object is pushable on
     */
    public HashSet<Axis> getPushableAxes() {
        return pushableAxes;
    }

    /**
     * Check if the object is currently colliding with another object
     *
     * @return True if the object is colliding with another object
     */
    public boolean isColliding() {
        for (Side side : Side.values()) {
            if (isColliding(side)) return true;
        }
        return false;
    }

    /**
     * Check if one or more specific objects are colliding with this object
     *
     * @param physics objects to check if colliding with
     * @return True if this object collides with the other objects
     */
    public boolean isColliding(Physics... physics) {
        for (HashSet<Physics> list : collidingObjects.values()) {
            if (list.containsAll(Arrays.asList(physics))) return true;
        }
        return false;
    }

    /**
     * Check if this object is colliding on the specified Sides
     *
     * @param side Sides of the object
     * @return True if the object is colliding on the Sides
     */
    public boolean isColliding(Side... side) {
        for (Side s : side) {
            if (collidingObjects.get(s).isEmpty()) return false;
        }
        return true;
    }

    /**
     * Check if a specific object collides with this one on a specific Side
     *
     * @param physics Object to check if colliding with
     * @param side    Side of object
     * @return True if the object is colliding with the other object on the specified Side
     */
    public boolean isColliding(Physics physics, Side side) {
        return collidingObjects.get(side).contains(physics);
    }

    /**
     * Get all objects colliding on the specified Sides
     * <br>
     * Specify no Sides to get all objects colliding with this object
     *
     * @param side Sides to get colliding objects of
     * @return Set of all objects colliding on the Sides
     */
    public HashSet<Physics> getCollidingObjects(Side... side) {
        HashSet<Physics> objects = new HashSet<>();
        for (Side s : (side.length > 0 ? side : Side.values())) {
            objects.addAll(collidingObjects.get(s));
        }
        return objects;
    }

    /**
     * Get all the Sides the object is colliding on
     *
     * @return Set of all colliding Sides
     */
    public HashSet<Side> getCollidingSides() {
        HashSet<Side> sides = new HashSet<>();
        for (Side side : Side.values()) if (isColliding(side)) sides.add(side);
        return sides;
    }

    /**
     * Check if this object overlaps one or more objects
     *
     * @param physics Objects to check for overlap
     * @return True if this object overlaps the specified objects
     */
    public boolean isOverlapping(Physics... physics) {
        if (physics.length == 0) return !overlappingObjects.isEmpty();
        return overlappingObjects.containsAll(Arrays.asList(physics));
    }

    /**
     * Get the set of all objects overlapping this one
     *
     * @return Set of overlapping objects
     */
    public HashSet<Physics> getOverlappingObjects() {
        return overlappingObjects;
    }

    /**
     * Set if the object can update
     * <br>
     * This is only able to be called by the parent class
     *
     * @param updatable True to allow updating
     */
    protected void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    /**
     * Check if the object can update
     * <br>
     * This is only able to be called by the parent class
     *
     * @return True if object can update
     */
    protected boolean isUpdatable() {
        return updatable;
    }

    /**
     * Set the object this object is in a Scene with
     * <br>
     * This is only able to be called by the parent class
     *
     * @param objects List of Physics objects
     */
    @SuppressWarnings("unchecked")
    protected void setSceneObjects(LinkedList<? extends Physics> objects) {
        this.sceneObjects = (LinkedList<Physics>) objects;
    }

    /**
     * Check if another set of Physics data is equal to this one
     *
     * @param o Physics to check for equality
     * @return True if other Physics data is equivalent to this
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Physics physics = (Physics) o;
        return Float.compare(physics.mass, mass) == 0 &&
                updatable == physics.updatable &&
                Objects.equals(gravity, physics.gravity) &&
                Objects.equals(position, physics.position) &&
                Objects.equals(velocity, physics.velocity) &&
                Objects.equals(acceleration, physics.acceleration) &&
                Objects.equals(drag, physics.drag) &&
                Objects.equals(roughness, physics.roughness) &&
                Objects.equals(collidingObjects, physics.collidingObjects) &&
                Objects.equals(overlappingObjects, physics.overlappingObjects) &&
                Objects.equals(kinematicAxes, physics.kinematicAxes) &&
                Objects.equals(pushableAxes, physics.pushableAxes) &&
                Objects.equals(collidableSides, physics.collidableSides);
    }
}
