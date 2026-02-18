# Programmhandbuch – Smart Toy Produktionslinie

## Inhaltsverzeichnis
1. [Programmstart](#programmstart)
2. [Übersicht der GUI](#übersicht-der-gui)
3. [Kamera- und Zoom-Steuerung](#kamera--und-zoom-steuerung)
4. [Interaktion mit Objekten](#interaktion-mit-objekten)
5. [Configurations-Menü](#konfigurations-menue)
6. [Statusanzeigen](#statusanzeigen)
7. [Tipps und Tricks](#tipps-und-tricks)

---

## Programmstart

### Systemanforderungen
- Java Runtime Environment (JRE) Version 11 oder höher
- Unterstützte Betriebssysteme: Windows, macOS, Linux

### Starten der Anwendung
1. Navigieren Sie zum Projektverzeichnis
2. Führen Sie das Programm über Maven aus:
   ```bash
   mvn clean javafx:run
   ```
   oder verwenden Sie die kompilierte JAR-Datei

Bei erfolgreichem Start öffnet sich ein Fenster mit der Titel "Produktionslinie" in einer Auflösung von 1600x900 Pixeln.

---

## Übersicht der GUI

Die grafische Benutzeroberfläche zeigt die gesamte Produktionslinie:

### Hauptelemente
- **Maschinen**: Animierte Produktionseinheiten (Fertigung, Montage, Kontrolle, Verpackung)
- **Förderbänder**: Transportieren Artikel zwischen Maschinen
- **Lager**: Zentrales Lager für Materialien und Produkten
- **Personal**:
  - Lageristen (Warehouse Clerks): Transportieren Materialien
  - Lieferanten (Suppliers): Bringen und holen Waren

### Visuelle Feedback-Elemente
- **Statuslampen**: Zeigen den Betriebszustand der Maschinen
  - Erste Lampe: Zeigt an, ob die Maschine läuft
    - grün: läuft (running)
    - dunkel: gestoppt (stopped)
  - Zweite Lampe: Zeigt den Status (betriebsbereit, geringe Kapazität, Leer, ...)
    - grün: betriebsbereit (operational)
    - gelb: geringe Kapazität (low capacity)
    - rot: leer (empty)
- **Animationen**: 
  - Maschinen zeigen Produktions- und Türöffnungsanimationen
  - Personal zeigt Lauf- und Bewegung's-Animationen

---

## Kamera- und Zoom-Steuerung

### Zoom
**Mausrad**:
- **Scrollen nach oben**: Hineinzoomen (Vergrößern)
- **Scrollen nach unten**: Herauszoomen (Verkleinern)

Der Zoom ist auf einen Bereich zwischen dem definierten Minimum und Maximum begrenzt.
Der Zoom erfolgt zur aktuellen Mausposition, sodass der Bereich unter dem Mauszeiger im Fokus bleibt.

### Kamera-Verschiebung
**Mittlere Maustaste** oder **Rechte Maustaste**:
- Halten und ziehen: Verschiebt die Kamera über die Produktionslinie
- Die Kamera folgt der Mausbewegung intuitiv

**Zoom zurücksetzen**:
- Funktion zum Zurücksetzen auf Standard-Zoomstufe ist implementiert

---

## Interaktion mit Objekten

### Info-Menüs öffnen
Klicken Sie mit der **linken Maustaste** auf folgende Objekte, um detaillierte Informationen anzuzeigen:

#### 1. Maschinen
Zeigt Informationen wie:
- Identifikationsnummer
- Produktionsgut (z.B. Control Case, Control PCB, Antriebseinheit, ...)
- Status (betriebsbereit, geringe Kapazität, Leer, ...)
- Laufende Produktion (ja/nein)
- Maximale Lagerkapazität
- Nächste Maschine in der Produktionskette
- Verarbeitungszeit (in ms)
- Aktueller Lagerbestand mit Materialarten und Mengen

#### 2. Lager (Central Platform/Main Depot)
Zeigt:
- Identifikationsnummer
- aktueller Status (betriebsbereit, geringe Kapazität, Leer, ...)
- Lagerbestand nach Materialtypen

#### 3. Lageristen (Warehouse Clerks)
Zeigt:
- Identifikationsnummer
- Status (z.B. gestoppt, transporting)
- Aktuelle Aufgabe (z.B. arbeitslos, beliefern, abholen)
- Ursprungsort des Materials
- Zielort des Materials
- Transportierte Waren
- Transportmenge
- Zeit zur Bearbeitung der aktuellen Aufgabe (in ms)
- Zeit bis zum nächsten Auftrag (in ms)

#### 4. Lieferanten (Suppliers)
Zeigt:
- Identifikationsnummer
- Status (z.B. gestoppt, transporting)
- Aktuelle Aufgabe (z.B. arbeitslos, beliefern, abholen)
- Intervallzeit der Lieferungen (in ms)
- Zeit zur Belieferung (in ms)
- Transportierte Waren mit Transportmenge

### Menü-Verhalten
- Ein Klick auf ein Objekt öffnet dessen Info-Menü
- Gleichzeitig werden alle anderen offenen Menüs automatisch geschlossen
- Ein erneuter Klick auf dasselbe Objekt schließt das Menü

---

## Configurations-Menü

### Menü öffnen
Drücken Sie **ESC** oder nutzen Sie die entsprechende Menü-Option, um das Configurations-Menü zu öffnen.

### Verfügbare Konfigurationen
Das Menü bietet zwei Hauptkonfigurationen:
1. **Production Config**: Einstellungen für Maschinen, Personal und Produktionsparameter
2. **Recipes Config**: Rezepte für Produktionsschritte und Materialzusammensetzung

### Funktionen im Configurations-Menü

#### Config auswählen
- Dropdown-Menü oben links zur Auswahl der Konfiguration
- Wechsel zwischen Production und Recipes Config
- Aktuelle Quelle wird im Label "Aktiv:"angezeigt

#### Buttons und ihre Funktionen

**Apply**:
- Übernimmt die im Textbereich vorgenommenen Änderungen
- Validiert das JSON-Format
- Speichert die Änderungen in der Benutzerkonfiguration

**Save As...**:
- Speichert die aktuelle Konfiguration als neue JSON-Datei unter einem benutzerdefinierten Namen und Speicherort

**Load File...**:
- Öffnet einen Datei-Dialog zur Auswahl einer externen JSON-Datei
- Lädt die Konfiguration aus der gewählten Datei
- Label zeigt "Geladen: [Dateiname]"

**Restart**:
- Löscht alle Produktionsdaten
- Lädt die Konfiguration neu
- Startet ein neues Spiel
- Schließt das Menü automatisch

**Resume**:
- Schließt das Configurations-Menü
- Setzt die Simulation fort

#### JSON-Konfiguration bearbeiten
Der zentrale Textbereich zeigt die aktuelle Konfiguration im JSON-Format:
- Direktes Bearbeiten möglich
- Syntax-Highlighting wird unterstützt
- Bei ungültigem JSON erscheint eine Fehlermeldung beim Versuch, Änderungen zu übernehmen

### Beispiel: Production Config
```json
{
  "station": {
    "identificationNumber": 21,
    "timeToSleep": 500,
    "maxStorageCapacity": 25,
    "initialQuantityOfProduct": 5,
    "maschinePriority": 1
  },
  "personnel": {
    "identificationNumber": 11,
    "supplyInterval_ms": 1000,
    "supplyTimer_ms": 5000,
    "maxCapacity": 100
  }
}
```

### Beispiel: Recipes Config
```json
{
  "recipes": [
    {
      "productionTime": 9500,
      "components": {
        "CONTROL_CASE": 1,
        "CONTROL_PCB": 1
      }
    }
  ]
}
```

---

## Statusanzeigen

### Maschinen-Status
Jede Maschine zeigt zwei farbige Statuslampen:

**Erste Lampe (Betrieb)**:
- grün: läuft (running)
- dunkel: gestoppt (stopped)

**Zweite Lampe (Status-Typ)**:
- Farbe ändert sich je nach aktuellem Status:
    - grün: betriebsbereit (operational)
    - gelb: geringe Kapazität (low capacity)
    - rot: leer (empty)

### Animations-Status

**Maschinen**:
- *Türen geschlossen*: Standard-Produktionsanimation
- *Türen öffnen/schließen*: Materialaufnahme oder -ausgabe
- *Produktionsanimation*: Maschine verarbeitet Material

**Personal**:
- *Laufen/Fahren*: Animationen in vier Richtungen (vorne, hinten, links, rechts)

---

## Tastenkombinationen & Mausbelegung - Übersicht

| Aktion | Steuerung |
|--------|-----------|
| Zoom hinein | Mausrad nach oben |
| Zoom heraus | Mausrad nach unten |
| Kamera verschieben | Mittlere/Rechte Maustaste + Ziehen |
| Info-Menü öffnen/schließen | Linksklick auf Objekt |
| Configurations-Menü | ESC-Taste |

---

## Fehlerbehebung

### Häufige Probleme

**Problem**: Konfiguration wird nicht gespeichert
- **Lösung**: Prüfen Sie, ob das JSON valide ist. Nutzen Sie einen JSON-Validator.

**Problem**: Simulation läuft nicht mehr
- **Lösung**: Öffnen Sie das Configurations-Menü und klicken Sie "Restart"

**Problem**: Objekte nicht sichtbar
- **Lösung**: Zoomen Sie heraus oder verschieben Sie die Kamera

**Problem**: Info-Menü öffnet nicht
- **Lösung**: Stellen Sie sicher, dass Sie direkt auf das Objekt (nicht daneben) klicken

---

## Weitere Informationen

### Log-Dateien
Das Programm erstellt Log-Dateien im Verzeichnis `logs/`:
- ` logfile_tag_Monat_Jahr_Stunde_Minute_Sekunde.log`: Enthält detaillierte Ereignisse und Fehlermeldungen
- Bei Problemen prüfen Sie diese Datei für Hinweise

### Konfigurationsdateien
- Standard-Konfigurationen für die produktionConfig liegten im Ressourcenverzeichnis: "assets/config/productionConfigs/ProductionConfigDefault.json"
- Standard-Konfigurationen für die recipesConfig liegen im Ressourcenverzeichnis: "assets/config/recipesConfigs/RecipesConfigDefault.json"
- Benutzer-Konfigurationen: Werden im Benutzerverzeichnis gespeichert
- JSON-Format wird für alle Konfigurationen verwendet

---

Dieses Projekt wurde im Rahmen des Moduls 3.8 – Betriebssysteme (BESYST) an der Hochschule Bremen entwickelt.

**Version**: 1.0  
**Stand**: Februar 2026  
**Entwickelt für**: Hochschule Bremen, Fakultät 4
