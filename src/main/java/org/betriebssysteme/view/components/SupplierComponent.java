package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.betriebssysteme.model.ProductionHeadquarters;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.view.ProductionLineApp;

import java.util.List;

public class SupplierComponent extends Component {


    private AnimatedTexture texture;
    private AnimationChannel driveToStorageAnim, driveAwayFromStorageAnim;
    private Point2D direction = new Point2D(0, -1);
    private int speed = 60;
    private Supplier supplier;

    private int lastDestinationId = Integer.MIN_VALUE;
    private boolean readySentForThisGoal = true;


    public SupplierComponent(Supplier supplier) {
        this.driveToStorageAnim = new AnimationChannel(FXGL.image("Supplier_Back.png"),
                4, 140, 125,
                Duration.seconds(2),
                0, 0);
        this.driveAwayFromStorageAnim = new AnimationChannel(FXGL.image("Supplier_Front.png"),
                4, 140, 125,
                Duration.seconds(2),
                0, 0);

        this.texture = new AnimatedTexture(driveToStorageAnim);
        this.supplier = supplier;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    @Override
    public void onAdded() {
        AStarMoveComponent<?> move = entity.getComponent(AStarMoveComponent.class);

        // Feuert, wenn AStarMoveComponent meint: angekommen (Pfad fertig)
        move.atDestinationProperty().addListener((obs, oldV, atDest) -> {
            if (!atDest) return;

            if (!readySentForThisGoal) {
                readySentForThisGoal = true;
                supplier.setReady();
            }
        });
    }

    @Override
    public void onUpdate(double tpf) {

        ProductionLineApp app = (ProductionLineApp) FXGL.getApp();
        int destinationId = supplier.getIdOfDestinationStation();

        if (destinationId < 0) {
            return;
        }

        if (destinationId == lastDestinationId) {
            return;
        }

        lastDestinationId = destinationId;
        readySentForThisGoal = false;

        AStarMoveComponent<?> move = entity.getComponent(AStarMoveComponent.class);
        if (destinationId == -1) {
            move.moveToCell((int) app.getEmptyPoint().getX()/ 50, (int) app.getEmptyPoint().getY()/50);
        }
        // HQ?
        if (destinationId == ProductionHeadquarters.getInstance().getIdentificationNumber()) {
            List<Entity> hqEntities = entity.getWorld().getEntitiesByComponent(CentralPlatformComponent.class);

            if (hqEntities.isEmpty()) {
                readySentForThisGoal = true;
                supplier.setReady();
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
        supplier.setReady();
    }


    public void driveToStorage() {
        texture.playAnimationChannel(driveToStorageAnim);
        direction = new Point2D(0, -1);
    }

    public void driveAwayFromStorage() {
        texture.playAnimationChannel(driveAwayFromStorageAnim);
        direction = new Point2D(0, 1);
    }


}
