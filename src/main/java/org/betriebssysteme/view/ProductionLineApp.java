package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;


import javafx.geometry.Rectangle2D;
import org.betriebssysteme.utility.EntityCollisionHandler;
import org.betriebssysteme.view.extras.Camera;
import org.betriebssysteme.view.extras.Zoom;
import org.betriebssysteme.view.factory.EntityNames;
import org.betriebssysteme.view.factory.EntityProductionLineFactory;

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
        EntityCollisionHandler.addCollisionBetweenSupplierAndStorage(getPhysicsWorld());
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

    public Entity spawnItemOnBelt(Entity belt) {
        Point2D center = belt.getCenter();

        double itemHalfW = 16;
        double itemHalfH = 16;

        return FXGL.spawn(
                EntityNames.ITEM,
                center.getX() - itemHalfW,
                center.getY() - itemHalfH
        );
    }




    public static void main(String[] args) {
        launch(args);
    }
}
