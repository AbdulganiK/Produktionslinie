package org.betriebssysteme.view.menu;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.betriebssysteme.control.JSONConfig;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.almasb.fxgl.dsl.FXGL.*;

public class ConfigGameMenu extends FXGLMenu {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TextArea configArea = new TextArea();
    private final ComboBox<ConfigEntry> configSelector = new ComboBox<>();

    private enum ConfigEntry {
        PRODUCTION("Production Config", JSONConfig.DEFAULT_CONFIG_RESOURCE),
        RECIPES("Recipes Config", JSONConfig.RECIPES_CONFIG_RESOURCE);

        final String label;
        final String resourcePath;

        ConfigEntry(String label, String resourcePath) {
            this.label = label;
            this.resourcePath = resourcePath;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public ConfigGameMenu() {
        super(MenuType.GAME_MENU);

        // 1) dunkler Overlay-Hintergrund (wie FXGL-Standard)
        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.55));
        getContentRoot().getChildren().add(bg);

        // 2) Panel in der Mitte
        BorderPane panel = new BorderPane();
        panel.setMaxWidth(900);
        panel.setMaxHeight(650);
        panel.setPrefSize(900, 650);
        panel.setBackground(new Background(new BackgroundFill(Color.rgb(245, 245, 245, 0.98), new CornerRadii(10), Insets.EMPTY)));
        panel.setPadding(new Insets(12));

        // 3) Top: Label + Dropdown
        Label lbl = new Label("Config auswählen:");
        configSelector.getItems().addAll(ConfigEntry.values());
        configSelector.setValue(ConfigEntry.PRODUCTION);

        HBox top = new HBox(10, lbl, configSelector);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 10, 0));
        panel.setTop(top);

        // 4) Center: TextArea (WICHTIG: Größe setzen!)
        configArea.setWrapText(false);
        configArea.setPrefSize(880, 520);
        panel.setCenter(configArea);

        // 5) Bottom: Buttons
        Button btnApply = new Button("Apply");
        Button btnRestart = new Button("Restart");
        Button btnResume = new Button("Resume");

        btnApply.setOnAction(e -> applyConfig());
        btnRestart.setOnAction(e -> {
            getGameController().startNewGame();
            fireResume(); // Menü schließen
        });
        btnResume.setOnAction(e -> fireResume());

        HBox buttons = new HBox(10, btnApply, btnRestart, btnResume);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        panel.setBottom(buttons);

        // 6) Panel zentrieren
        StackPane root = new StackPane(panel);
        root.setPrefSize(getAppWidth(), getAppHeight());
        StackPane.setAlignment(panel, Pos.CENTER);

        getContentRoot().getChildren().add(root);

        // beim Öffnen direkt laden
        openConfig();

        // optional: beim Wechsel im Dropdown automatisch laden
        configSelector.setOnAction(e -> openConfig());
    }

    private void openConfig() {
        try {
            String resource = configSelector.getValue().resourcePath;
            String text = JSONConfig.loadConfigText(resource);
            configArea.setText(text);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Open failed", ex.getMessage());
        }
    }

    private void applyConfig() {
        try {
            String edited = configArea.getText();
            MAPPER.readTree(edited); // JSON validieren

            String resource = configSelector.getValue().resourcePath;
            Path p = JSONConfig.getUserConfigPath(resource);

            Files.createDirectories(p.getParent());
            Files.writeString(p, edited, StandardCharsets.UTF_8);

            showAlert(Alert.AlertType.INFORMATION, "Saved", "Config gespeichert: " + p);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Ungültiges JSON", ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
