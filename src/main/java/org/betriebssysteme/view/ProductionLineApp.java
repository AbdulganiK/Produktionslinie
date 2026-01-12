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

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.MACHINE, EntityType.ITEM) {
            @Override
            protected void onCollision(Entity machine, Entity item) {
                ItemMoveComponent moveComponent = item.getComponent(ItemMoveComponent.class);
                moveComponent.setDirection(Point2D.ZERO);
                if (machine.getComponent(MachineComponent.class).isDoorOpen()) {
                    item.removeFromWorld();
                }
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.ITEM, EntityType.ITEM) {
            @Override
            protected void onCollisionBegin(Entity a, Entity b) {

                ItemMoveComponent moveA = a.getComponent(ItemMoveComponent.class);
                ItemMoveComponent moveB = b.getComponent(ItemMoveComponent.class);

                Point2D dir = moveA.getDirection();
                if (dir.equals(Point2D.ZERO)) {
                    return;
                }

                dir = dir.normalize();

                // Projektion der Positionen auf die Bewegungsrichtung
                double projA = a.getCenter().getX() * dir.getX() + a.getCenter().getY() * dir.getY();
                double projB = b.getCenter().getX() * dir.getX() + b.getCenter().getY() * dir.getY();
                // Block Logik
                if (projA < projB) {
                    // a blocken
                    moveA.setBlocked(true);
                } else {
                    // b blocken
                    moveB.setBlocked(true);
                }
            }

            @Override
            protected void onCollisionEnd(Entity a, Entity b) {
                // keine Berührung mehr
                a.getComponentOptional(ItemMoveComponent.class)
                        .ifPresent(m -> m.setBlocked(false));

                b.getComponentOptional(ItemMoveComponent.class)
                        .ifPresent(m -> m.setBlocked(false));
            }
        });

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BELT, EntityType.ITEM) {
            @Override
            protected void onCollisionBegin(Entity belt, Entity item) {
                BeltComponent beltComp = belt.getComponent(BeltComponent.class);
                ItemMoveComponent move = item.getComponent(ItemMoveComponent.class);
                move.addBeltContact(beltComp.getDirection());
            }

            @Override
            protected void onCollisionEnd(Entity belt, Entity item) {
                if (item.hasComponent(ItemMoveComponent.class)) {
                    ItemMoveComponent move = item.getComponent(ItemMoveComponent.class);
                    move.removeBeltContact();
                }
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
        spawnItemOnBelt(belts.get(1));
        spawnItemOnBelt(belts.get(2));
        spawnItemOnBelt(belts.get(3));
        spawnItemOnBelt(belts.get(4));

        Entity storage = FXGL.spawn(EntityNames.STORAGE, 1200, 1200);
        Entity central = FXGL.spawn(EntityNames.CENTRAL, 1800, 1200);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
