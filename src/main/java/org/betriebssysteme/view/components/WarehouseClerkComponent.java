package org.betriebssysteme.view.components;

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

    @Override
    public void onAdded() {
        entity.getComponent(AStarMoveComponent.class).moveToCell(8, 17);
    }


}
