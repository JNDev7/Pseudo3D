package xyz.jeremynoesen.pseudo3d.scene.entity;

import xyz.jeremynoesen.pseudo3d.scene.util.Axis;
import xyz.jeremynoesen.pseudo3d.scene.util.Box;
import xyz.jeremynoesen.pseudo3d.scene.util.Side;
import xyz.jeremynoesen.pseudo3d.scene.util.Vector;

import java.util.*;

/**
 * Axis-aligned bounding box physics
 *
 * @author Jeremy Noesen
 */
public abstract class Physics extends Box {

    /**
     * Entities this Entity is in a Scene with
     */
    private LinkedList<Physics> entities;

    /**
     * Gravity applied to the Entity (meters / second ^ 2)
     */
    private Vector gravity;

    /**
     * Position of the Entity (meters)
     */
    private Vector position;

    /**
     * Velocity of the Entity (meters / second)
     */
    private Vector velocity;

    /**
     * Terminal velocity of the Entity (meters / second)
     * <p>
     * This will limit how fast the Entity can go based on gravity and acceleration
     */
    private Vector terminalVelocity;

    /**
     * Acceleration of the Entity (meters / second ^ 2)
     */
    private Vector acceleration;

    /**
     * Coefficient of drag per axis
     */
    private Vector drag;

    /**
     * Roughness of the Entity per axis, used for friction
     */
    private Vector roughness;

    /**
     * Set of collideable sides
     */
    private final HashSet<Side> collidableSides;

    /**
     * Set of Entities colliding with this Entity with per Side
     */
    private final HashMap<Side, HashSet<Physics>> collidingEntities;

    /**
     * Set of Entities overlapping this one
     */
    private final HashSet<Physics> overlappingEntities;

    /**
     * Set of kinematic Axes
     */
    private final HashSet<Axis> kinematicAxes;

    /**
     * Set of pushable Axes
     */
    private final HashSet<Axis> pushableAxes;

    /**
     * Mass of the Entity
     */
    private float mass;

    /**
     * Whether the Entity can update or not
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
     * Create new default Physics
     */
    public Physics() {
        super();
        gravity = new Vector(0, -9.81f, 0);
        position = new Vector();
        velocity = new Vector();
        acceleration = new Vector();
        terminalVelocity = new Vector(20, 20, 20);
        drag = new Vector(0.5f, 0.5f, 0.5f);
        roughness = new Vector(5, 5, 5);
        entities = null;
        collidableSides = new HashSet<>(Arrays.asList(Side.values()));
        kinematicAxes = new HashSet<>(Arrays.asList(Axis.values()));
        pushableAxes = new HashSet<>(Arrays.asList(Axis.values()));
        skipMomentum = new HashSet<>();
        specialCollisions = new HashSet<>();
        updatable = true;
        deltaTime = 0;
        mass = 1;
        collidingEntities = new HashMap<>();
        overlappingEntities = new HashSet<>();
        for (Side s : Side.values()) collidingEntities.put(s, new HashSet<>());
    }

    /**
     * Copy constructor for Physics
     *
     * @param physics Physics to copy
     */
    public Physics(Physics physics) {
        super(physics);
        gravity = physics.gravity;
        position = physics.position;
        velocity = physics.velocity;
        acceleration = physics.acceleration;
        terminalVelocity = physics.terminalVelocity;
        drag = physics.drag;
        roughness = physics.roughness;
        mass = physics.mass;
        collidableSides = new HashSet<>(physics.collidableSides);
        kinematicAxes = new HashSet<>(physics.kinematicAxes);
        pushableAxes = new HashSet<>(physics.pushableAxes);
        updatable = physics.updatable;
        deltaTime = physics.deltaTime;
        skipMomentum = new HashSet<>();
        specialCollisions = new HashSet<>();
        collidingEntities = new HashMap<>();
        overlappingEntities = new HashSet<>(physics.overlappingEntities);
        entities = physics.entities;
        for (Side s : Side.values()) collidingEntities.put(s, new HashSet<>(physics.collidingEntities.get(s)));
    }

