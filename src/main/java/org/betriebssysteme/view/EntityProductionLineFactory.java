package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
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
                        new Point2D(4, 44),   // unten links
                        new Point2D(60, 24),  // unten rechts
                        new Point2D(60, 16),  // oben rechts
                        new Point2D(4, 36)    // oben links
                )
        );
        return FXGL.entityBuilder(data)
                .type(EntityType.BELT)
                .with(new BeltComponent())
                .bbox(beltHitBox).build();
    }





}
