package org.betriebssysteme.view.components;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.personnel.WarehouseClerk;

import java.util.List;

public class WarehouseClerkComponent extends Component {

    private final WarehouseClerk warehouseClerk;

    private int lastDestinationId = Integer.MIN_VALUE;
    private boolean readySentForThisGoal = true;

    public WarehouseClerkComponent(WarehouseClerk warehouseClerk) {
        this.warehouseClerk = warehouseClerk;
    }

    @Override
    public void onAdded() {
        AStarMoveComponent<?> move = entity.getComponent(AStarMoveComponent.class);

        // Feuert, wenn AStarMoveComponent meint: angekommen (Pfad fertig)
        move.atDestinationProperty().addListener((obs, oldV, atDest) -> {
            if (!atDest) return;

            if (!readySentForThisGoal) {
                readySentForThisGoal = true;
                warehouseClerk.setReady(); // <-- genau hier!
            }
        });
    }

    @Override
    public void onUpdate(double tpf) {
        int destinationId = warehouseClerk.getIdOfDestinationStation();

        if (destinationId < 0) {
            return;
        }

        if (destinationId == lastDestinationId) {
            return;
        }

        lastDestinationId = destinationId;
        readySentForThisGoal = false;

        AStarMoveComponent<?> move = entity.getComponent(AStarMoveComponent.class);

        // HQ?
        if (destinationId == ProductionHeadquarters.getInstance().getIdentificationNumber()) {
            List<Entity> hqEntities = entity.getWorld().getEntitiesByComponent(CentralPlatformComponent.class);
            if (hqEntities.isEmpty()) {
                readySentForThisGoal = true;
                warehouseClerk.setReady();
                return;
            }

            Entity hq = hqEntities.getFirst();
            var cell = hq.getComponent(DestinationCellComponent.class).getDestinationCell();

            move.moveToCell((int) cell.getX(), (int) cell.getY());
            return;
        }

        // Sonstige Station suchen
        for (Entity e : entity.getWorld().getEntities()) {
            if (!e.hasComponent(StationComponent.class) || !e.hasComponent(DestinationCellComponent.class))
                continue;

            if (e.getComponent(StationComponent.class).getStation().getIdentificationNumber() == destinationId) {
                var cell = e.getComponent(DestinationCellComponent.class).getDestinationCell();


                move.moveToCell((int) cell.getX(), (int) cell.getY());
                return;
            }
        }


        readySentForThisGoal = true;
        warehouseClerk.setReady();
    }

}
