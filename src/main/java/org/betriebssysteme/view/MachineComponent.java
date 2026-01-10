package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MachineComponent extends Component {
    private AnimatedTexture texture;
    private AnimationChannel turningOffAnim, turningOnAnim, productionWhileTakingItemAnim, productionWithoutTakingItemAnim, offAnim;

    public MachineComponent() {
        this.offAnim = new AnimationChannel(FXGL.image("Turning_Off_Animation.png"), 7, 64, 64, Duration.seconds(2), 6, 6);
        this.turningOffAnim = new AnimationChannel(FXGL.image("Turning_Off_Animation.png"), 7, 64, 64, Duration.seconds(2), 0, 6);
        this.turningOnAnim = new AnimationChannel(FXGL.image("Turning_On_Animation.png"), 7, 64, 64, Duration.seconds(5), 0, 6);
        this.productionWhileTakingItemAnim = new AnimationChannel(FXGL.image("Production_While_Taking_Items_Animation.png"), 7, 64, 64, Duration.seconds(2), 0, 6);
        this.productionWithoutTakingItemAnim = new AnimationChannel(FXGL.image("Production_Without_Taking_Items_Animation.png"), 7, 64, 64, Duration.seconds(2), 0, 6);
        this.texture = new AnimatedTexture(offAnim);
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

    public void setAnimation(MachineAnimationType animationType) {

        switch (animationType) {
            case ON:
                texture.setOnCycleFinished(() -> {

                    // Callback entfernen, damit er nicht mehrmals aufgerufen wird
                    texture.setOnCycleFinished(() -> {});

                    // 2. Nun in die Loop-Animation wechseln
                    texture.loopAnimationChannel(productionWhileTakingItemAnim);
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
                this.texture.loopAnimationChannel(productionWhileTakingItemAnim);
               break;
        }

    }



}
