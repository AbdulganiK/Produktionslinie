package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.betriebssysteme.model.personnel.Supplier;

public class SupplierComponent extends Component {


    private AnimatedTexture texture;
    private AnimationChannel driveToStorageAnim, driveAwayFromStorageAnim;
    private Point2D direction = new Point2D(0, -1);
    private int speed = 60;


    public SupplierComponent() {
        this.driveToStorageAnim = new AnimationChannel(FXGL.image("Supplier_Back.png"),
                4, 140, 125,
                Duration.seconds(2),
                0, 0);
        this.driveAwayFromStorageAnim = new AnimationChannel(FXGL.image("Supplier_Front.png"),
                4, 140, 125,
                Duration.seconds(2),
                0, 0);

        this.texture = new AnimatedTexture(driveToStorageAnim);
    }

    @Override
    public void onUpdate(double tpf) {
        entity.translate(direction.multiply(speed * tpf));
        if (entity.getY() > 1500) {
            this.driveToStorage();
        }
    }

    public void driveToStorage() {
        texture.playAnimationChannel(driveToStorageAnim);
        direction = new Point2D(0, -1);
    }

    public void driveAwayFromStorage() {
        texture.playAnimationChannel(driveAwayFromStorageAnim);
        direction = new Point2D(0, 1);
    }


    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(this.texture);
    }


}
