package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.image.Image;

public class MenuComponent extends Component {
    private Texture texture;
    private boolean visibility;

    public MenuComponent() {
        this.texture = FXGL.getAssetLoader().loadTexture("Info_Menu_Machine_Asset.png");
    }

    @Override
    public void onAdded() {
        this.texture.setTranslateY(-500);
        this.texture.setTranslateX(-400);
        this.texture.setScaleX(0.1);
        this.texture.setScaleY(0.1);
        this.setVisibility(false);
        entity.getViewComponent().addChild(this.texture);
    }

    public void setVisibility(boolean visible) {
        this.visibility = visible;
        this.texture.setVisible(visible);
    }

    public boolean getVisibility() {
        return this.visibility;
    }


}
