package jeremynoesen.pseudo3d.scene.entity;

import jeremynoesen.pseudo3d.scene.Scene;

import java.util.Objects;

/**
 * entity to be placed in a scene, includes physics and a sprite
 *
 * @author Jeremy Noesen
 */
public class Entity extends Physics {
    
    /**
     * image that represents the entity when rendered
     */
    private Sprite sprite;
    
    /**
     * whether the entity is on screen or not
     */
    private boolean onScreen;
    
    /**
     * whether to allow updating the entity when it is not on-screen
     */
    private boolean updateOffScreen;
    
    /**
     * constructs a new default entity
     */
    public Entity() {
        super();
        sprite = null;
        onScreen = false;
        updateOffScreen = true;
    }
    
    /**
     * copy constructor for entities
     *
     * @param entity entity to copy
     */
    public Entity(Entity entity) {
        super(entity);
        if (entity.sprite != null) sprite = new Sprite(entity.sprite);
    }
    
    /**
     * get the sprite assigned to this entity
     *
     * @return image sprite of this entity
     */
    public Sprite getSprite() {
        return sprite;
    }
    
    /**
     * set the sprite for the entity
     *
     * @param sprite new image to set as the sprite
     */
    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }
    
    /**
     * check if the entity is shown on screen. if the entity has no sprite, this will always be false
     *
     * @return true if shown on screen
     */
    public boolean isOnScreen() {
        return onScreen;
    }
    
    /**
     * set if the entity is on screen. manually setting this will do nothing, as this value is automatically updated
     *
     * @param onScreen true to be shown on screen
     */
    public void setOnScreen(boolean onScreen) {
        this.onScreen = onScreen;
    }
    
    /**
     * get whether the entity can update when not visible on screen
     *
     * @return true if can update off screen
     */
    public boolean canUpdateOffScreen() {
        return updateOffScreen;
    }
    
    /**
     * get whether the entity can update when not visible on screen
     *
     * @param updateOffScreen true to allow updating off screen
     */
    public void setUpdateOffScreen(boolean updateOffScreen) {
        this.updateOffScreen = updateOffScreen;
    }
    
    /**
     * set the scene the entity is in
     *
     * @param scene scene to place entity in
     */
    @Override
    public void setScene(Scene scene) {
        super.setScene(scene);
        if (scene != null && !scene.getEntities().contains(this)) scene.addEntity(this);
    }
    
    /**
     * check if this entity is identical to another
     *
     * @param o entity to check
     * @return true if this entity is identical to the other entity
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Entity that = (Entity) o;
        return Objects.equals(sprite, that.sprite) &&
                super.equals(that);
    }
}
