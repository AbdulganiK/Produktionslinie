package org.betriebssysteme.view;


import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class ItemMoveComponent extends Component {
    private Point2D direction = Point2D.ZERO;
    private double speed = 80;
    private boolean blocked = false;    // <- NEU


    private int beltContacts = 0;

    public void addBeltContact(Point2D dir) {
        direction = dir;
        beltContacts++;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setDirection(Point2D direction) {
        this.direction = direction;
    }

    public void removeBeltContact() {
        beltContacts = Math.max(0, beltContacts - 1);
        if (beltContacts == 0) {
            direction = Point2D.ZERO;
        }
    }

    public Point2D getDirection() {
        return direction;
    }

    @Override
    public void onUpdate(double tpf) {
        if (!direction.equals(Point2D.ZERO) && !blocked) {
            entity.translate(direction.multiply(speed * tpf));
        }
    }
}
