import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import jeremynoesen.pseudo3d.Pseudo3D;
import jeremynoesen.pseudo3d.input.Keyboard;
import jeremynoesen.pseudo3d.scene.Scene;
import jeremynoesen.pseudo3d.scene.entity.Entity;
import jeremynoesen.pseudo3d.scene.entity.Sprite;
import jeremynoesen.pseudo3d.scene.renderer.Camera;
import jeremynoesen.pseudo3d.scene.util.Vector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

/**
 * sandbox-style testing class for testing various bits of this project
 *
 * @author Jeremy Noesen
 */
public class Sandbox {
    
    /**
     * run the sandbox test scene
     *
     * @param args program arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        Sprite playerFront = new Sprite(new Image(new FileInputStream("src/test/resources/images/player/front.png")));
        Sprite playerBack = new Sprite(new Image(new FileInputStream("src/test/resources/images/player/back.png")));
        Sprite playerLeft = new Sprite(new Image(new FileInputStream("src/test/resources/images/player/left.png")));
        Sprite playerRight = new Sprite(new Image(new FileInputStream("src/test/resources/images/player/right.png")));
        Sprite floor = new Sprite(new Image(new FileInputStream("src/test/resources/images/floor.png")));
        Sprite background = new Sprite(new Image(new FileInputStream("src/test/resources/images/background.png")));
        
        Scene scene = new Scene();
        Entity entity = new Entity();
        entity.setSprite(playerFront);
        entity.setWidth((float) entity.getSprite().getImage().getWidth());
        entity.setHeight((float) entity.getSprite().getImage().getHeight());
        entity.setDepth((float) entity.getSprite().getImage().getWidth());
        entity.setPosition(new Vector(0, 0, -150));
        entity.setMass(1f);
        scene.addEntity(entity);
        
        Entity entity1 = new Entity(entity);
        entity1.setMass(4f);
        scene.addEntity(entity1);
        
        for (int j = 1; j < 18; j++) {
            for (int i = 1; i <= 5; i++) {
                Entity copy = new Entity();
                copy.setSprite(floor);
                copy.setWidth((float) copy.getSprite().getImage().getWidth());
                copy.setHeight((float) copy.getSprite().getImage().getHeight());
                copy.setDepth((float) copy.getSprite().getImage().getWidth());
                copy.setPosition(new Vector(copy.getWidth() * j - 425, -226, -copy.getWidth() * i - 52));
                copy.setKinematic(false);
                scene.addEntity(copy);
            }
        }
        
        Entity backdrop = new Entity();
        backdrop.setWidth(1000);
        backdrop.setHeight(1000);
        backdrop.setKinematic(false);
        backdrop.setPosition(new Vector(0, 0, -300));
        background.setWidth(1000);
        background.setHeight(1000);
        backdrop.setSprite(background);
        scene.addEntity(backdrop);
        
        Camera camera = new Camera();
        camera.setFieldOfView((float) Math.toRadians(40));
        camera.setPosition(new Vector(0, 0, -100));
        camera.setSensorSize(500);
        
        scene.setCamera(camera);
        scene.setBackground(Color.DARKGRAY);
        
        Pseudo3D.init(500, 500, true, "Sandbox");
        Pseudo3D.setActiveScene(scene);
        Pseudo3D.launch();
        
        scene.addRunnable(() -> {
            
            if (Keyboard.isPressed(KeyCode.W) && camera.getFieldOfView() > 0) {
                entity.setVelocity(entity.getVelocity().setZ(-1));
                entity.setSprite(playerBack);
            }
            
            if (Keyboard.isPressed(KeyCode.S) && camera.getFieldOfView() > 0) {
                entity.setVelocity(entity.getVelocity().setZ(1));
                entity.setSprite(playerFront);
            }
            
            if ((Keyboard.isPressed(KeyCode.W) && Keyboard.isPressed(KeyCode.S)) ||
                    camera.getFieldOfView() == 0) {
                entity.setVelocity(entity.getVelocity().setZ(0));
            }
            
            if (Keyboard.isPressed(KeyCode.A)) {
                entity.setVelocity(entity.getVelocity().setX(-1));
                entity.setSprite(playerLeft);
            }
            
            if (Keyboard.isPressed(KeyCode.D)) {
                entity.setVelocity(entity.getVelocity().setX(1));
                entity.setSprite(playerRight);
            }
            
            if (Keyboard.isPressed(KeyCode.A) && Keyboard.isPressed(KeyCode.D)) {
                entity.setVelocity(entity.getVelocity().setX(0));
            }
            
            if (Keyboard.isPressed(KeyCode.SPACE)) {
                entity.setVelocity(entity.getVelocity().setY(1));
            }
            
            if (Keyboard.isPressed(KeyCode.SHIFT)) {
                entity.setVelocity(entity.getVelocity().setY(-1));
            }
            
            if (Keyboard.isPressed(KeyCode.SPACE) && Keyboard.isPressed(KeyCode.SHIFT)) {
                entity.setVelocity(entity.getVelocity().setY(0));
            }
            
            if (Keyboard.isPressed(KeyCode.UP)) {
                camera.setFieldOfView(camera.getFieldOfView() + 0.01f);
            }
            
            if (Keyboard.isPressed(KeyCode.DOWN)) {
                camera.setFieldOfView(Math.max(camera.getFieldOfView() - 0.01f, 0));
            }
            
            if (Keyboard.isPressed(KeyCode.LEFT)) {
                camera.setRotation(camera.getRotation() + 0.01f);
            }
            
            if (Keyboard.isPressed(KeyCode.RIGHT)) {
                camera.setRotation(camera.getRotation() - 0.01f);
            }
            
            if (Keyboard.isPressed(KeyCode.F)) {
                Random random = new Random();
                camera.setOffset(new Vector(random.nextInt() % 4 - 2, random.nextInt() % 4 - 2));
                camera.setRotation(((random.nextInt() % 4) - 2) * 0.001f);
            }
            
            if (Keyboard.isPressed(KeyCode.R)) {
                camera.setOffset(new Vector());
                camera.setRotation(0);
                entity.setPosition(new Vector(0, 0, -150));
                entity1.setPosition(new Vector(0, 0, -150));
                camera.setFieldOfView((float) Math.toRadians(40));
            }
        });
    }
}
