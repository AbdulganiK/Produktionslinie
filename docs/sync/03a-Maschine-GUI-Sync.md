# Maschine-GUI-Synchronisation

**Dokumentation:** Synchronisationsmodell  
**Fokus:** Polling + Callback Pattern zwischen Maschinen und GUI

---

## Übersicht

Die **Maschine-GUI-Synchronisation** ist anders als WarehouseClerk/Supplier. Sie verwendet **kein Monitor-Pattern (wait/notify)**, sondern ein **Polling + Callback Pattern**.

**Grund:** Maschinen-Threads dürfen nicht blockieren während der Produktion.

---

## Mechanismus

### 1. Flag-basierte Kommunikation (Maschine → GUI)

#### cargoHandoverToNextMaschineInProgress Flag

```java
// In Maschine.java
protected boolean cargoHandoverToNextMaschineInProgress = false;
```

**Setzen des Flags:**
```java
protected void deliverToNextMachine(Cargo cargo) {
    if (nextMaschine != null) {
        boolean cargoNotified = false;
        while (!cargoNotified) {
            boolean remainingCapacity = nextMaschine.getRemainingStorageCapacity(cargo);
            if (remainingCapacity) {
                notifyNextMaschineOfCargoSending(cargo);
                cargoHandoverToNextMaschineInProgress = true;  // ← Flag setzen
                cargoNotified = true;
            }
        }
    }
}
```

**Auslesen des Flags (mit Auto-Reset):**
```java
public boolean getCargoHandoverToNextMaschineInProgress() {
    boolean copy = cargoHandoverToNextMaschineInProgress;
    if (copy) {
        cargoHandoverToNextMaschineInProgress = false;  // ← Auto-Reset
    }
    return copy;
}
```

**Zweck:** 
- Maschine signalisiert: "Ich habe Cargo an Nachfolger gesendet"
- Flag wird beim Auslesen automatisch zurückgesetzt (Single-Use)

---

### 2. Polling (GUI → Maschine)

#### GUI-Component prüft regelmäßig

```java
// In MachineComponent.java
@Override
public void onUpdate(double tpf) {  // Wird jeden Frame aufgerufen
    Maschine maschine = (Maschine) entity.getComponent(StationComponent.class).getStation();
    
    // Polling: Prüfe Flag
    if (maschine.getCargoHandoverToNextMaschineInProgress()) {
        ProductionLineApp app = (ProductionLineApp) FXGL.getApp();
        if (belt != null) {
            app.spawnItemOnBelt(this.belt);  // ← Starte Animation
        }
    }
}
```

**Frequenz:** Jeden Frame (~60x pro Sekunde)

**Zweck:**
- GUI erkennt, wenn Cargo-Übergabe gestartet werden soll
- Startet Item-Animation auf dem Belt

---

### 3. Callback (GUI → Maschine)

#### Nach Animation-Abschluss

```java
// In EntityCollisionHandler.java
public static void addCollisionBetweenMachineAndEntity(PhysicsWorld physicsWorld) {
    physicsWorld.addCollisionHandler(new CollisionHandler(EntityType.MACHINE, EntityType.ITEM) {
        @Override
        protected void onCollision(Entity machine, Entity item) {
            ItemMoveComponent moveComponent = item.getComponent(ItemMoveComponent.class);
            moveComponent.setDirection(Point2D.ZERO);
            
            if (machine.getComponent(MachineComponent.class).isDoorOpen()) {
                item.removeFromWorld();
                
                // Callback: Informiere Maschine
                Maschine maschineData = (Maschine) machine.getComponent(StationComponent.class).getStation();
                maschineData.notifyMachineCargoHandoverCompleted();  // ← Callback
            }
        }
    });
}
```

**Trigger:** Kollision zwischen Item (animiertes Cargo) und Maschine

**Zweck:**
- GUI informiert Maschine: "Animation abgeschlossen, Cargo angekommen"
- Maschine kann Cargo in Storage aufnehmen

---

### 4. Callback-Verarbeitung (Maschine)

```java
// In Maschine.java
public void notifyMachineCargoHandoverCompleted() {
    Cargo cargo;
    try {
        notificationSemaphore.acquire();  // ← Thread-safe
        cargo = cargosOnTransit.poll();   // ← Hole Cargo aus Transit-Queue
        if (cargo == null) {
            logger.warn("No cargo found on transit");
            return;
        }
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        notificationSemaphore.release();
    }
    
    // Cargo in Storage aufnehmen (verwendet storageSemaphore)
    int warningQuantity = resiveCargo(cargo, 1);
}
```

**Aufrufer:** GUI-Thread (JavaFX Application Thread)

**Synchronisation:** 
- `notificationSemaphore` für Transit-Queue
- `storageSemaphore` (in resiveCargo) für Storage

---

## Vollständiger Flow

