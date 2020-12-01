package jndev.pseudo3d.loader;

import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * used to load images and save them to speed up loading times
 *
 * @author JNDev (Jeremaster101)
 */
public class ImageLoader {
    
    /**
     * hashmap of all loaded images. images are saved to their path to make retrieving the files easier
     */
    private static final HashMap<String, Image> images = new HashMap<>();
    
    /**
     * this will load all images into a hash map with the file's path as the key
     *
     * @param dir directory where images are stored
     */
    public static void load(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    try {
                        images.put(file.getPath(), new Image(new FileInputStream(file.getPath())));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (file.isDirectory())
                        load(file);
                }
            }
        } else {
            try {
                images.put(dir.getPath(), new Image(new FileInputStream(dir.getPath())));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * get a sprite from the list of loaded images
     *
     * @param name name of image to retrieve
     * @return image object
     */
    public static Image get(String name) {
        return images.get(name);
    }
}
