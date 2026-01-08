package org.betriebssysteme.view;

import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.ScrollEvent;

public class Zoom {

    private double zoom = 1.0;          // aktueller Zoom
    private double minZoom = 0.5;
    private double maxZoom = 3.0;

    public void initZoomToMouse() {
        Viewport vp = FXGL.getGameScene().getViewport();

        // Scroll-Events auf der FXGL-Szene abfangen
        FXGL.getPrimaryStage().getScene().addEventFilter(ScrollEvent.SCROLL, e -> {

            double oldZoom = vp.getZoom();
            double newZoom = oldZoom;

            // raus / rein
            if (e.getDeltaY() > 0) {
                newZoom *= 1.1;
            } else if (e.getDeltaY() < 0) {
                newZoom *= 0.9;
            }

            // clampen
            newZoom = clamp(newZoom, minZoom, maxZoom);
            if (newZoom == oldZoom) {
                return;
            }

            // Mausposition im Fenster (Screen-Koords der FXGL-Szene)
            double mouseX = e.getSceneX();
            double mouseY = e.getSceneY();

            // aktuellen Viewport-Offset
            double vpX = vp.getX();
            double vpY = vp.getY();

            // Weltkoordinate unter der Maus VOR dem Zoom
            double worldX = mouseX / oldZoom + vpX;
            double worldY = mouseY / oldZoom + vpY;

            // Zoom setzen
            vp.setZoom(newZoom);

            // neuen Viewport so einstellen, dass worldX/worldY wieder unter der Maus liegt
            double newVpX = worldX - mouseX / newZoom;
            double newVpY = worldY - mouseY / newZoom;

            vp.setX(newVpX);
            vp.setY(newVpY);

            e.consume();
        });
    }


    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public void initResetZoom() {
        FXGL.onKeyDown(javafx.scene.input.KeyCode.R, () -> {

            Viewport vp = FXGL.getGameScene().getViewport();

            // Alles zurücksetzen
            vp.setZoom(1.0);
            vp.setX(0);
            vp.setY(0);

            System.out.println("Zoom reset!");
        });
    }
}
