package org.betriebssysteme.view;


import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class ItemMoveComponent extends Component {
    private Point2D dir = Point2D.ZERO;
    private double speed = 80; // Pixel pro Sekunde, stell dir passend ein

    public void setDirection(Point2D dir) {
        this.dir = dir;
    }

    public void clearDirection() {
        this.dir = Point2D.ZERO;
    }

    @Override
    public void onUpdate(double tpf) {
        if (!dir.equals(Point2D.ZERO)) {
            entity.translate(dir.multiply(speed * tpf));
        }
    }
}
