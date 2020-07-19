package jndev.pseudo3d.sprite;

import java.awt.*;

/**
 * sprite made up of a single image
 */
public class ImageSprite implements Sprite {
    
    /**
     * image to represent sprite
     */
    private Image image;
    
    /**
     * create new image sprite
     *
     * @param image image to represent sprite
     */
    public ImageSprite(Image image) {
        this.image = image;
    }
    
    /**
     * get the sprite image
     *
     * @return sprite image
     */
    @Override
    public Image getImage() {
        return image;
    }
}