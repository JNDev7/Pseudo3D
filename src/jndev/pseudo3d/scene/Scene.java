package jndev.pseudo3d.scene;

import jndev.pseudo3d.object.Object;
import jndev.pseudo3d.renderer.Camera;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * scene to place objects on as well as a camera to render them
 */
public class Scene {
    
    /**
     * all objects in the scene
     */
    private ArrayList<Object> objects;
    
    /**
     * camera for the scene to determine where to render from
     */
    private Camera camera;
    
    /**
     * comparator used to sort objects from high to low z position
     */
    private Comparator<Object> zComparator = (o1, o2) -> (int) (o2.getPosition().getZ() - o1.getPosition().getZ());
    
    /**
     * create a new scene
     */
    public Scene() {
        objects = new ArrayList<>();
        camera = new Camera();
    }
    
    /**
     * copy constructor for scene
     *
     * @param scene scene to copy
     */
    public Scene(Scene scene) {
        objects = new ArrayList<>(scene.getObjects());
        camera = scene.getCamera();
    }
    
    /**
     * tick the scene once and every object in it. also sort all objects by z location from high to low
     */
    public void tick() {
        objects.forEach(Object::tick);
        objects.sort(zComparator);
    }
    
    /**
     * get all the objects in this scene
     *
     * @return ArrayList of all objects in this scene
     */
    public ArrayList<Object> getObjects() {
        return objects;
    }
    
    /**
     * add an object to this scene
     *
     * @param object object to add
     */
    public void addObject(Object object) {
        objects.add(object);
        object.setScene(this);
    }
    
    /**
     * remove an object from this scene
     *
     * @param object object to remove
     */
    public void removeObject(Object object) {
        if (objects.contains(object)) {
            objects.remove(object);
            object.setScene(null);
        }
    }
    
    /**
     * set the objects in this scene
     *
     * @param objects ArrayList of objects
     */
    public void setObjects(ArrayList<Object> objects) {
        this.objects = objects;
    }
    
    /**
     * get the camera for this scene
     *
     * @return scene's camera
     */
    public Camera getCamera() {
        return camera;
    }
    
    /**
     * give this scene a different camera
     *
     * @param camera new camera
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
