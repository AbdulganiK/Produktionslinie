package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import org.betriebssysteme.view.animationtyps.BeltAnimationType;
import org.betriebssysteme.view.factory.BeltDirection;

public class BeltComponent extends Component {
    AnimatedTexture texture;
    AnimationChannel startBeltAnim, midBeltAnim, endBeltAnim;
    private BeltDirection beltDirection;

    private Point2D DIR = new Point2D(27, -13).normalize();

    public Point2D getDirection() {
        return DIR;
    }

    public BeltComponent(BeltDirection direction) {
        if (direction == BeltDirection.VERTICAL) {
            this.startBeltAnim = new AnimationChannel(FXGL.image("belt-start-vertical.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.midBeltAnim = new AnimationChannel(FXGL.image("belt-mid-vertical.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.endBeltAnim = new AnimationChannel(FXGL.image("belt-end-vertical.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.texture = new AnimatedTexture(this.startBeltAnim);
        } else {
            this.startBeltAnim = new AnimationChannel(FXGL.image("belt-start-horizontal.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.midBeltAnim = new AnimationChannel(FXGL.image("belt-mid-horizontal.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.endBeltAnim = new AnimationChannel(FXGL.image("belt-end-horizontal.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.texture = new AnimatedTexture(this.startBeltAnim);
        }
    }



    public BeltComponent() {
        this(BeltDirection.VERTICAL);
    }

    public BeltDirection getBeltDirection() {
        return beltDirection;
    }

    public void setBeltDirection(BeltDirection direction) {
        if (direction == BeltDirection.VERTICAL) {
            this.startBeltAnim = new AnimationChannel(FXGL.image("belt-start-vertical.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.midBeltAnim = new AnimationChannel(FXGL.image("belt-mid-vertical.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.endBeltAnim = new AnimationChannel(FXGL.image("belt-end-vertical.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.texture = new AnimatedTexture(this.startBeltAnim);
            this.DIR = new Point2D(27, -13).normalize();
            this.beltDirection = BeltDirection.VERTICAL;
        } else {
            this.startBeltAnim = new AnimationChannel(FXGL.image("belt-start-horizontal.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.midBeltAnim = new AnimationChannel(FXGL.image("belt-mid-horizontal.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.endBeltAnim = new AnimationChannel(FXGL.image("belt-end-horizontal.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
            this.texture = new AnimatedTexture(this.startBeltAnim);
            this.DIR = new Point2D(-27, -13).normalize();

            this.beltDirection = BeltDirection.HORIZONTAL;
        }
        entity.getViewComponent().addChild(this.texture);

    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(this.texture);
    }

    public void setAnimation(BeltAnimationType beltAnimationType){
        switch (beltAnimationType) {
            case START ->  this.texture.loopAnimationChannel(startBeltAnim);
            case MID -> this.texture.loopAnimationChannel(midBeltAnim);
            case END -> this.texture.loopAnimationChannel(endBeltAnim);
        }
    }


}
