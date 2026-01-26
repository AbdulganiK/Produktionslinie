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
import javafx.stage.FileChooser;
import org.betriebssysteme.control.JSONConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.almasb.fxgl.dsl.FXGL.*;

public class ConfigGameMenu extends FXGLMenu {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TextArea configArea = new TextArea();
    private final ComboBox<ConfigEntry> configSelector = new ComboBox<>();
    private final Label sourceLabel = new Label();
    private Path externalFile = null;

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

        Rectangle bg = new Rectangle(getAppWidth(), getAppHeight(), Color.color(0, 0, 0, 0.55));
        getContentRoot().getChildren().add(bg);

        BorderPane panel = new BorderPane();
        panel.setMaxWidth(900);
        panel.setMaxHeight(650);
        panel.setPrefSize(900, 650);
        panel.setBackground(new Background(new BackgroundFill(Color.rgb(245, 245, 245, 0.98), new CornerRadii(10), Insets.EMPTY)));
        panel.setPadding(new Insets(12));

        Label lbl = new Label("Config auswählen:");
        configSelector.getItems().addAll(ConfigEntry.values());
        configSelector.setValue(ConfigEntry.PRODUCTION);

        HBox top = new HBox(10, lbl, configSelector, sourceLabel);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(0, 0, 10, 0));
        panel.setTop(top);

        configArea.setWrapText(false);
        configArea.setPrefSize(880, 520);
        panel.setCenter(configArea);

        Button btnApply = new Button("Apply");
        Button btnRestart = new Button("Restart");
        Button btnResume = new Button("Resume");
        Button btnLoadFile = new Button("Load File...");
        Button btnSaveAs = new Button("Save As...");

        btnApply.setOnAction(e -> {
            if (applyConfig()) {
                reloadCurrentOrAll();
            }
        });

        btnRestart.setOnAction(e -> {
            boolean ok = reloadCurrentOrAll();
            if (!ok) return;
            getGameController().startNewGame();
            fireResume();
        });

        btnResume.setOnAction(e -> fireResume());

        btnLoadFile.setOnAction(e -> loadExternalFile());
        btnSaveAs.setOnAction(e -> saveAs());

        HBox buttons = new HBox(10, btnApply, btnSaveAs, btnLoadFile, btnRestart, btnResume);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        panel.setBottom(buttons);

        StackPane root = new StackPane(panel);
        root.setPrefSize(getAppWidth(), getAppHeight());
        StackPane.setAlignment(panel, Pos.CENTER);

        getContentRoot().getChildren().add(root);

        openConfig();

        configSelector.setOnAction(e -> {
            openConfig();
            boolean ok = applyConfigSilent();
            if (ok) {
                if (!reloadCurrentOrAll()) return;
                getGameController().startNewGame();
                fireResume();
            }
        });
    }

    private void openConfig() {
        try {
            ConfigEntry selected = configSelector.getValue();
            if (selected == null) {
                return;
            }
            String resource = selected.resourcePath;
            String text = JSONConfig.loadConfigText(resource);
            configArea.setText(text);
            externalFile = null;
            sourceLabel.setText("Quelle: " + resource);
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Open failed", ex.getMessage());
        }
    }

    private void loadExternalFile() {
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            var window = getContentRoot().getScene().getWindow();
            java.io.File file = fc.showOpenDialog(window);
            if (file == null) return;

            Path path = file.toPath();
            String text = Files.readString(path, StandardCharsets.UTF_8);
            configArea.setText(text);
            externalFile = path;
            sourceLabel.setText("Geladen: " + path.getFileName().toString());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Load failed", ex.getMessage());
        }
    }

    private boolean applyConfig() {
        try {
            String edited = configArea.getText();
            MAPPER.readTree(edited);

            Path target;
            ConfigEntry selected = configSelector.getValue();

            if (externalFile != null) {
                target = externalFile;
                Files.createDirectories(Objects.requireNonNull(target.getParent()));
                Files.writeString(target, edited, StandardCharsets.UTF_8);

                if (selected == null) {
                    throw new IllegalStateException("Bitte oben auswählen, ob das Production oder Recipes ersetzen soll.");
                }
                Path active = JSONConfig.getUserConfigPath(selected.resourcePath);
                Files.createDirectories(Objects.requireNonNull(active.getParent()));
                Files.writeString(active, edited, StandardCharsets.UTF_8);

                sourceLabel.setText("Aktiv: " + active.getFileName() + " (aus " + externalFile.getFileName() + ")");
            } else {
                if (selected == null) throw new IllegalStateException("Keine Config ausgewählt");
                target = JSONConfig.getUserConfigPath(selected.resourcePath);

                Files.createDirectories(Objects.requireNonNull(target.getParent()));
                Files.writeString(target, edited, StandardCharsets.UTF_8);

                sourceLabel.setText("Aktiv: " + target.getFileName().toString());
            }

            return reloadCurrentOrAll();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Ungültiges JSON / Speichern fehlgeschlagen", ex.getMessage());
            return false;
        }
    }

    private boolean applyConfigSilent() {
        try {
            String edited = configArea.getText();
            MAPPER.readTree(edited);

            Path target;
            if (externalFile != null) {
                target = externalFile;
            } else {
                ConfigEntry selected = configSelector.getValue();
                if (selected == null) throw new IllegalStateException("Keine Config ausgewählt");
                target = JSONConfig.getUserConfigPath(selected.resourcePath);
            }

            Files.createDirectories(Objects.requireNonNull(target.getParent()));
            Files.writeString(target, edited, StandardCharsets.UTF_8);

            if (externalFile == null) {
                sourceLabel.setText("Gespeichert: " + target.getFileName().toString());
            } else {
                sourceLabel.setText("Gespeichert: " + target.toAbsolutePath().toString());
            }

            return reloadCurrentOrAll();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Ungültiges JSON / Speichern fehlgeschlagen", ex.getMessage());
            return false;
        }
    }

    private void saveAs() {
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            var window = getContentRoot().getScene().getWindow();
            java.io.File file = fc.showSaveDialog(window);
            if (file == null) return;

            Path path = file.toPath();
            String edited = configArea.getText();
            MAPPER.readTree(edited);

            Files.createDirectories(Objects.requireNonNull(path.getParent()));
            Files.writeString(path, edited, StandardCharsets.UTF_8);

            externalFile = path;
            sourceLabel.setText("Gespeichert als: " + path.toAbsolutePath().toString());
            showAlert(Alert.AlertType.INFORMATION, "Saved", "Config gespeichert: " + path);

            reloadCurrentOrAll();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Save As failed", ex.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Ungültiges JSON", ex.getMessage());
        }
    }

    private boolean reloadCurrentOrAll() {
        try {
            ConfigEntry sel = configSelector.getValue();
            if (sel != null && externalFile == null) {
                JSONConfig.reload(sel.resourcePath);
            } else {
                JSONConfig.reloadAll();
            }
            return true;
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Reload failed", ex.getMessage());
            return false;
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