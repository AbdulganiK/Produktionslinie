package org.betriebssysteme.view;


import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
public class ItemMoveComponent extends Component {

    private Point2D direction = Point2D.ZERO;
    private double speed = 40;
    private boolean blocked = false;

    // alle Belts, mit denen das Item gerade kollidiert
    private final java.util.List<Entity> belts = new java.util.ArrayList<>();

    public void addBeltContact(Entity belt) {
        if (!belts.contains(belt)) {
            belts.add(belt);
        }

        // Wenn das der erste Belt ist, übernimmt er die Kontrolle
        if (belts.size() == 1) {
            BeltComponent bc = belt.getComponent(BeltComponent.class);
            direction = bc.getDirection();
        }
    }

    public Point2D getDirection() {
        return direction;
    }

    public void setDirection(Point2D direction) {
        this.direction = direction;
    }

    public void removeBeltContact(Entity belt) {
        belts.remove(belt);

        if (belts.isEmpty()) {
            direction = Point2D.ZERO;
        } else {
            // alter belt
            Entity active = belts.get(0);
            BeltComponent bc = active.getComponent(BeltComponent.class);
            direction = bc.getDirection();
        }
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public void onUpdate(double tpf) {
        if (!direction.equals(Point2D.ZERO) && !blocked) {
            entity.translate(direction.multiply(speed * tpf));
        }
    }
}
