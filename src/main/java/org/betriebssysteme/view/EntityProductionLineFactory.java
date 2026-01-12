package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.awt.*;

import static com.almasb.fxgl.dsl.FXGLForKtKt.texture;

public class EntityProductionLineFactory implements EntityFactory {

    @Spawns(EntityNames.MACHINE)
    public Entity newMachine(SpawnData data) {
        return FXGL.entityBuilder(data)
                .with(new MenuComponent())
                .type(EntityType.MACHINE)
                .with(new MachineComponent())
                .with(new StatusComponent())
                .with(new CollidableComponent(true))
                .bbox(new HitBox(BoundingShape.box(64, 64)))
                .build();
    }

    @Spawns(EntityNames.STORAGE)
    public Entity newStorage(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.STORAGE)
                .with(new MenuComponent(350, 0))
                .with(new StatusComponent())
                .with(new StorageComponent())
                .build();
    }

    @Spawns(EntityNames.BELT)
    public Entity newBelt(SpawnData data) {
        HitBox beltHitBox = new HitBox(
                "BELT",
                new Point2D(0, 0),
                BoundingShape.polygon(
                        new Point2D(27, 38),   // unten links
                        new Point2D(60, 24),   // unten rechts
                        new Point2D(60, 16),   // oben rechts
                        new Point2D(12, 26)    // oben links


                )
        );
        return FXGL.entityBuilder(data)
                .type(EntityType.BELT)
                .with(new BeltComponent())
                .with(new CollidableComponent(true))
                .bbox(beltHitBox).build();
    }

    @Spawns(EntityNames.ITEM)
    public Entity newItem(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.ITEM)
                .view("item.png")
                .bbox(new HitBox(BoundingShape.box(16, 16)))
                .with(new CollidableComponent(true))
                .with(new ItemMoveComponent())
                .zIndex(2000)
                .build();
    }

    @Spawns(EntityNames.CENTRAL)
    public Entity newCentral(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(EntityType.CENTRAL)
                .with(new CentralPlatformComponent())
                .with(new StatusComponent(50, -90))
                .with(new MenuComponent(300, -100))
                .build();

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





}
