package org.betriebssysteme.view.factory;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.model.stations.Station;
import org.betriebssysteme.view.ProductionLineApp;
import org.betriebssysteme.view.components.*;

import static com.almasb.fxgl.dsl.FXGLForKtKt.texture;

public class EntityProductionLineFactory implements EntityFactory {


    @Spawns(EntityNames.MACHINE)
    public Entity newMachine(SpawnData data) {
        Station station = data.get("station");
        return FXGL.entityBuilder(data)
                .with(new StationComponent(station))
                .with(new MenuComponent(30, 0))
                .type(EntityType.MACHINE)
                .with(new MachineComponent())
                .with(new StatusComponent())
                .with(new CollidableComponent(true))
                .bbox(new HitBox(BoundingShape.box(64, 64)))
                .build();
    }

    @Spawns(EntityNames.STORAGE)
    public Entity newStorage(SpawnData data) {
        ProductionLineApp app = (ProductionLineApp) FXGL.getApp();
        Station station = data.get("station");
        HitBox hitBox = new HitBox(new Point2D(10, 20), BoundingShape.box(160, 70));

        return FXGL.entityBuilder(data)
                .type(EntityType.STORAGE)
                .bbox(hitBox)
                .with(new StationComponent(station))
                .with(new CollidableComponent(true))
                .with(new MenuComponent(350, 0))
                .with(new StatusComponent())
                .with(new NotWalkableComponent(app.getGrid(), 6, 0, 0, 0))
                .with(new StorageComponent())
                .build();
    }

    @Spawns(EntityNames.BELT)
    public Entity newBelt(SpawnData data) {
        ProductionLineApp app = (ProductionLineApp) FXGL.getApp();
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
                .anchorFromCenter()
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
        ProductionLineApp app = (ProductionLineApp) FXGL.getApp();
        return FXGL.entityBuilder(data)
                .type(EntityType.CENTRAL)
                .with(new CentralPlatformComponent())
                .with(new StatusComponent(50, -90))
                .with(new NotWalkableComponent(app.getGrid(), 7,0, 0, -2))
                .with(new MenuComponent(300, -100))
                .build();

    }

    @Spawns(EntityNames.WAREHOUSE_CLERK)
    public Entity newWarehouseClerk(SpawnData data) {
        ProductionLineApp app = (ProductionLineApp) FXGL.getApp();

        return FXGL.entityBuilder(data)
                .type(EntityType.WAREHOUSE_CLERK)
                .viewWithBBox(new Rectangle(50, 50, Color.BLUE))
                .with(new CellMoveComponent(50, 50, 150))
                .with(new AStarMoveComponent(app.getGrid()))
                .with(new WarehouseClerkComponent())
                .zIndex(10000)
                .anchorFromCenter()
                .buildAndAttach();
    }

    @Spawns(EntityNames.SUPPLIER)
    public Entity newSupplier(SpawnData data) {
        HitBox hitBox = new HitBox(new Point2D(55, 25), BoundingShape.box(30, 60));
        return FXGL.entityBuilder(data)
                .type(EntityType.SUPPLIER)
                .with(new CollidableComponent(true))
                .with(new SupplierComponent())
                .bbox(hitBox)
                .with(new MenuComponent(0, -120))
                .zIndex(1000)
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
