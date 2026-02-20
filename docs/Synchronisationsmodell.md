# Synchronisationsmodell der Produktionslinie

## Übersicht

Ihr Projekt implementiert eine **Multithread-Produktionslinie** mit komplexen Synchronisationsmechanismen zur Koordinierung paralleler Prozesse. Das System verwendet primär **Semaphore** zur Thread-Synchronisation und vermeidet Deadlocks durch sorgfältiges Ressourcenmanagement.

---

## Architektur der Thread-basierten Komponenten

### 1. Thread-Hierarchie

```
ProductionHeadquarters (Zentrale Verwaltung)
    │
    ├── Maschine (extends Thread)
    │   ├── ProductionMaschine
    │   ├── ControlMachine
    │   └── PackagingMaschine
    │
    ├── WarehouseClerk (extends Thread)
    │
    ├── Supplier (extends Thread)
    │
    └── MainDepot (implements Station, kein eigener Thread)
```

---

## Synchronisationsmechanismen

### 1. **Semaphore-basierte Synchronisation**

Ihr System verwendet **java.util.concurrent.Semaphore** mit 1 Permit (binäre Semaphore) als Mutex-Ersatz.

#### 1.1 ProductionHeadquarters - Request Queue Synchronisation

**Kritische Ressource:** `PriorityQueue<Request> requestQueue`

**Synchronisationsmechanismus:**
```java
private final Semaphore requestQueueSemaphore = new Semaphore(1);
```

**Geschützte Operationen:**
- `addRequest(Request request)` - Hinzufügen von Anfragen
- `pollRequest()` - Abrufen von Anfragen
- `deleteAllData()` - Löschen aller Daten

**Pattern:**
```java
requestQueueSemaphore.acquireUninterruptibly();
try {
    // Kritischer Abschnitt
    requestQueue.add(request);
} finally {
    requestQueueSemaphore.release();
}
```

**Zweck:** Verhindert Race Conditions bei gleichzeitigem Zugriff mehrerer Threads (Maschinen) auf die zentrale Anfrage-Queue.

---

#### 1.2 Maschine - Storage Synchronisation

**Kritische Ressource:** `Map<Cargo, Integer> storage`

**Synchronisationsmechanismus:**
```java
protected Semaphore storageSemaphore = new Semaphore(1);
```

**Geschützte Operationen:**
- `storeProduct(Cargo cargo)` - Produkt einlagern
- `resiveCargo(Cargo cargo, int quantity)` - Cargo empfangen
- `handOverCargo(Cargo cargo, int quantity)` - Cargo übergeben
- `getRemainingStorageCapacity(Cargo cargo)` - Freie Kapazität prüfen

**Pattern:**
```java
storageSemaphore.acquire();
try {
    int currentQuantity = storage.getOrDefault(cargo, 0);
    storage.put(cargo, currentQuantity + 1);
} catch (InterruptedException e) {
    throw new RuntimeException(e);
} finally {
    storageSemaphore.release();
}
```

**Zweck:** Verhindert Datenverlust und Inkonsistenzen beim gleichzeitigen Zugriff von:
- Produktions-Thread (schreibt)
- WarehouseClerk-Threads (lesen/schreiben)
- Nachfolge-Maschinen (lesen)

---

#### 1.3 Maschine - CargoOnTransit Synchronisation

**Kritische Ressource:** `Queue<Cargo> cargosOnTransit`

**Synchronisationsmechanismus:**
```java
Semaphore notificationSemaphore = new Semaphore(1);
```

**Geschützte Operationen:**
- `addCargoTransitNotification(Cargo cargo)` - Cargo-Transit ankündigen
- `notifyMachineCargoHandoverCompleted()` - Übergabe bestätigen
- `getRemainingStorageCapacity(Cargo cargo)` - Kapazität unter Berücksichtigung von Transit-Cargo berechnen

**Zweck:** Koordiniert Cargo-Übergaben zwischen Maschinen und verhindert Überfüllung des Lagers durch Berücksichtigung von "unterwegs" befindlichen Gütern.

---

#### 1.4 MainDepot - Storage Synchronisation

**Kritische Ressource:** `Map<Cargo, Integer> cargoStorage`

**Synchronisationsmechanismus:**
```java
private final Semaphore cargoStorageSemaphore = new Semaphore(1);
```

**Geschützte Operationen:**
- `resiveCargo(Cargo cargo, int quantity)` - Cargo annehmen
- `handOverCargo(Cargo cargo, int quantity)` - Cargo ausgeben

