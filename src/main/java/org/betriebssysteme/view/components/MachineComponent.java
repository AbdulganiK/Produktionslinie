package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
import org.betriebssysteme.model.stations.Maschine;
import org.betriebssysteme.utility.EventHandler;
import org.betriebssysteme.view.animationtyps.MachineAnimationType;
import org.betriebssysteme.view.ProductionLineApp;

public class MachineComponent extends Component {

    private AnimatedTexture texture;

    private AnimationChannel turningOffAnim;
    private AnimationChannel turningOnAnim;

    private AnimationChannel productionWhileTakingItemAnimPart1;
    private AnimationChannel productionWhileTakingItemAnimPart2;

    private AnimationChannel productionWithoutTakingItemAnim;
    private AnimationChannel offAnim;

    private Entity belt;


    private boolean doorOpen = false;

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public MachineComponent() {

        this.offAnim = new AnimationChannel(
                FXGL.image("Turning_Off_Animation.png"),
                7, 64, 64,
                Duration.seconds(2),
                6, 6
        );

        this.turningOffAnim = new AnimationChannel(
                FXGL.image("Turning_Off_Animation.png"),
                7, 64, 64,
                Duration.seconds(2),
                0, 6
        );

        this.turningOnAnim = new AnimationChannel(
                FXGL.image("Turning_On_Animation.png"),
                7, 64, 64,
                Duration.seconds(5),
                0, 6
        );


        double totalSeconds = 2.0;

        this.productionWhileTakingItemAnimPart1 = new AnimationChannel(
                FXGL.image("Production_While_Taking_Items_Animation.png"),
                7, 64, 64,
                Duration.seconds(totalSeconds * 4.0 / 7.0),
                0, 3
        );

        this.productionWhileTakingItemAnimPart2 = new AnimationChannel(
                FXGL.image("Production_While_Taking_Items_Animation.png"),
                7, 64, 64,
                Duration.seconds(totalSeconds * 3.0 / 7.0),
                4, 6
        );

        this.productionWithoutTakingItemAnim = new AnimationChannel(
                FXGL.image("Production_Without_Taking_Items_Animation.png"),
                7, 64, 64,
                Duration.seconds(2),
                0, 6
        );

        this.texture = new AnimatedTexture(offAnim);
    }

    public void setBelt(Entity belt) {
        this.belt = belt;
    }

    @Override
    public void onUpdate(double tpf) {
        Maschine maschine = (Maschine) entity.getComponent(StationComponent.class).getStation();
        StatusComponent statusComponent = entity.getComponent(StatusComponent.class);
        maschine.getStatus();
        switch (maschine.getStatus().getStatusTyp()) {
            case INFO -> statusComponent.running();
            case WARNING -> statusComponent.warning();
            case CRITICAL -> statusComponent.error();
        }
        if (maschine.getCargoHandoverToNextMaschineInProgress()) {
            ProductionLineApp app = (ProductionLineApp) FXGL.getApp();
            if (belt != null) {
                app.spawnItemOnBelt(this.belt);
            }
        }
    }

    public void handleMachineClick(javafx.scene.input.MouseEvent e) {
        EventHandler.handleMenuCLick(e, entity);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(this.texture);
        this.texture.setOnMouseClicked(this::handleMachineClick);

        texture.loopAnimationChannel(this.productionWithoutTakingItemAnim);
    }


    private void loopProductionWhileTakingItems() {
        doorOpen = false;

        texture.setOnCycleFinished(() -> {

            doorOpen = true;

            texture.setOnCycleFinished(() -> {
                doorOpen = false;

                loopProductionWhileTakingItems();
            });

            texture.playAnimationChannel(productionWhileTakingItemAnimPart2);
        });

        texture.playAnimationChannel(productionWhileTakingItemAnimPart1);
    }


    public void setAnimation(MachineAnimationType animationType) {

        switch (animationType) {
            case ON:
                texture.setOnCycleFinished(() -> {

                    texture.setOnCycleFinished(() -> {});

                    loopProductionWhileTakingItems();
                });
                texture.playAnimationChannel(turningOnAnim);
                break;

            case OFF:
                this.texture.playAnimationChannel(turningOffAnim);
                break;

            case PRODUCING_WITHOUT_TAKING_ITEMS:
                this.texture.loopAnimationChannel(this.productionWithoutTakingItemAnim);
                break;

            case PRODUCING_AND_TAKING_ITEMS:
                loopProductionWhileTakingItems();
                break;
        }
    }
}