```
Maschine A (Thread)              GUI (JavaFX Thread)              Maschine B (Thread)
───────────────────              ───────────────────              ───────────────────

produceProduct()
    ↓
deliverToNextMachine()
    ↓
notifyNextMaschineOfCargoSending() ──▶ cargosOnTransit.add(cargo)
    ↓                                  (in Maschine B)
cargoHandoverInProgress = true
    ↓
    
                                 onUpdate() (jeden Frame)
                                     ↓
                                 if (getCargoHandoverInProgress())
                                     ↓ (true & auto-reset)
                                 spawnItemOnBelt()
                                     ↓
                                 [Item-Animation startet]
                                     ↓
                                 [Item bewegt sich zu Maschine B]
                                     ↓
                                 onCollision(machine, item)
                                     ↓
                                 item.removeFromWorld()
                                     ↓
                                 maschineB.notifyMachineCargoHandoverCompleted() ──▶ cargosOnTransit.poll()
                                                                                      ↓
                                                                                  resiveCargo(cargo, 1)
                                                                                      ↓
                                                                                  storage.put(cargo, qty+1)
```

---

## Unterschiede zu WarehouseClerk/Supplier

| Aspekt | WarehouseClerk/Supplier | Maschine |
|--------|-------------------------|----------|
| **Pattern** | Monitor (wait/notify) | Polling + Callback |
| **Blockierung** | Thread blockiert mit wait() | Thread blockiert NICHT |
| **Trigger** | GUI ruft setReady() | GUI pollt Flag |
| **Frequenz** | Event-basiert (1x) | Polling (60 FPS) |
| **Direction** | Bidirektional (GUI ↔ Worker) | Unidirektional (Maschine → GUI) + Callback |
| **Synchronisation** | synchronized | Semaphore |

---

## Warum dieses Pattern?

### ✅ Vorteile

1. **Kein Blockieren:** Maschinen-Thread läuft weiter
2. **Asynchron:** Animation kann parallel laufen
3. **GUI-getrieben:** GUI bestimmt Animation-Timing
4. **Thread-safe:** Callback nutzt Semaphore

### 🟡 Nachteile

1. **Polling-Overhead:** GUI prüft jeden Frame (aber sehr leichtgewichtig)
2. **Komplexer:** Mehr Komponenten beteiligt als wait/notify

### 🎯 Design-Entscheidung

**Warum nicht wait/notify?**
- Maschinen produzieren kontinuierlich
- Blockieren würde Produktion stoppen
- GUI braucht Kontrolle über Animation-Timing

---

## Synchronisations-Details

### Transit-Queue-Schutz

```java
// Vorgänger-Maschine fügt hinzu
notificationSemaphore.acquire();
cargosOnTransit.add(cargo);
notificationSemaphore.release();

// GUI liest aus (nach Animation)
notificationSemaphore.acquire();
cargo = cargosOnTransit.poll();
notificationSemaphore.release();
```

**Verhindert:** Race Condition zwischen addCargoTransitNotification() und notifyMachineCargoHandoverCompleted()

### Storage-Schutz

```java
// In notifyMachineCargoHandoverCompleted()
resiveCargo(cargo, 1);  // ← Nutzt storageSemaphore intern

// In resiveCargo()
storageSemaphore.acquire();
storage.put(cargo, currentQuantity + 1);
storageSemaphore.release();
```

**Verhindert:** Konflikt mit Maschinen-Thread der gleichzeitig storage liest

---

## Code-Beispiele

### Maschine setzt Flag

```java
// Schritt 1: Maschine A prüft Kapazität von B
boolean hasCapacity = nextMaschine.getRemainingStorageCapacity(cargo);

// Schritt 2: Wenn Kapazität vorhanden
if (hasCapacity) {
    // Schritt 3: Benachrichtige B (Transit-Queue)
    nextMaschine.addCargoTransitNotification(cargo);
    
    // Schritt 4: Setze Flag für GUI
    cargoHandoverToNextMaschineInProgress = true;
}
```

### GUI startet Animation

```java
// Polling (jeden Frame)
if (maschine.getCargoHandoverToNextMaschineInProgress()) {
    // Flag war true → jetzt automatisch false
    app.spawnItemOnBelt(belt);
}
```

### GUI ruft Callback

```java
// Bei Kollision
maschineData.notifyMachineCargoHandoverCompleted();
```

---

## Zusammenfassung

### Maschine-GUI-Synchronisation verwendet:

1. **Flag:** `cargoHandoverToNextMaschineInProgress` (boolean)
2. **Polling:** GUI prüft Flag jeden Frame (60 FPS)
3. **Auto-Reset:** Flag wird beim Lesen automatisch zurückgesetzt
4. **Callback:** GUI ruft `notifyMachineCargoHandoverCompleted()` nach Animation
5. **Semaphore:** Schutz für Transit-Queue und Storage

### Pattern-Name:

**"Polling + Callback Pattern"** oder **"Flag-based Event Pattern"**

### Thread-Safety:

✅ **Garantiert durch:**
- Semaphore für cargosOnTransit
- Semaphore für storage
- Atomare boolean-Operation (Auto-Reset)

---

**Nächstes Dokument:** Zurück zu [03-Monitor.md](03-Monitor.md) für Vergleich mit wait/notify