**Zweck:** Zentrale Lagerverwaltung mit Thread-sicheren Zugriffen durch mehrere WarehouseClerk-Threads.

---

#### 1.5 Supplier - Periodischer MainDepot-Zugriff

**Kritische Ressource:** `MainDepot.cargoStorage` (indirekt über MainDepot-Semaphore)

**Synchronisationsmechanismus:**
```java
// Supplier nutzt die MainDepot-Methoden, die intern synchronisiert sind:
public int refillCargo(Cargo cargo, int quantity) {
    MainDepot mainDepot = (MainDepot) ProductionHeadquarters.getInstance()
                                        .getStations().get(mainDepotId);
    int resivedQuantity = mainDepot.resiveCargo(cargo, quantity);
    return resivedQuantity;
}

public int collectCargo(Cargo cargo, int quantity) {
    MainDepot mainDepot = (MainDepot) ProductionHeadquarters.getInstance()
                                        .getStations().get(mainDepotId);
    int receivedQuantity = mainDepot.handOverCargo(cargo, quantity);
    return receivedQuantity;
}
```

**Supply-Routine-Ablauf:**
1. **Initialisierung:** Supplier-eigener Cargo-Storage wird mit Materialien befüllt
2. **Reise zum Depot:** `awaitReady()` wartet auf GUI-Animation
3. **Refill & Collect:** 
   - Alle Materialien (Wood, Metal, Plastic, etc.) an MainDepot liefern
   - Fertige Produkte (PACKAGE) aus MainDepot abholen
   - Schrott (SCRAP) aus MainDepot abholen
4. **Rückreise:** `awaitReady()` wartet auf GUI-Animation
5. **Sleep:** Wartet `supplyInterval_ms` bis zur nächsten Supply-Routine

**Besonderheit:**
- **Keine Requests:** Supplier arbeitet nicht request-basiert wie WarehouseClerk
- **Periodisch:** Timer-gesteuerte Nachschub-Lieferungen
- **Eigener Cargo-Storage:** HashMap ohne Semaphore (nur vom eigenen Thread genutzt)
- **Thread-safe MainDepot-Zugriff:** Nutzt MainDepot's `cargoStorageSemaphore`

**Zweck:** 
- Automatische Nachschub-Lieferungen für das MainDepot
- Abtransport fertiger Produkte und Schrott
- Simulation eines externen Lieferanten

---

#### 1.6 Maschine - GUI-Synchronisation (Polling + Callback)

**Kritische Ressource:** `boolean cargoHandoverToNextMaschineInProgress`

**Synchronisationsmechanismus:**
```java
// KEIN Semaphore für das Flag selbst!
// Aber Semaphore für die zugehörigen Datenstrukturen

// Maschine setzt Flag
protected void deliverToNextMachine(Cargo cargo) {
    if (nextMaschine != null) {
        boolean remainingCapacity = nextMaschine.getRemainingStorageCapacity(cargo);
        if (remainingCapacity) {
            nextMaschine.addCargoTransitNotification(cargo);
            cargoHandoverToNextMaschineInProgress = true;  // ← Flag setzen
        }
    }
}

// GUI pollt Flag (60 FPS)
public boolean getCargoHandoverToNextMaschineInProgress() {
    boolean copy = cargoHandoverToNextMaschineInProgress;
    if (copy) {
        cargoHandoverToNextMaschineInProgress = false;  // ← Auto-Reset
    }
    return copy;
}

// GUI ruft Callback nach Animation
public void notifyMachineCargoHandoverCompleted() {
    Cargo cargo;
    try {
        notificationSemaphore.acquire();
        cargo = cargosOnTransit.poll();
    } finally {
        notificationSemaphore.release();
    }
    resiveCargo(cargo, 1);  // ← Nutzt storageSemaphore
}
```

**Ablauf:**
1. **Maschine A:** Setzt `cargoHandoverInProgress = true`
2. **GUI (Polling):** Prüft Flag jeden Frame (60 FPS)
3. **GUI:** Bei true → startet Item-Animation, Flag wird auto-reset
4. **GUI (Callback):** Nach Animation-Ende → ruft `notifyMachineCargoHandoverCompleted()` auf Maschine B
5. **Maschine B:** Holt Cargo aus Transit-Queue und speichert in Storage

**Unterschied zu WarehouseClerk/Supplier:**

| Aspekt | WarehouseClerk/Supplier | Maschine |
|--------|-------------------------|----------|
| **Pattern** | Monitor (wait/notify) | Polling + Callback |
| **Blockierung** | Thread blockiert | Thread blockiert NICHT |
| **Trigger** | GUI ruft setReady() | GUI pollt Flag |
| **Frequenz** | Event-basiert | Polling (60 FPS) |

