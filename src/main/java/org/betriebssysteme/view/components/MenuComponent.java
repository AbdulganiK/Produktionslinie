package org.betriebssysteme.view.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MenuComponent extends Component {

    private final Texture background;
    private boolean visibility;

    private static final int ROWS = 9;

    private Group menuRoot;
    private Text[] nameLabels = new Text[ROWS];
    private Text[] valueLabels = new Text[ROWS];
    private double baseValueX;
    private double additionalTranslateX;
    private double additionalTranslateY;

    private boolean visible;
    private int baseZIndex;
    private boolean baseZInitialized = false;
    private int zIndexBeforeVisible;


    public void ensureBaseZIndex(int currentZ) {
        if (!baseZInitialized) {
            baseZIndex = currentZ;
            baseZInitialized = true;
        }
    }

    public int getBaseZIndex() {
        return baseZIndex;
    }

    public MenuComponent() {
        this.additionalTranslateX = 0;
        this.additionalTranslateY = 0;
        this.background = FXGL.getAssetLoader().loadTexture("Info_Menu_Machine_Asset.png");
    }

    public MenuComponent(double additionalTranslateX, double additionalTranslateY) {
        this.additionalTranslateX = additionalTranslateX;
        this.additionalTranslateY = additionalTranslateY;
        this.background = FXGL.getAssetLoader().loadTexture("Info_Menu_Machine_Asset.png");
    }

    @Override
    public void onAdded() {

        menuRoot = new Group();

        background.setTranslateX(0);
        background.setTranslateY(0);


        menuRoot.getChildren().add(background);

        double w = background.getWidth();
        double h = 200;

        menuRoot.setTranslateX(-350 + this.additionalTranslateX);
        menuRoot.setTranslateY(-450 + this.additionalTranslateY);

        menuRoot.setScaleX(0.3);
        menuRoot.setScaleY(0.6);

        double startXNames = 200;  // linke Spalte
        double startXValues = w * 0.59;   // rechte Spalte

        baseValueX = startXValues;

        Font font = Font.font("Consolas", 28);

        for (int i = 0; i < ROWS; i++) {

            // individuelle Y-Position pro Zeile
            h = h + 30;

            Text nameText = new Text("");
            nameText.setFill(Color.LIGHTGRAY);
            nameText.setStrokeWidth(2);
            nameText.setFont(font);
            nameText.setTranslateX(startXNames);
            nameText.setTranslateY(h);

            Text valueText = new Text("");
            valueText.setFill(Color.LIMEGREEN);
            valueText.setStrokeWidth(2);
            valueText.setFont(font);
            // HIER: nicht mehr 500, sondern Basis-X benutzen
            valueText.setTranslateX(baseValueX);
            valueText.setTranslateY(h);

            nameLabels[i] = nameText;
            valueLabels[i] = valueText;

            menuRoot.getChildren().addAll(nameText, valueText);
        }

        setVisibility(false);
        entity.getViewComponent().addChild(menuRoot);
    }

    public void setVisibility(boolean visible) {
        this.visibility = visible;

        if (menuRoot != null) {
            menuRoot.setVisible(visible);

            if (visible) {
                int highestZ = entity.getWorld()
                        .getEntities()
                        .stream()
                        .mapToInt(Entity::getZIndex)
                        .max()
                        .orElse(0);

                entity.setZIndex(highestZ + 1);
            }
        }
    }


    public boolean getVisibility() {
        return this.visibility;
    }

    public void setPropertyName(int row, String name) {
        if (row < 0 || row >= ROWS) return;
        nameLabels[row].setText(name);
    }

    public void setPropertyValue(int row, String value) {
        if (row < 0 || row >= ROWS) return;
        valueLabels[row].setText(value);
    }

    public void setProperty(int row, String name, String value) {
        setPropertyName(row, name);
        setPropertyValue(row, value);
    }

    public void setNameLabels(String[] labels) {
        int max = Math.min(labels.length, nameLabels.length);

        for (int i = 0; i < max; i++) {
            nameLabels[i].setText(labels[i]);
        }
    }

    public void setValueLabels(String[] labels) {
        int max = Math.min(labels.length, valueLabels.length);

        for (int i = 0; i < max; i++) {
            valueLabels[i].setText(labels[i]);
        }
    }


    public void setLagerBestand(String value) {
        setProperty(3, "Bestand", value);
    }

    public void setGeschwindigkeit(String value) {
        setProperty(4, "Speed", value);
    }

    public void setKapazitaet(String value) {
        setProperty(5, "Kapazität", value);
    }


}
