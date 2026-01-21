package org.betriebssysteme.view.components;

import java.util.ArrayList;
import java.util.List;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import com.almasb.fxgl.entity.component.Component;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.view.factory.WarehouseClerkSkin;

public class WarehouseClerkComponent extends Component {

    private final WarehouseClerk clerk;
    private final WarehouseClerkSkin skin;

    private AnimatedTexture texture;

    private AnimationChannel walkFront, walkBack, walkLeft, walkRight;

    private static final int W = 64, H = 64;
    private static final double SPEED = 140;

    private static final double MACHINE_ZONE_Y = 820;

    private List<Point2D> currentPath = new ArrayList<>();
    private int pathIndex = 0;
    private int currentTargetId = -1;

    public WarehouseClerkComponent(WarehouseClerk clerk) {
        this.clerk = clerk;
        this.skin = WarehouseClerkSkin.random();
        buildAnimations();
        this.texture = new AnimatedTexture(walkFront);
    }

    private void buildAnimations() {
        walkFront = new AnimationChannel(FXGL.image(skin.front), 6, W, H, Duration.seconds(0.8), 0, 6 - 1);
        walkBack = new AnimationChannel(FXGL.image(skin.back), 6, W, H, Duration.seconds(0.8), 0, 6 - 1);
        walkLeft = new AnimationChannel(FXGL.image(skin.left), 8, W, H, Duration.seconds(0.8), 0, 8 - 1);
        walkRight = new AnimationChannel(FXGL.image(skin.right), 8, W, H, Duration.seconds(0.8), 0, 8 - 1);
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        setScale(1.2);
        texture.loopAnimationChannel(walkFront);
    }

    private void setScale(double s) {
        texture.setScaleX(s);
        texture.setScaleY(s);
    }

    public void setDirection(String dir) {
        AnimationChannel ch;
        ch = switch (dir) {
            case "up" -> walkBack;
            case "down" -> walkFront;
            case "left" -> walkLeft;
            case "right" -> walkRight;
            default -> walkFront;
        };
        texture.loopAnimationChannel(ch);
    }

    @Override
    public void onUpdate(double tpf) {
        int dest = clerk.getDestinationStationId();
        int origin = clerk.getOriginStationId();

        if (dest == -1 && origin == -1) return;

        int targetId = (dest != -1) ? dest : origin;

        if (targetId != currentTargetId) {
            buildPathFromCurrentPositionTo(targetId);
        }

        if (pathIndex >= currentPath.size()) return;

        Point2D waypoint = currentPath.get(pathIndex);
        boolean reached = moveTowardsAndReturnReached(waypoint, tpf);
        if (reached) pathIndex++;
    }

    private void addViaCorridorNode(Point2D nextNode, Point2D start) {
        Point2D last = currentPath.isEmpty() ? start : currentPath.get(currentPath.size() - 1);

        double corridorY = nextNode.getY();

        if (Math.abs(last.getY() - corridorY) > 0.5) {
            currentPath.add(new Point2D(last.getX(), corridorY));
        }

        Point2D last2 = currentPath.isEmpty() ? start : currentPath.get(currentPath.size() - 1);
        if (Math.abs(last2.getX() - nextNode.getX()) > 0.5) {
            currentPath.add(new Point2D(nextNode.getX(), corridorY));
        }
    }

    private void buildPathFromCurrentPositionTo(int stationId) {

    }

    private boolean moveTowardsAndReturnReached(Point2D target, double tpf) {
        Point2D pos = entity.getPosition();
        Point2D diff = target.subtract(pos);
        double dist = diff.magnitude();

        if (dist < 2.0) {
            entity.setPosition(target);
            return true;
        }

        Point2D dir = diff.normalize();
        Point2D step = dir.multiply(SPEED * tpf);

        if (step.magnitude() > dist) {
            entity.setPosition(target);
            return true;
        } else {
            entity.setPosition(pos.add(step));
        }

        if (Math.abs(dir.getX()) > Math.abs(dir.getY())) {
            setDirection(dir.getX() > 0 ? "right" : "left");
        } else {
            setDirection(dir.getY() > 0 ? "down" : "up");
        }

        return false;
    }
}
