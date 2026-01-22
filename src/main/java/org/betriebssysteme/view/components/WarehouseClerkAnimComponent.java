package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class WarehouseClerkAnimComponent extends Component {

    private static final int FRAME_W = 64;
    private static final int FRAME_H = 64;

    private static final Duration FRONT_BACK_DURATION = Duration.seconds(0.7); // 6 frames
    private static final Duration SIDE_DURATION       = Duration.seconds(0.8); // 8 frames

    private static final double IDLE_EPS = 0.05;

    private AnimationChannel walkTopAnim, walkBotAnim, walkRightAnim, walkLeftAnim;
    private AnimatedTexture texture;

    private Point2D lastPos;
    private AnimationChannel currentChannel;

    // wir tracken, ob wir gerade aktiv loopen
    private boolean isAnimating = false;

    public WarehouseClerkAnimComponent() {
        walkTopAnim = new AnimationChannel(
                FXGL.image("clerk_back.png"),
                6, FRAME_W, FRAME_H,
                FRONT_BACK_DURATION,
                0, 5
        );

        walkBotAnim = new AnimationChannel(
                FXGL.image("clerk_front.png"),
                6, FRAME_W, FRAME_H,
                FRONT_BACK_DURATION,
                0, 5
        );

        walkRightAnim = new AnimationChannel(
                FXGL.image("clerk_right_side.png"),
                8, FRAME_W, FRAME_H,
                SIDE_DURATION,
                0, 7
        );

        walkLeftAnim = new AnimationChannel(
                FXGL.image("clerk_left_side.png"),
                8, FRAME_W, FRAME_H,
                SIDE_DURATION,
                0, 7
        );

        currentChannel = walkBotAnim;
        texture = new AnimatedTexture(currentChannel);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        lastPos = entity.getPosition();

        texture.stop();
        isAnimating = false;
    }

    @Override
    public void onUpdate(double tpf) {
        Point2D now = entity.getPosition();
        Point2D delta = now.subtract(lastPos);
        lastPos = now;

        double dx = delta.getX();
        double dy = delta.getY();

        if (Math.abs(dx) < IDLE_EPS && Math.abs(dy) < IDLE_EPS) {
            if (isAnimating) {
                texture.stop();
                isAnimating = false;
            }
            return;
        }

        AnimationChannel next;
        if (Math.abs(dx) >= Math.abs(dy)) {
            next = dx >= 0 ? walkRightAnim : walkLeftAnim;
        } else {
            next = dy >= 0 ? walkBotAnim : walkTopAnim;
        }

        if (next != currentChannel || !isAnimating) {
            currentChannel = next;
            texture.loopAnimationChannel(currentChannel);
            isAnimating = true;
        }
    }


    private void setIdleChannel(AnimationChannel ch) {
        currentChannel = ch;
        texture.playAnimationChannel(ch);
        texture.stop();
        isAnimating = false;
    }
}
