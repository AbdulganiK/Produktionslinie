package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

public class BeltComponent extends Component {
    AnimatedTexture texture;
    AnimationChannel startBeltAnim, midBeltAnim, endBeltAnim;

    public BeltComponent() {
        this.startBeltAnim = new AnimationChannel(FXGL.image("belt-start.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
        this.midBeltAnim = new AnimationChannel(FXGL.image("belt-mid.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
        this.endBeltAnim = new AnimationChannel(FXGL.image("belt-end.png"), 4, 64, 64, Duration.seconds(0.5), 0, 3);
        this.texture = new AnimatedTexture(this.startBeltAnim);
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
