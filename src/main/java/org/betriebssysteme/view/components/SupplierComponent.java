package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
import org.betriebssysteme.model.personnel.Supplier;
import org.betriebssysteme.utility.EventHandler;
import org.betriebssysteme.utility.Utility;

import java.util.Arrays;

public class SupplierComponent extends Component {

    private static final int STORAGE_X = 16;
    private static final int STORAGE_Y = 9;

    private static final int AWAY_X = 16;
    private static final int AWAY_Y = 35;

    private final Supplier supplier;

    private final AnimatedTexture texture;
    private final AnimationChannel driveToStorageAnim;
    private final AnimationChannel driveAwayFromStorageAnim;

    // Backend Ziel
    private int lastBackendDest = Integer.MIN_VALUE;

    private boolean readySentForLeg = true;

    public SupplierComponent(Supplier supplier) {
        this.supplier = supplier;

        this.driveToStorageAnim = new AnimationChannel(
                FXGL.image("Supplier_Back.png"),
                4, 140, 125,
                Duration.seconds(2),
                0, 0
        );

        this.driveAwayFromStorageAnim = new AnimationChannel(
                FXGL.image("Supplier_Front.png"),
                4, 140, 125,
                Duration.seconds(2),
                0, 0
        );

        this.texture = new AnimatedTexture(driveToStorageAnim);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        this.texture.setOnMouseClicked(this::handleSupplierClick);

        AStarMoveComponent<?> move = entity.getComponent(AStarMoveComponent.class);

        // Nur bei echter Ankunft ready melden
        move.atDestinationProperty().addListener((obs, oldV, atDest) -> {
            if (!atDest) return;

            if (!readySentForLeg) {
                readySentForLeg = true;
                supplier.setReady();
            }
        });
    }

    public void handleSupplierClick(javafx.scene.input.MouseEvent e) {
        EventHandler.handleMenuCLick(e, entity);
    }

    @Override
    public void onUpdate(double tpf) {
        Utility.setInfo(entity.getComponent(MenuComponent.class), supplier.getInfoArray());
        int backendDest = supplier.getIdOfDestinationStation();

        // Falls sich Backend-Ziel nicht geändert hat
        if (backendDest == lastBackendDest) {
            return;
        }

        lastBackendDest = backendDest;
        readySentForLeg = false;

        AStarMoveComponent<?> move = entity.getComponent(AStarMoveComponent.class);

        // Bei -1  zurückfahren
        if (backendDest == -1) {
            driveAwayFromStorage();
            move.moveToCell(AWAY_X, AWAY_Y);
            return;
        }

        // zum Lager fahren
        driveToStorage();
        move.moveToCell(STORAGE_X, STORAGE_Y);
    }

    private void driveToStorage() {
        texture.loopAnimationChannel(driveToStorageAnim);
    }

    private void driveAwayFromStorage() {
        texture.loopAnimationChannel(driveAwayFromStorageAnim);
    }

    public Supplier getSupplier() {
        return supplier;
    }
}
