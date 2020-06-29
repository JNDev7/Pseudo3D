package jndev.pseudo3d.object.physics;

import jndev.pseudo3d.util.Box;
import jndev.pseudo3d.util.Vector;

/**
 * motion data for an object
 *
 * @author JNDev (Jeremaster101)
 */
public abstract class Motion extends Box {
    
    /**
     * gravity constant used for physics simulation. unit is pixels per tick
     */
    private final double GRAVITY = 0.0981;
    
    /**
     * position of object (pixels)
     */
    private Vector position;
    
    /**
     * velocity of object, or rate of change of position (pixels / tick)
     */
    private Vector velocity;
    
    /**
     * terminal velocity of an object. velocity can not exceed +/- the terminal velocity components
     */
    private Vector terminalVelocity;
    
    /**
     * acceleration of object, or rate of change of velocity (pixels / tick ^ 2)
     */
    private Vector acceleration;
    
    /**
     * jerk of an object, or rate of change of acceleration (pixels / tick ^ 3)
     */
    private Vector jerk;
    
    /**
     * value used to scale gravity constant
     */
    private double gravityScale;
    
    /**
     * initialize all values
     */
    public Motion() {
        super();
        position = new Vector();
        velocity = new Vector();
        terminalVelocity = new Vector(10, 10, 10);
        acceleration = new Vector();
        jerk = new Vector();
        gravityScale = 1.0;
    }
    
    /**
     * copy constructor for motion
     *
     * @param motion motion to copy
     */
    public Motion(Motion motion) {
        super(motion);
        position = motion.position;
        velocity = motion.velocity;
        terminalVelocity = motion.terminalVelocity;
        acceleration = motion.acceleration;
        jerk = motion.jerk;
        gravityScale = motion.gravityScale;
    }
    
    /**
     * calculates the next frame of motion in the x and y axis
     */
    public void tick() {
        updateMotion();
        super.setPosition(position);
    }
    
    /**
     * update the motion of an object in 2D space using jerk, acceleration, velocity, and position
     */
    private void updateMotion() {
        acceleration = new Vector(acceleration.getX() + jerk.getX(),
                acceleration.getY() + jerk.getY(),
                acceleration.getZ() + jerk.getZ());
        //update acceleration based on jerk
        
        double vx = velocity.getX() + acceleration.getX();
        double vy = velocity.getY() + acceleration.getY() - (GRAVITY * gravityScale);
        double vz = velocity.getZ() + acceleration.getZ();
        //update velocity based on acceleration and gravity
        
        if (vx > terminalVelocity.getX())
            vx = terminalVelocity.getX();
        if (vx < -terminalVelocity.getX())
            vx = -terminalVelocity.getX();
        if (vy > terminalVelocity.getY())
            vy = terminalVelocity.getY();
        if (vy < -terminalVelocity.getY())
            vy = -terminalVelocity.getY();
        if (vz > terminalVelocity.getZ())
            vz = terminalVelocity.getZ();
        if (vz < -terminalVelocity.getZ())
            vz = -terminalVelocity.getZ();
        //fix velocities to be within terminal velocity
        
        velocity = new Vector(vx, vy, vz);
        //set new velocity
        
        position = new Vector(position.getX() + velocity.getX(),
                position.getY() + velocity.getY(),
                position.getZ() + velocity.getZ());
        //update position based on velocity
    }
    
    /**
     * get the position vector of the object
     *
     * @return position vector of object
     */
    @Override
    public Vector getPosition() {
        return position;
    }
    
    /**
     * set the position of the object
     *
     * @param position position vector
     */
    @Override
    public void setPosition(Vector position) {
        this.position = position;
        super.setPosition(position);
    }
    
    /**
     * get the velocity vector of the object
     *
     * @return velocity vector of an object
     */
    public Vector getVelocity() {
        return velocity;
    }
    
    /**
     * set the velocity of the object
     *
     * @param velocity velocity vector
     */
    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }
    
    /**
     * get the acceleration vector of the object
     *
     * @return acceleration vector of an object
     */
    public Vector getAcceleration() {
        return acceleration.setY(acceleration.getY() - (GRAVITY * gravityScale));
    }
    
    /**
     * set the acceleration of the object
     *
     * @param acceleration acceleration vector
     */
    public void setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
    }
    
    /**
     * get the jerk vector of the object
     *
     * @return jerk of the object
     */
    public Vector getJerk() {
        return jerk;
    }
    
    /**
     * set the jerk of the object
     *
     * @param jerk jerk vector
     */
    public void setJerk(Vector jerk) {
        this.jerk = jerk;
    }
    
    /**
     * get the scale gravity. default is 1.0
     *
     * @return gravity scale
     */
    public double getGravityScale() {
        return gravityScale;
    }
    
    /**
     * set the gravity scale
     *
     * @param scale gravity scale
     */
    public void setGravityScale(double scale) {
        gravityScale = scale;
    }
    
    /**
     * set terminal velocity for this object
     *
     * @return terminal velocity vector
     */
    public Vector getTerminalVelocity() {
        return terminalVelocity;
    }
    
    /**
     * set the terminal velocity for this object
     *
     * @param terminalVelocity terminal velocity vector
     */
    public void setTerminalVelocity(Vector terminalVelocity) {
        terminalVelocity = terminalVelocity.setX(Math.abs(terminalVelocity.getX()));
        terminalVelocity = terminalVelocity.setY(Math.abs(terminalVelocity.getY()));
        terminalVelocity = terminalVelocity.setZ(Math.abs(terminalVelocity.getZ()));
        this.terminalVelocity = terminalVelocity;
    }
    
    /**
     * check if this motion data is equal to another
     *
     * @param o object to check
     * @return true if the motion datas are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Motion motion = (Motion) o;
        return Double.compare(motion.gravityScale, gravityScale) == 0 &&
                position.equals(motion.position) &&
                velocity.equals(motion.velocity) &&
                acceleration.equals(motion.acceleration) &&
                jerk.equals(motion.jerk);
    }
}
