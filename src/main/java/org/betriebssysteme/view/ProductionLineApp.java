package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.level.tiled.TiledMap;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.input.ScrollEvent;
import com.almasb.fxgl.entity.level.tiled.TiledMap;

import java.util.ArrayList;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getAssetLoader;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.*;

public class ProductionLineApp extends GameApplication {

    Zoom zoom = new Zoom();
    Camera camera = new Camera();



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

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BELT, EntityType.ITEM) {
            @Override
            protected void onCollisionBegin(Entity belt, Entity item) {
                BeltComponent beltComp = belt.getComponent(BeltComponent.class);
                ItemMoveComponent move = item.getComponent(ItemMoveComponent.class);
                move.addBeltContact(beltComp.getDirection());
            }

            @Override
            protected void onCollisionEnd(Entity belt, Entity item) {
                System.out.println(">>> END BELT-ITEM (CollisionHandler)");
                ItemMoveComponent move = item.getComponent(ItemMoveComponent.class);
                move.removeBeltContact();
            }
        });
    }


    public static Entity spawnItemOnBelt(Entity belt) {
        Point2D center = belt.getCenter();

        double itemHalfW = 16;
        double itemHalfH = 16;

        return FXGL.spawn(
                EntityNames.ITEM,
                center.getX() - itemHalfW,
                center.getY() - itemHalfH
        );
    }


    @Override
    protected void initGame() {

        setLevelFromMap("map.tmx");

        // hinzufuegen der Entity Fabrik
        getGameWorld().addEntityFactory(new EntityProductionLineFactory());

        Entity machine = FXGL.spawn(EntityNames.MACHINE, 1000, 1000);
        machine.setZIndex(100);
        machine.getComponent(MachineComponent.class).setAnimation(MachineAnimationType.ON);
        ArrayList<Entity> belts = BeltFactory.spawnBeltsBeforeMachine(machine, 10);
        BeltFactory.spawnBeltsAfterMachine(machine, 10);

        spawnItemOnBelt(belts.getFirst());

        Entity storage = FXGL.spawn(EntityNames.STORAGE, 1200, 1200);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
