package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.betriebssysteme.model.personnel.Supplier;

import java.util.List;

public class SupplierComponent extends Component {

    private final Supplier supplier;

    private AnimatedTexture texture;
    private AnimationChannel walkFront, walkBack, walkLeft, walkRight;

    private static final int W = 128, H = 128;
    private static final double SPEED = 80;
    private static final double EPS = 2.0;

    private List<Point2D> patrolPath;
    private int patrolIndex = 0;

    private int lastDestinationId = Integer.MIN_VALUE;

    public SupplierComponent(Supplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public void onAdded() {
        walkFront = new AnimationChannel(FXGL.image("Supplier_Front.png"), 4, W, H, Duration.seconds(0.8), 0, 3);
        walkBack  = new AnimationChannel(FXGL.image("Supplier_Back.png"),  4, W, H, Duration.seconds(0.8), 0, 3);
        walkLeft  = new AnimationChannel(FXGL.image("Supplier_Left.png"),  4, W, H, Duration.seconds(0.8), 0, 3);
        walkRight = new AnimationChannel(FXGL.image("Supplier_Right.png"), 4, W, H, Duration.seconds(0.8), 0, 3);

        texture = new AnimatedTexture(walkFront);
        texture.setScaleX(1.2);
        texture.setScaleY(1.2);
        entity.getViewComponent().addChild(texture);
        texture.loopAnimationChannel(walkFront);

        patrolPath = List.of(
                new Point2D(Scenario.LEFT_X,  Scenario.BOTTOM_Y),
                new Point2D(Scenario.RIGHT_X, Scenario.BOTTOM_Y),
                new Point2D(Scenario.RIGHT_X, Scenario.TOP_Y),
                new Point2D(Scenario.LEFT_X,  Scenario.TOP_Y)
        );
    }

    @Override
    public void onUpdate(double tpf) {
        int destId = supplier.getDestinationStationId();

        if (destId != lastDestinationId) {
            lastDestinationId = destId;
            patrolIndex = 0;
        }

        if (destId >= 0 && Scenario.STATION_ARRIVAL_POS.containsKey(destId)) {
            Point2D target = Scenario.STATION_ARRIVAL_POS.get(destId);
            moveTowards(target, tpf);
            return;
        }

        Point2D patrolTarget = patrolPath.get(patrolIndex);

        if (moveTowards(patrolTarget, tpf)) {
            patrolIndex = (patrolIndex + 1) % patrolPath.size();
        }
    }

    private boolean moveTowards(Point2D target, double tpf) {
        Point2D pos = entity.getPosition();
        Point2D diff = target.subtract(pos);
        double dist = diff.magnitude();

        if (dist < EPS) {
            entity.setPosition(target);
            return true;
        }

        Point2D dir = diff.normalize();
        Point2D step = dir.multiply(SPEED * tpf);

        if (step.magnitude() > dist) {
            entity.setPosition(target);
            return true;
        }

        entity.setPosition(pos.add(step));

        if (Math.abs(dir.getX()) > Math.abs(dir.getY())) {
            texture.loopAnimationChannel(dir.getX() > 0 ? walkRight : walkLeft);
        } else {
            texture.loopAnimationChannel(dir.getY() > 0 ? walkFront : walkBack);
        }

        return false;
    }
}
