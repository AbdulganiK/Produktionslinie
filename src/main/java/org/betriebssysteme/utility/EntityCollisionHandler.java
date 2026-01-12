package org.betriebssysteme.utility;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import javafx.geometry.Point2D;
import org.betriebssysteme.view.BeltComponent;
import org.betriebssysteme.view.EntityType;
import org.betriebssysteme.view.ItemMoveComponent;
import org.betriebssysteme.view.MachineComponent;

public class EntityCollisionHandler {

    public static void addCollisionBetweenMachineAndEntity(PhysicsWorld physicsWorld) {
        physicsWorld.addCollisionHandler(new com.almasb.fxgl.physics.CollisionHandler(EntityType.MACHINE, EntityType.ITEM) {
            @Override
            protected void onCollision(Entity machine, Entity item) {
                ItemMoveComponent moveComponent = item.getComponent(ItemMoveComponent.class);
                moveComponent.setDirection(Point2D.ZERO);
                if (machine.getComponent(MachineComponent.class).isDoorOpen()) {
                    item.removeFromWorld();
                }
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
}