    /**
     * Update the motion of the Entity
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
        updatePosition();
    }

    /**
     * Apply acceleration and gravity to the velocity
     */
    private void applyAcceleration() {
        for (Axis axis : Axis.values()) {
            if (!isKinematicOn(axis)) continue;

            float v = velocity.get(axis);
            float a = acceleration.get(axis) + gravity.get(axis);
            float vt = terminalVelocity.get(axis);

            if (v > -vt && a < 0)
                velocity = velocity.set(axis, Math.max(v + (a * deltaTime), -vt));
            else if (v < vt && a > 0)
                velocity = velocity.set(axis, Math.min(v + (a * deltaTime), vt));
        }
    }

    /**
     * Apply the effect of friction to the velocity
     */
    private void applyFriction() {
        Vector friction = calculateFriction();

        float vx = velocity.getX(), vy = velocity.getY(), vz = velocity.getZ();
        float fx = friction.getX(), fy = friction.getY(), fz = friction.getZ();

        if (isKinematicOn(Axis.X)) {
            if (vx < 0) vx = Math.min(vx + fy + fz, 0);
            else if (vx > 0) vx = Math.max(vx - fy - fz, 0);
        }
        if (isKinematicOn(Axis.Y)) {
            if (vy < 0) vy = Math.min(vy + fx + fz, 0);
            else if (vy > 0) vy = Math.max(vy - fx - fz, 0);
        }
        if (isKinematicOn(Axis.Z)) {
            if (vz < 0) vz = Math.min(vz + fx + fy, 0);
            else if (vz > 0) vz = Math.max(vz - fx - fy, 0);
        }

        velocity = new Vector(vx, vy, vz);
    }

    /**
     * Get the friction Vector for the Entity
     *
     * @return Friction Vector
     */
    private Vector calculateFriction() {
        Vector output = new Vector();
        for (Axis axis : Axis.values()) {
            if (!isKinematicOn(axis)) continue;

            float v = velocity.get(axis);

            if (collidesOn(axis)) {
                float f = 0;
                int count = 0;
                Side side = Side.getSide(axis, v);
                float stackedMass = calculateStackedMass(v, axis);

                if (side != null && collidesOn(side)) {
                    for (Physics physics : collidingEntities.get(side)) {
                        if (physics.updatable) {
                            f += physics.roughness.get(axis) * Math.abs(v - physics.getVelocity().get(axis));
                            count++;
                        }
                    }
                }

                if (count > 0) f = ((f + roughness.get(axis)) / (count + 1)) * stackedMass * deltaTime;
                output = output.set(axis, f);

                f = 0;
                count = 0;

                Side opposite = Side.getSide(axis, -v);
                if (opposite != null && collidesOn(opposite)) {
                    for (Physics physics : collidingEntities.get(opposite)) {
                        if (physics.updatable && Math.signum(physics.getVelocity().get(axis)) == Math.signum(v)) {
                            f += physics.roughness.get(axis) * Math.abs(physics.getVelocity().get(axis) - v);
                            count++;
                        }
                    }
                }

                if (count > 0) f = ((f + roughness.get(axis)) / (count + 1)) * (stackedMass - mass) * deltaTime;
                output = output.set(axis, output.get(axis) + f);
            }
        }
        return output;
    }

    /**
     * Sum the masses of stacked Entities
     *
     * @param velocity Input velocity
     * @param axis     Axis of motion
     * @return Sum of masses
     */
    private float calculateStackedMass(float velocity, Axis axis) {
        float totalMass = 0;
        Queue<Physics> current = new ArrayDeque<>();
        Set<Physics> visited = new HashSet<>();
        current.add(this);

        while (!current.isEmpty()) {
            Physics physics = current.poll();

            if (!visited.contains(physics)) {
                totalMass += physics.mass;
                Side side = Side.getSide(axis, -velocity);
                if (side != null) {
                    for (Physics colliding : physics.getCollidingEntities(side))
                        if (colliding.updatable && colliding.isKinematicOn(axis)) current.add(colliding);
                }
                visited.add(physics);
            }
        }
        return totalMass;
    }

