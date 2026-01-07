package org.betriebssysteme.view;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class ProductionLineApp extends GameApplication {

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Produktionslinie");

    }

    public static void main(String[] args) {
        launch(args);
    }
}