**Warum dieses Pattern?**
- **Kein Blockieren:** Maschinen müssen kontinuierlich produzieren
- **Asynchron:** Animation läuft parallel zur Produktion
- **GUI-Kontrolle:** GUI bestimmt Animation-Timing

**Thread-Safety:**
- `notificationSemaphore` schützt `cargosOnTransit`
- `storageSemaphore` schützt `storage`
- Flag-Zugriff ist atomar (boolean read/write)

**Zweck:**
- Synchronisation zwischen Maschinen-Cargo-Übergabe und GUI-Animation
- Nicht-blockierende Kommunikation
- Callback-Benachrichtigung nach Animations-Ende

---

### 2. **Monitor-basierte Synchronisation (synchronized + wait/notify)**

#### 2.1 WarehouseClerk - Animation-Synchronisation

**Verwendung:**
```java
private synchronized void awaitReady() throws InterruptedException {
    ready = false;
    while (!ready) {
        wait();
    }
}

public synchronized void setReady() {
    ready = true;
    notifyAll();
}
```

**Zweck:** 
- Koordination zwischen WarehouseClerk-Thread und GUI-Thread
- WarehouseClerk wartet auf Bestätigung, dass Animation abgeschlossen ist
- GUI ruft `setReady()` auf, um Clerk fortzusetzen

**Pattern:** Producer-Consumer mit Monitor-Pattern

---

#### 2.2 Supplier - Animation-Synchronisation

**Verwendung:**
```java
private synchronized void awaitReady() throws InterruptedException {
    ready = false;
    while (!ready) {
        wait();
    }
}

public synchronized void setReady() {
    ready = true;
    notifyAll();
}
```

**Zweck:**
- Koordination zwischen Supplier-Thread und GUI-Thread
- Supplier wartet auf GUI-Bestätigung bei Bewegung zum/vom MainDepot
- GUI ruft `setReady()` auf, nachdem Bewegungsanimation abgeschlossen ist

**Pattern:** Producer-Consumer mit Monitor-Pattern

**Unterschied zum WarehouseClerk:**
- Supplier arbeitet **periodisch** (Timer-basiert: `supplyInterval_ms`)
- WarehouseClerk arbeitet **request-basiert** (wartet auf Requests aus Queue)

---

## Synchronisationsstrategien

### 1. **Deadlock-Vermeidung**

#### Strategie 1: Keine verschachtelten Locks
- Jeder Thread erwirbt maximal **ein Semaphore gleichzeitig**
- Keine Situationen, in denen Thread A auf B wartet während Thread B auf A wartet

#### Strategie 2: Timeout und Retry bei Ressourcen-Blockierung
```java
while (!cargoNotified) {
    boolean remainingCapacity = nextMaschine.getRemainingStorageCapacity(cargo);
    if (!remainingCapacity) {
        stopMachine();  // Maschine stoppt statt zu blockieren
        Thread.sleep(timeToSleep);  // Wartet und versucht erneut
    } else {
        notifyNextMaschineOfCargoSending(cargo);
        cargoNotified = true;
    }
}
```

**Vorteil:** Verhindert permanente Blockierungen durch aktives Zurückziehen und Wiederholen.

---

### 2. **Starvation-Vermeidung**

**PriorityQueue mit Prioritäten:**
```java
private final PriorityQueue<Request> requestQueue = 
    new PriorityQueue<>(Comparator.comparingInt(Request::priority).reversed());
```

**Mechanismus:**
- Requests haben Prioritäten (`maschinePriority`)
- Höhere Priorität = schnellere Bearbeitung
- Verhindert, dass wichtige Maschinen verhungern

---

### 3. **Resource Pooling**

**WarehouseClerk als Worker-Pool:**
- Mehrere WarehouseClerk-Threads konkurrieren um Requests
- Gemeinsame Request-Queue (thread-safe durch Semaphore)
- Automatische Lastverteilung durch `pollRequest()`

---

## Kommunikationsmuster

### 1. **Producer-Consumer (Maschine ↔ Request Queue)**

**Producer:** Maschinen
```java
protected void sendCargoRequest(Cargo cargo, int quantity) {
    Request request = new Request(quantity, maschinePriority, cargo, identificationNumber);
    ProductionHeadquarters.getInstance().addRequest(request);
}
```

