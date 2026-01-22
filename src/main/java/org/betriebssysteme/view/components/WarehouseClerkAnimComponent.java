package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.betriebssysteme.utility.EventHandler;

public class WarehouseClerkAnimComponent extends Component {

    private static final int FRAME_W = 64;
    private static final int FRAME_H = 64;

    // Walk (wie bisher)
    private static final Duration FRONT_BACK_DURATION = Duration.seconds(1); // 6 frames
    private static final Duration SIDE_DURATION       = Duration.seconds(1); // 8 frames

    // Idle: 1 Reihe mit 7 Frames (0..6)
    private static final Duration IDLE_DURATION       = Duration.seconds(1);

    private static final double IDLE_EPS = 0.05;

    private enum Dir { FRONT, BACK, LEFT, RIGHT }

    private AnimationChannel walkTopAnim, walkBotAnim, walkRightAnim, walkLeftAnim;
    private AnimationChannel idleTopAnim, idleBotAnim, idleRightAnim, idleLeftAnim;

    private AnimatedTexture texture;
    private Point2D lastPos;

    private AnimationChannel currentChannel;

    private boolean isWalking = false;
    private Dir lastDir = Dir.FRONT;

    public WarehouseClerkAnimComponent() {
        // WALK
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

        // IDLE (7 Frames, 1 Reihe)
        idleTopAnim = new AnimationChannel(
                FXGL.image("clerk_idle_back.png"),
                7, FRAME_W, FRAME_H,
                IDLE_DURATION,
                0, 6
        );

        idleBotAnim = new AnimationChannel(
                FXGL.image("clerk_idle_front.png"),
                7, FRAME_W, FRAME_H,
                IDLE_DURATION,
                0, 6
        );

        idleLeftAnim = new AnimationChannel(
                FXGL.image("clerk_idle_left.png"),
                7, FRAME_W, FRAME_H,
                IDLE_DURATION,
                0, 6
        );

        idleRightAnim = new AnimationChannel(
                FXGL.image("clerk_idle_right.png"),
                7, FRAME_W, FRAME_H,
                IDLE_DURATION,
                0, 6
        );

        // Start: idle front
        currentChannel = idleBotAnim;
        texture = new AnimatedTexture(currentChannel);
    }

    @Override
    public void onAdded() {
        texture.setTranslateX(-20);
        texture.setTranslateY(-40);
        entity.getViewComponent().addChild(texture);

        this.texture.setOnMouseClicked(this::handleClerkClick);

        lastPos = entity.getPosition();

        // initial idle loop
        texture.loopAnimationChannel(currentChannel);
        isWalking = false;
        lastDir = Dir.FRONT;
    }

    public void handleClerkClick(javafx.scene.input.MouseEvent e) {
        System.out.println("CLERK CLICK");
        EventHandler.handleMenuCLick(e, entity);
    }

    @Override
    public void onUpdate(double tpf) {
        Point2D now = entity.getPosition();
        Point2D delta = now.subtract(lastPos);
        lastPos = now;

        double dx = delta.getX();
        double dy = delta.getY();

        boolean idle = Math.abs(dx) < IDLE_EPS && Math.abs(dy) < IDLE_EPS;

        if (idle) {
            // je nach letzter Richtung passende Idle-Animation
            AnimationChannel idleCh = getIdleFor(lastDir);

            if (currentChannel != idleCh || isWalking) {
                currentChannel = idleCh;
                texture.loopAnimationChannel(currentChannel);
                isWalking = false;
            }
            return;
        }

        // Bewegung -> Richtung bestimmen + lastDir updaten
        AnimationChannel walkCh;
        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx >= 0) {
                lastDir = Dir.RIGHT;
                walkCh = walkRightAnim;
            } else {
                lastDir = Dir.LEFT;
                walkCh = walkLeftAnim;
            }
        } else {
            if (dy >= 0) {
                lastDir = Dir.FRONT;
                walkCh = walkBotAnim;
            } else {
                lastDir = Dir.BACK;
                walkCh = walkTopAnim;
            }
        }

        if (currentChannel != walkCh || !isWalking) {
            currentChannel = walkCh;
            texture.loopAnimationChannel(currentChannel);
            isWalking = true;
        }
    }

    private AnimationChannel getIdleFor(Dir dir) {
        return switch (dir) {
            case FRONT -> idleBotAnim;
            case BACK  -> idleTopAnim;
            case LEFT  -> idleLeftAnim;
            case RIGHT -> idleRightAnim;
        };
    }
}
