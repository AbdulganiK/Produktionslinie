package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.level.tiled.TiledMap;
import com.almasb.fxgl.physics.CollisionHandler;
import javafx.geometry.Point2D;



import javafx.scene.input.ScrollEvent;
import com.almasb.fxgl.entity.level.tiled.TiledMap;
import org.betriebssysteme.utility.EntityCollisionHandler;
import org.betriebssysteme.utility.EntityPlacer;

import java.util.ArrayList;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.*;

public class ProductionLineApp extends GameApplication {

    Zoom zoom = new Zoom();
    Camera camera = new Camera();
    EntityProductionLineFactory entityFactory = new EntityProductionLineFactory();



    @Override
    protected void initInput() {
        this.zoom.initResetZoom();
        this.zoom.initZoomToMouse();
        camera.initCameraDrag();
    }


    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Produktionslinie");
        gameSettings.setDeveloperMenuEnabled(true);
        gameSettings.setWidth(1600);
        gameSettings.setHeight(900);

    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 0);
        // Collision Handling
        EntityCollisionHandler.addCollisionBetweenMachineAndEntity(getPhysicsWorld());
        EntityCollisionHandler.addCollisionBetweenItems(getPhysicsWorld());
        EntityCollisionHandler.addCollisionBetweenItemAndBelt(getPhysicsWorld());
    }



    @Override
    protected void initGame() {

        setLevelFromMap("map.tmx");

        // hinzufuegen der Entity Fabrik
        getGameWorld().addEntityFactory(entityFactory);

        Entity machine = FXGL.spawn(EntityNames.MACHINE, 1000, 1000);
        machine.setZIndex(100);
        machine.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);
        Entity machine2 = FXGL.spawn(EntityNames.MACHINE);
        ArrayList<Entity> beltsBefore = BeltFactory.spawnBeltsBeforeMachine(machine, 10);
        ArrayList<Entity> beltsAfter = BeltFactory.spawnBeltsAfterMachine(machine, 10);
        entityFactory.spawnItemOnBelt(beltsBefore.getFirst());
        entityFactory.spawnItemOnBelt(beltsBefore.get(1));
        entityFactory.spawnItemOnBelt(beltsBefore.get(2));
        entityFactory.spawnItemOnBelt(beltsBefore.get(3));
        entityFactory.spawnItemOnBelt(beltsBefore.get(4));
        EntityPlacer.placeMachineAfterBelt(machine2, beltsAfter.getLast());
        Entity storage = FXGL.spawn(EntityNames.STORAGE, 1200, 1200);
        Entity central = FXGL.spawn(EntityNames.CENTRAL, 1800, 1200);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