**Consumer:** WarehouseClerk
```java
private boolean getRequested() {
    currentRequest = ProductionHeadquarters.getInstance().pollRequest();
    return currentRequest != null;
}
```

---

### 2. **Pipeline-Pattern (Maschine → Maschine)**

**Kommunikation:**
1. Maschine A prüft Kapazität von Maschine B
2. Maschine A sendet Notification an Maschine B
3. GUI führt Animation durch
4. GUI ruft `notifyMachineCargoHandoverCompleted()` auf Maschine B
5. Maschine B verarbeitet Cargo

**Code:**
```java
// Schritt 1-2: Maschine A
protected void deliverToNextMachine(Cargo cargo) {
    boolean remainingCapacity = nextMaschine.getRemainingStorageCapacity(cargo);
    if (remainingCapacity) {
        notifyNextMaschineOfCargoSending(cargo);
        cargoHandoverToNextMaschineInProgress = true;
    }
}

// Schritt 4-5: Maschine B
public void notifyMachineCargoHandoverCompleted() {
    notificationSemaphore.acquire();
    cargo = cargosOnTransit.poll();
    notificationSemaphore.release();
    resiveCargo(cargo, 1);
}
```

---

## Thread-Lebenszyklus

### 1. **Daemon Threads**

Alle Worker-Threads sind als Daemon markiert:
```java
setDaemon(true);
```

**Vorteil:** 
- Automatisches Herunterfahren beim Beenden der Hauptanwendung
- Keine "hanging threads"

---

### 2. **Endlos-Schleifen mit Sleep**

**Maschine:**
```java
@Override
public void run() {
    while (true) {
        runProductionCycle();
        Thread.sleep(timeToSleep);
    }
}
```

**WarehouseClerk:**
```java
@Override
public void run() {
    while (true) {
        runTaskCycle();
        Thread.sleep(timeForSleep_ms);
    }
}
```

**Supplier:**
```java
@Override
public void run() {
    status = StatusInfo.OPERATIONAL;
    while (true) {
        try {
            supplyRoutine();
            Thread.sleep(supplyInterval_ms);  // Periodischer Nachschub
        } catch (InterruptedException e) {
            status = StatusWarning.STOPPED;
            Thread.currentThread().interrupt();
            break;  // Graceful Shutdown
        }
    }
}
```

**Zweck:** 
- CPU-Schonung und realistische Simulation von Produktionszyklen
- Supplier: Periodische Auffüllung des MainDepot (z.B. alle 30 Sekunden)
- Maschinen: Kontinuierliche Produktion mit konfigurierbaren Zyklen
- WarehouseClerk: Reaktive Verarbeitung von Requests

---

## Kritische Abschnitte - Zusammenfassung

| Komponente | Kritische Ressource | Synchronisation | Zugreifende Threads |
|------------|---------------------|-----------------|---------------------|
| **ProductionHeadquarters** | requestQueue | Semaphore (1) | Alle Maschinen, Alle Clerks |
| **Maschine** | storage | Semaphore (1) | Eigener Thread, WarehouseClerk, GUI |
| **Maschine** | cargosOnTransit | Semaphore (1) | Eigener Thread, Vorgänger-Maschine, GUI |
| **MainDepot** | cargoStorage | Semaphore (1) | Alle WarehouseClerk, Supplier |
| **WarehouseClerk** | ready-Flag | synchronized + wait/notify | Eigener Thread, GUI |
| **Supplier** | ready-Flag | synchronized + wait/notify | Eigener Thread, GUI |

---

## Best Practices implementiert

### ✅ **1. Immutability bei Request**
```java
public record Request(int quantity, int priority, Cargo cargo, int stationId) {}
```
Records sind unveränderlich → keine Synchronisation nötig nach Erstellung.

---

### ✅ **2. Try-Finally bei Semaphore**
```java
semaphore.acquire();
try {
    // Kritischer Abschnitt
} finally {
    semaphore.release();  // Garantiert auch bei Exception
}
```

---

### ✅ **3. Singleton-Pattern mit Lazy Initialization**
```java
public static ProductionHeadquarters getInstance() {
    if (singletonInstance == null) {
        singletonInstance = new ProductionHeadquarters();
    }
    return singletonInstance;
}
```

**Hinweis:** Nicht thread-safe! Könnte durch Double-Checked Locking verbessert werden:
```java
private static volatile ProductionHeadquarters singletonInstance;

public static ProductionHeadquarters getInstance() {
    if (singletonInstance == null) {
        synchronized (ProductionHeadquarters.class) {
            if (singletonInstance == null) {
                singletonInstance = new ProductionHeadquarters();
            }
        }
    }
    return singletonInstance;
}
```