    /**
     * Apply the effect of drag to the velocity
     */
    private void applyDrag() {
        Vector drag = calculateDrag();

        float vx = velocity.getX(), vy = velocity.getY(), vz = velocity.getZ();
        float dx = drag.getX(), dy = drag.getY(), dz = drag.getZ();

        if (isKinematicOn(Axis.X)) {
            if (vx < 0) vx = Math.min(vx + dx, 0);
            else if (vx > 0) vx = Math.max(vx - dx, 0);
        }
        if (isKinematicOn(Axis.Y)) {
            if (vy < 0) vy = Math.min(vy + dy, 0);
            else if (vy > 0) vy = Math.max(vy - dy, 0);
        }
        if (isKinematicOn(Axis.Z)) {
            if (vz < 0) vz = Math.min(vz + dz, 0);
            else if (vz > 0) vz = Math.max(vz - dz, 0);
        }

        velocity = new Vector(vx, vy, vz);
    }

    /**
     * Get the drag Vector based on velocity and dimensions
     *
     * @return Drag Vector
     */
    private Vector calculateDrag() {
        Vector output = new Vector();
        for (Axis axis : Axis.values()) {
            if (isKinematicOn(axis))
                output = output.set(axis, drag.get(axis) * getFaceArea(axis) * deltaTime * Math.abs(velocity.get(axis)));
        }
        return output;
    }

