package org.betriebssysteme.view;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MenuComponent extends Component {

    private final Texture background;
    private boolean visibility;

    private static final int ROWS = 6;

    private Group menuRoot;
    private Text[] nameLabels  = new Text[ROWS];
    private Text[] valueLabels = new Text[ROWS];

    private double[] rowYFactors = new double[] {
            0.25, // Zeile 0
            0.36, // Zeile 1
            0.47, // Zeile 2
            0.58, // Zeile 3
            0.69, // Zeile 4
            0.8  // Zeile 5
    };

    public MenuComponent() {
        this.background = FXGL.getAssetLoader().loadTexture("Info_Menu_Machine_Asset.png");
    }

    @Override
    public void onAdded() {

        // Root für alles im Menü (Background + Text)
        menuRoot = new Group();

        // Background kommt bei (0,0) in diese Gruppe
        background.setTranslateX(0);
        background.setTranslateY(0);
        menuRoot.getChildren().add(background);

        menuRoot.setTranslateX(-400);
        menuRoot.setTranslateY(-500);
        menuRoot.setScaleX(0.1);
        menuRoot.setScaleY(0.1);

        double w = background.getWidth();
        double h = background.getHeight();

        double startXNames  = w * 0.14;  // linke Spalte
        double startXValues = w * 0.7;  // rechte Spalte



        Font font = Font.font("Consolas", 80);

        for (int i = 0; i < ROWS; i++) {

            // individuelle Y-Position pro Zeile
            double rowY = h * rowYFactors[i];

            Text nameText = new Text("Eigenschaft " + (i + 1));
            nameText.setFill(Color.LIGHTGRAY);
            nameText.setStroke(Color.BLACK);
            nameText.setStrokeWidth(2);
            nameText.setFont(font);
            nameText.setTranslateX(startXNames);
            nameText.setTranslateY(rowY);

            Text valueText = new Text("15");
            valueText.setFill(Color.LIMEGREEN);
            valueText.setStroke(Color.BLACK);
            valueText.setStrokeWidth(2);
            valueText.setFont(font);
            valueText.setTranslateX(startXValues);
            valueText.setTranslateY(rowY);

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
        }
    }

    public boolean getVisibility() {
        return this.visibility;
    }

    /** Setzt den Namen (linke Spalte) für eine Zeile 0..5 */
    public void setPropertyName(int row, String name) {
        if (row < 0 || row >= ROWS) return;
        nameLabels[row].setText(name);
    }

    /** Setzt den Wert (rechte Spalte) für eine Zeile 0..5 */
    public void setPropertyValue(int row, String value) {
        if (row < 0 || row >= ROWS) return;
        valueLabels[row].setText(value);
    }

    /** Setzt Name und Wert in einer Zeile */
    public void setProperty(int row, String name, String value) {
        setPropertyName(row, name);
        setPropertyValue(row, value);
    }

    // optional: ein paar bequeme Helper, falls du feste Reihen hast
    public void setID(String value)          { setProperty(0, "ID", value); }
    public void setProduct(String value)      { setProperty(1, "Produkt", value); }
    public void setStatus(String value)     { setProperty(2, "Status", value); }
    public void setLagerBestand(String value)     { setProperty(3, "Lagerbestand", value); }
    public void setGeschwindigkeit(String value)  { setProperty(4, "Geschwindigkeit", value); }
    public void setKapazitaet(String value)    { setProperty(5, "Kapazität", value); }
}
