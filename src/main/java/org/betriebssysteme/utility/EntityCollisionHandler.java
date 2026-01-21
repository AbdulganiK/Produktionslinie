package org.betriebssysteme.utility;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.betriebssysteme.model.stations.Maschine;
import org.betriebssysteme.view.components.ItemMoveComponent;
import org.betriebssysteme.view.components.MachineComponent;
import org.betriebssysteme.view.components.StationComponent;
import org.betriebssysteme.view.components.SupplierComponent;
import org.betriebssysteme.view.factory.EntityType;

public class EntityCollisionHandler {

    public static void addCollisionBetweenMachineAndEntity(PhysicsWorld physicsWorld) {
        physicsWorld.addCollisionHandler(new com.almasb.fxgl.physics.CollisionHandler(EntityType.MACHINE, EntityType.ITEM) {
            @Override
            protected void onCollision(Entity machine, Entity item) {
                ItemMoveComponent moveComponent = item.getComponent(ItemMoveComponent.class);
                moveComponent.setDirection(Point2D.ZERO);
                if (machine.getComponent(MachineComponent.class).isDoorOpen()) {
                    item.removeFromWorld();
                    Maschine maschineData = (Maschine) machine.getComponent(StationComponent.class).getStation();
                    maschineData.notifyMachineCargoHandoverCompleted();
                }
            }
        });
    }

    public static void addCollisionBetweenSupplierAndStorage(PhysicsWorld physicsWorld) {
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.STORAGE, EntityType.SUPPLIER) {
            @Override
            protected void onCollisionBegin(Entity storage, Entity supplier) {
                System.out.println("KOLLISIONN");
               supplier.setVisible(false);
               supplier.getComponent(SupplierComponent.class).driveAwayFromStorage();
            }

            @Override
            protected void onCollisionEnd(Entity storage, Entity supplier) {
                FXGL.getGameTimer().runOnceAfter(() -> {
                    supplier.setVisible(true);
                }, Duration.seconds(1));
            }
        });

    }

    public static void addCollisionBetweenItems(PhysicsWorld physicsWorld) {
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.ITEM, EntityType.ITEM) {
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
    }

    public static void addCollisionBetweenItemAndBelt(PhysicsWorld physicsWorld) {
        physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.BELT, EntityType.ITEM) {
            @Override
            protected void onCollisionBegin(Entity belt, Entity item) {
                ItemMoveComponent move = item.getComponent(ItemMoveComponent.class);
                move.addBeltContact(belt);
            }

            @Override
            protected void onCollisionEnd(Entity belt, Entity item) {
                item.getComponentOptional(ItemMoveComponent.class)
                        .ifPresent(move -> move.removeBeltContact(belt));
            }
        });
    }

}
