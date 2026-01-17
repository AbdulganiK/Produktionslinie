package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import com.almasb.fxgl.entity.component.Component;
import org.betriebssysteme.model.personnel.WarehouseClerk;
import org.betriebssysteme.utility.EventHandler;

public class WarehouseClerkComponent extends Component {

    private static final int FRAMES = 6;
    private static final int FRAME_W = 64;
    private static final int FRAME_H = 64;

    private static final double SCALE = 2.0;

    private final WarehouseClerk clerk;
    private final WarehouseClerkSkin skin;

    private AnimatedTexture texture;
    private AnimationChannel idleAnim;


    public WarehouseClerkComponent(WarehouseClerk clerk){
        this(clerk, WarehouseClerkSkin.random());
    }

    public WarehouseClerkComponent(WarehouseClerk clerk, WarehouseClerkSkin skin){
        this.clerk = clerk;
        this.skin = skin;

        this.idleAnim = new AnimationChannel(
                FXGL.image(skin.file()),
                FRAMES, FRAME_W, FRAME_H,
                Duration.seconds(0.9),
                0, FRAMES -1
        );

        this.texture = new AnimatedTexture(idleAnim);
    }

    @Override
    public void onAdded(){
        entity.getViewComponent().addChild(texture);

        texture.setScaleX(SCALE);
        texture.setScaleY(SCALE);

        texture.setOnMouseClicked(e -> EventHandler.handleMenuCLick(e, entity));

        texture.loopAnimationChannel(idleAnim);
    }

    public WarehouseClerk getClerk() {
        return clerk;
    }
    public WarehouseClerkSkin getSkin(){
        return skin;
    }

}
