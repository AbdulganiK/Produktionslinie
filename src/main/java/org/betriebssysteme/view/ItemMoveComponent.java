package org.betriebssysteme.view;


import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;

public class ItemMoveComponent extends Component {
    private Point2D direction = Point2D.ZERO;
    private double speed = 80;

    private int beltContacts = 0;

    public void addBeltContact(Point2D dir) {
        direction = dir;
        beltContacts++;
    }

    public void removeBeltContact() {
        beltContacts = Math.max(0, beltContacts - 1);
        if (beltContacts == 0) {
            direction = Point2D.ZERO;
        }
    }


    @Override
    public void onUpdate(double tpf) {
        if (!direction.equals(Point2D.ZERO)) {
            entity.translate(direction.multiply(speed * tpf));
        }
    }
}
