package org.betriebssysteme.view.components;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import com.almasb.fxgl.entity.component.Component;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.view.factory.WarehouseClerkSkin;

public class WarehouseClerkComponent extends Component {

    private WarehouseClerk warehouseClerk;

    public WarehouseClerkComponent(WarehouseClerk warehouseClerk) {
        this.warehouseClerk = warehouseClerk;
    }


    @Override
    public void onAdded() {
        entity.getComponent(AStarMoveComponent.class).moveToCell(38, 21);
        entity.getComponent(AStarMoveComponent.class).moveToCell(43, 21);
        entity.getComponent(AStarMoveComponent.class).moveToCell(42, 15);
        entity.getComponent(AStarMoveComponent.class).moveToCell(45, 14);
        entity.getComponent(AStarMoveComponent.class).moveToCell(28, 15);
        entity.getComponent(AStarMoveComponent.class).moveToCell(31, 14);
        entity.getComponent(AStarMoveComponent.class).moveToCell(42, 8);
    }

    @Override
    public void onUpdate(double tpf) {
        ArrayList<Entity> entities = entity.getWorld().getEntities();
        int destinationID = warehouseClerk.getDestinationStationId();
        for (Entity entityI : entities) {

        }


    }
}