---

### ✅ **4. Defensive Copying bei Status-Checks**
```java
public boolean getCargoHandoverToNextMaschineInProgress() {
    boolean copy = cargoHandoverToNextMaschineInProgress;
    if (copy) {
        cargoHandoverToNextMaschineInProgress = false;
    }
    return copy;
}
```

---

## Potenzielle Probleme und Lösungen

### ⚠️ Problem 1: Singleton nicht thread-safe

**Aktuell:**
```java
if (singletonInstance == null) {
    singletonInstance = new ProductionHeadquarters();
}
```

**Lösung:** Siehe "Best Practices" oben mit `volatile` und `synchronized`.

---

### ⚠️ Problem 2: Busy-Waiting bei Maschinen

**Aktuell:**
```java
while (!cargoNotified) {
    if (!remainingCapacity) {
        Thread.sleep(timeToSleep);  // Busy-waiting
    }
}
```

**Alternative:** Blocking Queue oder Condition Variables für effizientere Wartezeiten.

---

### ✅ Stärke: Robustes Error Handling

**Beispiel:**
```java
try {
    semaphore.acquire();
    // Operation
} catch (InterruptedException e) {
    throw new RuntimeException(e);
} finally {
    semaphore.release();
}
```

Garantiert, dass Semaphore auch bei Exceptions freigegeben wird.

---

## Zusammenfassung

### Stärken des Synchronisationsmodells:

1. **Konsistente Verwendung von Semaphoren** für alle kritischen Abschnitte
2. **Klare Verantwortlichkeiten** - jede Ressource hat genau ein schützendes Semaphore
3. **Deadlock-frei** durch No-Nested-Locks-Policy
4. **GUI-Thread-Koordination** durch Monitor-Pattern (WarehouseClerk & Supplier)
5. **Skalierbar** durch Worker-Pool (WarehouseClerk)
6. **Hybrides Cargo-Management** - Request-basiert (WarehouseClerk) + Timer-basiert (Supplier)

### Architektur-Highlights:

- **Pipeline-Architektur** mit asynchroner Kommunikation
- **Request-basiertes System** für Ressourcen-Anforderungen (Maschinen → WarehouseClerk)
- **Timer-basiertes System** für periodischen Nachschub (Supplier → MainDepot)
- **Priority-Scheduling** zur Vermeidung von Starvation
- **Animation-Integration** mit Thread-Synchronisation

### Design-Pattern:

1. **Singleton** (ProductionHeadquarters)
2. **Producer-Consumer** (Maschinen → Request Queue → WarehouseClerk)
3. **Pipeline** (Maschine → Maschine)
4. **Monitor** (WarehouseClerk & Supplier Animation-Sync)
5. **Worker Pool** (WarehouseClerk)
6. **Timer-based Worker** (Supplier)

### Thread-Rollen:

| Thread-Typ | Arbeitsweise | Synchronisation | Aufgabe |
|------------|--------------|-----------------|---------|
| **Maschine** | Kontinuierlich | storageSemaphore, notificationSemaphore, Polling-Flag | Produktion, Requests senden |
| **WarehouseClerk** | Request-basiert | Monitor (wait/notify) | Cargo zwischen Stationen transportieren |
| **Supplier** | Timer-basiert | Monitor (wait/notify) | MainDepot auffüllen & entleeren |
| **GUI** | Event-driven | setReady() Aufrufe, Polling, Callbacks | Animationen & User-Interaktion |

### GUI-Synchronisations-Patterns:

| Thread-Typ | Pattern | Blockierung | Initiator | Zweck |
|------------|---------|-------------|-----------|-------|
| **WarehouseClerk** | Monitor (wait/notify) | Ja | GUI (setReady) | Bewegungs-Animation |
| **Supplier** | Monitor (wait/notify) | Ja | GUI (setReady) | Bewegungs-Animation |
| **Maschine** | Polling + Callback | Nein | Maschine (Flag) | Cargo-Übergabe-Animation |

---

**Fazit:** Ihr Synchronisationsmodell ist **gut strukturiert**, **deadlock-frei** und nutzt bewährte Patterns. Die Verwendung von Semaphoren ist konsistent und korrekt implementiert. Die Trennung zwischen request-basiertem (WarehouseClerk) und timer-basiertem (Supplier) Cargo-Management ist ein elegantes Design. Kleinere Verbesserungen könnten beim Singleton-Pattern und bei der Effizienz von Warteschleifen vorgenommen werden.









