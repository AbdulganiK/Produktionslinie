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
import java.util.Scanner;

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
        Scenario scenario = new Scenario(entityFactory);
        scenario.runFirst();


    }


    public static void main(String[] args) {
        launch(args);
    }
}