    /**
     * Apply the effects of momentum to the velocity
     */
    private void applyMomentum() {
        skipMomentum.clear();
        for (Axis axis : Axis.values()) {
            if (!isKinematicOn(axis)) continue;

            float v = velocity.get(axis);

            if (collidesOn(axis)) {
                Side side = Side.getSide(axis, v);
                if (side != null && collidesOn(side)) {
                    for (Physics physics : collidingEntities.get(side)) {

                        if (physics.updatable) {
                            if (physics.isKinematicOn(axis) && physics.isPushableOn(axis)) {

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
            }
            velocity = velocity.set(axis, v);
        }
    }

    /**
     * Update the position of the Entity based on the velocity
     */
    private void updatePosition() {
        for (Axis axis : Axis.values()) {
            if (isKinematicOn(axis))
                setPosition(position.set(axis, position.get(axis) + velocity.multiply(deltaTime).get(axis)));
        }
    }

    /**
     * Check if an Entity has collided with this Entity
     */
    public void tickCollisions() {
        if (!updatable || entities == null) return;
        resetCollisions();
        for (Physics physics : entities) {
            if (physics != this && physics.updatable && super.overlaps(physics)) {
                if (isCollideable()) {
                    collideWith(physics);
                } else {
                    overlapWith(physics);
                }
            }
        }
    }

    /**
     * Reset all collision data
     */
    private void resetCollisions() {
        collidingEntities.values().forEach(HashSet::clear);
        overlappingEntities.clear();
        specialCollisions.clear();
    }

    /**
     * Fix the position of this Entity to make a collision occur
     *
     * @param physics Entity colliding with this Entity
     */
    private void collideWith(Physics physics) {
        float[] overlaps = new float[6];
        overlaps[0] = Math.abs(getMinimum().getX() - physics.getMaximum().getX()); //left overlap
        overlaps[1] = Math.abs(getMaximum().getX() - physics.getMinimum().getX()); //right overlap
        overlaps[2] = Math.abs(getMinimum().getY() - physics.getMaximum().getY()); //bottom overlap
        overlaps[3] = Math.abs(getMaximum().getY() - physics.getMinimum().getY()); //top overlap
        overlaps[4] = Math.abs(getMinimum().getZ() - physics.getMaximum().getZ()); //back overlap
        overlaps[5] = Math.abs(getMaximum().getZ() - physics.getMinimum().getZ()); //front overlap

        Axis axis = Axis.X;
        byte dir = -1;
        byte zeros = 0;
        float distance = overlaps[0];

        for (int i = 0; i < 6; i++) {
            if (overlaps[i] < distance) {
                distance = overlaps[i];
                dir = (byte) -Math.pow(-1, i);
                switch (i) {
                    case 0, 1 -> axis = Axis.X;
                    case 2, 3 -> axis = Axis.Y;
                    case 4, 5 -> axis = Axis.Z;
                }
            }
            if (overlaps[i] == 0) zeros++;
        }
        if (zeros > 1) return;

        Side side = Side.getSide(axis, dir);

        if (collidableSides.contains(side) && physics.collidableSides.contains(Side.getSide(axis, -dir))) {
            if (isKinematicOn(axis) && velocity.get(axis) * dir > 0) {
                if (Math.signum(velocity.get(axis)) == -Math.signum(physics.velocity.get(axis))
                        && !physics.specialCollisions.contains(this)) {
                    distance *= velocity.get(axis) / (velocity.get(axis) - physics.velocity.get(axis));
                    specialCollisions.add(physics);
                }
                setPosition(position.set(axis, position.get(axis) - (distance * dir)));
            }
            collidingEntities.get(side).add(physics);
        } else {
            overlapWith(physics);
        }
    }

    /**
     * Set this Entity as overlapping another
     *
     * @param physics Entity to overlap with
     */
    private void overlapWith(Physics physics) {
        overlappingEntities.add(physics);
    }

    /**
     * Get the position of the Entity
     *
     * @return Position Vector of the Entity
     */
    public Vector getPosition() {
        return position;
    }

    /**
     * Set the position of the Entity
     *
     * @param position Position Vector
     * @return This Physics
     */
    public Physics setPosition(Vector position) {
        this.position = position;
        super.setPosition(position);
        return this;
    }

    /**
     * Get the velocity of the Entity
     *
     * @return Velocity Vector of the Entity
     */
    public Vector getVelocity() {
        return velocity;
    }

    /**
     * Set the velocity of the Entity
     *
     * @param velocity Velocity Vector
     * @return This Physics
     */
    public Physics setVelocity(Vector velocity) {
        this.velocity = velocity;
        return this;
    }

    /**
     * Get the acceleration of the Entity
     *
     * @return Acceleration Vector of the Entity
     */
    public Vector getAcceleration() {
        return acceleration;
    }

    /**
     * Set the acceleration of the Entity
     *
     * @param acceleration Acceleration Vector
     * @return This Physics
     */
    public Physics setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    /**
     * Get the gravity applied to the Entity
     *
     * @return Gravity Vector
     */
    public Vector getGravity() {
        return gravity;
    }

    /**
     * Set the gravity applied to the Entity
     *
     * @param gravity Gravity Vector
     * @return This Physics
     */
    public Physics setGravity(Vector gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * Get the terminal velocity for this Entity
     *
     * @return Terminal velocity Vector
     */
    public Vector getTerminalVelocity() {
        return terminalVelocity;
    }

    /**
     * Set the terminal velocity for this Entity
     *
     * @param terminalVelocity Terminal velocity Vector
     * @return This Physics
     */
    public Physics setTerminalVelocity(Vector terminalVelocity) {
        this.terminalVelocity = terminalVelocity;
        return this;
    }

    /**
     * Get the drag coefficient of the Entity
     *
     * @return Drag coefficient Vector of the Entity
     */
    public Vector getDrag() {
        return drag;
    }

    /**
     * Set the drag coefficient for the Entity
     *
     * @param drag Drag coefficient Vector
     * @return This Physics
     */
    public Physics setDrag(Vector drag) {
        this.drag = drag;
        return this;
    }

    /**
     * Get the roughness of the Entity
     *
     * @return Roughness per axis as a Vector
     */
    public Vector getRoughness() {
        return roughness;
    }

    /**
     * Set the roughness of the Entity
     *
     * @param roughness Roughness per axis as a Vector
     * @return This Physics
     */
    public Physics setRoughness(Vector roughness) {
        this.roughness = roughness;
        return this;
    }

    /**
     * Check if an Entity can be collided with on any side
     *
     * @return True if able to be collided with on any side
     */
    public boolean isCollideable() {
        return !collidableSides.isEmpty();
    }

    /**
     * Check if an Entity can be collided with on a specific Side
     *
     * @return True if able to be collided with on a specific Side
     */
    public boolean isCollideableOn(Side side) {
        return collidableSides.contains(side);
    }

    /**
     * Check if an Entity can be collided with on a specific Axis
     *
     * @return True if able to be collided with on a specific Axis
     */
    public boolean isCollideableOn(Axis axis) {
        return switch (axis) {
            case X -> isCollideableOn(Side.LEFT) || isCollideableOn(Side.RIGHT);
            case Y -> isCollideableOn(Side.BOTTOM) || isCollideableOn(Side.TOP);
            case Z -> isCollideableOn(Side.BACK) || isCollideableOn(Side.FRONT);
        };
    }

    /**
     * Check if an Entity can be collided with on all sides
     *
     * @return True if able to be collided with on all sides
     */
    public boolean isFullyCollideable() {
        return collidableSides.size() == 6;
    }

    /**
     * Get the Sides the Entity can collide on
     *
     * @return HashSet of Sides the Entity can collide on
     */
    public HashSet<Side> getCollideableSides() {
        return collidableSides;
    }

    /**
     * Set the Entity to be collideable on all sides or no sides
     *
     * @param collideable True to allow colliding on all sides, false for no sides
     * @return This Physics
     */
    public Physics setFullyCollideable(boolean collideable) {
        collidableSides.clear();
        if (collideable) collidableSides.addAll(Arrays.asList(Side.values()));
        return this;
    }

    /**
     * Set the Entity to be collideable per Side
     *
     * @param sides Variable amount of Sides
     * @return This Physics
     */
    public Physics setCollidableOn(Side... sides) {
        collidableSides.clear();
        collidableSides.addAll(Arrays.asList(sides));
        return this;
    }

    /**
     * Set the Entity to be collideable per Axis
     *
     * @param axes Variable amount of Axes
     * @return This Physics
     */
    public Physics setCollidableOn(Axis... axes) {
        collidableSides.clear();
        for (Axis axis : axes) {
            collidableSides.add(Side.getSide(axis, 1));
            collidableSides.add(Side.getSide(axis, -1));
        }
        return this;
    }

    /**
     * Set the Entity to be collideable per Side
     *
     * @param sides HashSet of Sides
     * @return This Physics
     */
    public Physics setCollidableSides(HashSet<Side> sides) {
        collidableSides.clear();
        collidableSides.addAll(sides);
        return this;
    }

    /**
     * Check if the Entity is currently colliding with another Entity
     *
     * @return True if the Entity is colliding with another Entity
     */
    public boolean isColliding() {
        for (Side side : Side.values()) {
            if (collidesOn(side)) return true;
        }
        return false;
    }

    /**
     * Check if an Entity collides with this Entity
     *
     * @param physics Entity to check if colliding with
     * @return True if this Entity collides with the other Entity
     */
    public boolean collidesWith(Physics physics) {
        for (HashSet<Physics> list : collidingEntities.values()) {
            if (list.contains(physics)) return true;
        }
        return false;
    }

    /**
     * Check if this Entity is colliding on the specified Side
     *
     * @param side Side of the Entity
     * @return True if the Entity is colliding on the Side
     */
    public boolean collidesOn(Side side) {
        return !collidingEntities.get(side).isEmpty();
    }

    /**
     * Check if this Entity is colliding on the specified Sides perpendicular to the Axis
     *
     * @param axis Axis of collision
     * @return True if the Entity is colliding on the Axis
     */
    public boolean collidesOn(Axis axis) {
        return switch (axis) {
            case X -> collidesOn(Side.LEFT) || collidesOn(Side.RIGHT);
            case Y -> collidesOn(Side.BOTTOM) || collidesOn(Side.TOP);
            case Z -> collidesOn(Side.BACK) || collidesOn(Side.FRONT);
        };
    }

    /**
     * Check if an Entity collides with this one on a specific Side
     *
     * @param physics Entity to check if colliding with
     * @param side    Side of Entity
     * @return True if the Entity is colliding with the other Entity on the specified Side
     */
    public boolean collidesWithOn(Physics physics, Side side) {
        return collidingEntities.get(side).contains(physics);
    }

    /**
     * Check if an Entity collides with this one on a specific Axis
     *
     * @param physics Entity to check if colliding with
     * @param axis    Axis of the grid
     * @return True if the Entity is colliding with the other Entity on the specified Axis
     */
    public boolean collidesWithOn(Physics physics, Axis axis) {
        return switch (axis) {
            case X -> collidesWithOn(physics, Side.LEFT) || collidesWithOn(physics, Side.RIGHT);
            case Y -> collidesWithOn(physics, Side.BOTTOM) || collidesWithOn(physics, Side.TOP);
            case Z -> collidesWithOn(physics, Side.BACK) || collidesWithOn(physics, Side.FRONT);
        };
    }

    /**
     * Get all Entities colliding on the specified Side
     *
     * @param side Side to get colliding Entities of
     * @return Set of all Entities colliding on the Side
     */
    public HashSet<Physics> getCollidingEntities(Side side) {
        return collidingEntities.get(side);
    }

    /**
     * Get all Entities colliding on the specified Axis
     *
     * @param axis Axis to get colliding Entities of
     * @return Set of all Entities colliding on the Axis
     */
    public HashSet<Physics> getCollidingEntities(Axis axis) {
        HashSet<Physics> colliding = new HashSet<>();
        switch (axis) {
            case X -> {
                colliding.addAll(getCollidingEntities(Side.LEFT));
                colliding.addAll(getCollidingEntities(Side.RIGHT));
            }
            case Y -> {
                colliding.addAll(getCollidingEntities(Side.BOTTOM));
                colliding.addAll(getCollidingEntities(Side.TOP));
            }
            case Z -> {
                colliding.addAll(getCollidingEntities(Side.BACK));
                colliding.addAll(getCollidingEntities(Side.FRONT));
            }
        }
        return colliding;
    }

    /**
     * Get a Set of all Entities colliding with this one
     *
     * @return Set of all colliding Entities
     */
    public HashSet<Physics> getCollidingEntities() {
        HashSet<Physics> allEntities = new HashSet<>();
        for (Side side : Side.values()) allEntities.addAll(collidingEntities.get(side));
        return allEntities;
    }

    /**
     * Get all the Sides the Entity is colliding on
     *
     * @return Set of all colliding Sides
     */
    public HashSet<Side> getCollidingSides() {
        HashSet<Side> sides = new HashSet<>();
        for (Side side : Side.values()) if (collidesOn(side)) sides.add(side);
        return sides;
    }

    /**
     * Get all the axes the Entity is colliding on
     *
     * @return Set of all colliding axes
     */
    public HashSet<Axis> getCollidingAxes() {
        HashSet<Axis> axes = new HashSet<>();
        for (Axis axis : Axis.values()) if (collidesOn(axis)) axes.add(axis);
        return axes;
    }

    /**
     * See if this Entity is overlapping any Entity
     *
     * @return True if overlapping
     */
    public boolean isOverlapping() {
        return !overlappingEntities.isEmpty();
    }

    /**
     * Check if this Entity overlaps another
     *
     * @param physics Entity to check with
     * @return True if this Entity overlaps the specified Entity
     */
    public boolean overlaps(Physics physics) {
        return overlappingEntities.contains(physics);
    }

    /**
     * Get the set of all Entities overlapping this one
     *
     * @return Set of overlapping Entities
     */
    public HashSet<Physics> getOverlappingEntities() {
        return overlappingEntities;
    }

    /**
     * Check if the Entity is kinematic on any axis
     *
     * @return True if the Entity is kinematic on any axis
     */
    public boolean isKinematic() {
        return !kinematicAxes.isEmpty();
    }

    /**
     * Check if the Entity is kinematic on every axis
     *
     * @return True if the Entity is kinematic on any axis
     */
    public boolean isFullyKinematic() {
        return kinematicAxes.size() == 3;
    }

    /**
     * Check if the Entity is kinematic on the specified Axis
     *
     * @return True if the Entity is kinematic on the specified Axis
     */
    public boolean isKinematicOn(Axis axis) {
        return kinematicAxes.contains(axis);
    }

    /**
     * Get the Axes the Entity is kinematic on
     *
     * @return HashSet of Axes the Entity is kinematic on
     */
    public HashSet<Axis> getKinematicAxes() {
        return kinematicAxes;
    }

    /**
     * Set whether the Entity can move or not per Axis
     *
     * @param axes Axes to set kinematic
     * @return This Physics
     */
    public Physics setKinematicOn(Axis... axes) {
        kinematicAxes.clear();
        kinematicAxes.addAll(Arrays.asList(axes));
        return this;
    }

    /**
     * Set whether the Entity can move or not for all axes
     *
     * @param kinematic True to allow motion on all axes
     * @return This Physics
     */
    public Physics setFullyKinematic(boolean kinematic) {
        kinematicAxes.clear();
        if (kinematic) kinematicAxes.addAll(Arrays.asList(Axis.values()));
        return this;
    }

    /**
     * Set the kinematic axes for the Entity
     *
     * @param axes HashSet of Axes to set kinematic
     * @return This Physics
     */
    public Physics setKinematicAxes(HashSet<Axis> axes) {
        kinematicAxes.clear();
        kinematicAxes.addAll(axes);
        return this;
    }

    /**
     * Check if the Entity is pushable on any axis
     *
     * @return True if the Entity is pushable on any axis
     */
    public boolean isPushable() {
        return !pushableAxes.isEmpty();
    }

    /**
     * Check if the Entity is pushable on every axis
     *
     * @return True if the Entity is pushable on every axis
     */
    public boolean isFullyPushable() {
        return pushableAxes.size() == 3;
    }

    /**
     * Check if the Entity is pushable on the specified Axis
     *
     * @return True if the Entity is pushable on the specified Axis
     */
    public boolean isPushableOn(Axis axis) {
        return pushableAxes.contains(axis);
    }

    /**
     * Get the Axes the Entity is pushable on
     *
     * @return HashSet of Axes the Entity is pushable on
     */
    public HashSet<Axis> getPushableAxes() {
        return pushableAxes;
    }

    /**
     * Set whether the Entity can be pushed or not per Axis
     *
     * @param axes Axes to set pushable
     * @return This Physics
     */
    public Physics setPushableOn(Axis... axes) {
        pushableAxes.clear();
        pushableAxes.addAll(Arrays.asList(axes));
        return this;
    }

    /**
     * Set whether the Entity can be pushed or not for all axes
     *
     * @param pushable True to allow being pushed on all axes
     * @return This Physics
     */
    public Physics setFullyPushable(boolean pushable) {
        pushableAxes.clear();
        if (pushable) pushableAxes.addAll(Arrays.asList(Axis.values()));
        return this;
    }

    /**
     * Set the pushable axes for the Entity
     *
     * @param axes HashSet of Axes to set pushable
     * @return This Physics
     */
    public Physics setPushableAxes(HashSet<Axis> axes) {
        pushableAxes.clear();
        pushableAxes.addAll(axes);
        return this;
    }

    /**
     * Get the mass of the Entity
     *
     * @return Mass of the Entity
     */
    public float getMass() {
        return mass;
    }

    /**
     * Set the mass of the Entity
     *
     * @param mass New mass for the Entity
     * @return This Physics
     */
    public Physics setMass(float mass) {
        this.mass = mass;
        return this;
    }

    /**
     * Set the Entities this Entity is in a Scene with
     * <p>
     * This is only able to be called by the parent class
     *
     * @param entities List of Physics objects
     */
    @SuppressWarnings("unchecked")
    protected void setEntities(LinkedList<? extends Physics> entities) {
        this.entities = (LinkedList<Physics>) entities;
    }

    /**
     * Set if the Entity can update
     * <p>
     * This is only able to be called by the parent class
     *
     * @param updatable True to allow updating
     */
    protected void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    /**
     * Check if the Entity can update
     * <p>
     * This is only able to be called by the parent class
     *
     * @return True if Entity can update
     */
    protected boolean isUpdatable() {
        return updatable;
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
                Objects.equals(terminalVelocity, physics.terminalVelocity) &&
                Objects.equals(acceleration, physics.acceleration) &&
                Objects.equals(drag, physics.drag) &&
                Objects.equals(roughness, physics.roughness) &&
                Objects.equals(collidingEntities, physics.collidingEntities) &&
                Objects.equals(overlappingEntities, physics.overlappingEntities) &&
                Objects.equals(kinematicAxes, physics.kinematicAxes) &&
                Objects.equals(pushableAxes, physics.pushableAxes) &&
                Objects.equals(collidableSides, physics.collidableSides);
    }
}
