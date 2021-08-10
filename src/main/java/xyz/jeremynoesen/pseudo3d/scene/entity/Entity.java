package xyz.jeremynoesen.pseudo3d.scene.entity;

import xyz.jeremynoesen.pseudo3d.scene.Scene;
import xyz.jeremynoesen.pseudo3d.scene.render.Sprite;

import java.util.Objects;

/**
 * Entity to be placed in a Scene, with Physics and a Sprite
 *
 * @author Jeremy Noesen
 */
public class Entity extends Physics {

    /**
     * Scene the Entity is in
     */
    private Scene scene;

    /**
     * Sprite for the Entity
     */
    private Sprite sprite;

    /**
     * Whether the Entity is on-screen or not
     */
    private boolean onScreen;

    /**
     * Whether to allow updating the Entity when it is not on-screen
     */
    private boolean updateOffScreen;

    /**
     * Speed modifier for physics and rendering
     */
    private float speed;

    /**
     * Whether the Entity is visible and updatable in the Scene or not
     */
    boolean enabled;

    /**
     * Whether the Entity is visible in the Scene or not
     */
    boolean visible;

    /**
     * Whether the Entity has physics or not
     */
    boolean physics;

    /**
     * Create a new default Entity
     */
    public Entity() {
        super();
        scene = null;
        sprite = null;
        onScreen = false;
        updateOffScreen = false;
        speed = 1;
        enabled = true;
        visible = true;
        physics = true;
    }

    /**
     * Copy constructor for Entities
     *
     * @param entity Entity to copy
     */
    public Entity(Entity entity) {
        super(entity);
        if (entity.sprite != null) sprite = new Sprite(entity.sprite);
        this.scene = entity.scene;
        this.onScreen = entity.onScreen;
        this.updateOffScreen = entity.updateOffScreen;
        this.speed = entity.speed;
        this.enabled = entity.enabled;
        this.visible = entity.visible;
        this.physics = entity.physics;
    }

    /**
     * Get the Sprite of the Entity
     *
     * @return Sprite of the Entity
     */
    public Sprite getSprite() {
        return sprite;
    }

    /**
     * Set the Sprite for the Entity
     *
     * @param sprite New Sprite
     * @return This Entity
     */
    public Entity setSprite(Sprite sprite) {
        this.sprite = sprite;
        return this;
    }

    /**
     * Check if the Entity is shown on-screen
     * <p>
     * If the Entity has no Sprite, this will always be false
     *
     * @return True if shown on-screen
     */
    public boolean isOnScreen() {
        return onScreen;
    }

    /**
     * Set if the Entity is on-screen
     * <p>
     * Manually setting this will do nothing, as this value is automatically updated by the Renderer
     *
     * @param onScreen True when visible on-screen
     */
    public void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;
        setUpdatable(enabled && physics && (onScreen || updateOffScreen || !visible));
    }

    /**
     * Get whether the Entity can update when not visible on-screen
     *
     * @return True if the Entity can update off-screen
     */
    public boolean canUpdateOffScreen() {
        return updateOffScreen;
    }

    /**
     * Set whether the Entity can update when not visible on-screen
     *
     * @param updateOffScreen True to allow updating off-screen
     * @return This Entity
     */
    public Entity setUpdateOffScreen(boolean updateOffScreen) {
        this.updateOffScreen = updateOffScreen;
        return this;
    }

    /**
     * Set the Scene the Entity is in
     *
     * @param scene Scene to place Entity in
     * @return This Entity
     */
    public Entity setScene(Scene scene) {
        if (scene != null) {
            super.setEntities(scene.getEntities());
        } else {
            super.setEntities(null);
        }
        this.scene = scene;
        return this;
    }

    /**
     * Get the Scene the Entity is in
     *
     * @return Scene Entity is in
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Get the speed modifier of the Entity
     *
     * @return Speed modifier value
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the speed modifier for the Entity
     *
     * @param speed New speed modifier value
     * @return This Entity
     */
    public Entity setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Update the motion of the Entity
     *
     * @param deltaTime Time elapsed of the previous tick
     */
    @Override
    public void tickMotion(float deltaTime) {
        super.tickMotion(deltaTime * speed);
    }

    /**
     * Check if the Entity is enabled in the Scene
     *
     * @return True if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the Entity to be visible in the Scene and updatable in Physics
     *
     * @param enabled True to enable Entity in Scene
     * @return This Entity
     */
    public Entity setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) setUpdatable(false);
        return this;
    }

    /**
     * Check if the Entity is visible in rendering
     *
     * @return True if visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set the Entity to be visible in rendering
     *
     * @return This Entity
     */
    public Entity setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * Check if the Entity has physics enabled
     *
     * @return True if Entity has physics
     */
    public boolean hasPhysics() {
        return physics;
    }

    /**
     * Set the Entity to have physics or not
     *
     * @param enabled True to enable physics
     * @return This Entity
     */
    public Entity setPhysics(boolean enabled) {
        this.physics = enabled;
        if (!enabled) setUpdatable(false);
        return this;
    }

    /**
     * Check if this Entity is identical to another
     *
     * @param o Entity to check
     * @return True if this Entity is identical to the other Entity
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Entity entity = (Entity) o;
        return onScreen == entity.onScreen &&
                updateOffScreen == entity.updateOffScreen &&
                Float.compare(entity.speed, speed) == 0 &&
                enabled == entity.enabled &&
                visible == entity.visible &&
                physics == entity.physics &&
                Objects.equals(scene, entity.scene) &&
                Objects.equals(sprite, entity.sprite);
    }
}
