# Thread-Interaktionsdiagramm der Produktionslinie

## Übersicht der Thread-Kommunikation

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      ProductionHeadquarters (Singleton)                  │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  PriorityQueue<Request> requestQueue                             │   │
│  │  🔒 Semaphore requestQueueSemaphore                              │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
         ▲                                                      │
         │ addRequest()                          pollRequest()  │
         │                                                      ▼
    ┌────┴────────┐                              ┌──────────────────────┐
    │  Maschinen  │                              │  WarehouseClerk 1-N  │
    │  (Threads)  │                              │     (Threads)        │
    └─────────────┘                              └──────────────────────┘

                      ┌──────────────────────────┐
                      │   MainDepot (Station)    │
                      │  🔒 cargoStorageSemaphore│
                      └──────────────────────────┘
                                  ▲
                                  │
                    refillCargo() │ collectCargo()
                                  │
                      ┌───────────┴──────────┐
                      │   Supplier (Thread)  │
                      │   Periodische Läufe  │
                      └──────────────────────┘

════════════════════════════════════════════════════════════════════════════
```

## Detaillierte Thread-Architektur

### 1. Maschinen-Pipeline

```
┌──────────────────┐         ┌──────────────────┐         ┌──────────────────┐
│ ProductionMaschine│        │  ControlMachine   │        │ PackagingMaschine │
│    (Thread)      │───────▶│    (Thread)       │───────▶│    (Thread)      │
└──────────────────┘         └──────────────────┘         └──────────────────┘
│ 🔒 storageSemaphore│       │ 🔒 storageSemaphore│       │ 🔒 storageSemaphore│
│ 🔒 notificationSem │       │ 🔒 notificationSem │       │ 🔒 notificationSem │
│                    │       │                    │       │                    │
│ Map<Cargo,Integer> │       │ Map<Cargo,Integer> │       │ Map<Cargo,Integer> │
│      storage       │       │      storage       │       │      storage       │
└────────────────────┘       └────────────────────┘       └────────────────────┘
         │                            │                            │
         │ sendCargoRequest()         │ sendCargoRequest()         │ sendCargoRequest()
         └────────────────────────────┴────────────────────────────┘
                                      │
                                      ▼
                        ┌─────────────────────────┐
                        │  ProductionHeadquarters │
                        │     requestQueue        │
                        └─────────────────────────┘
```

### 2. Supplier-System (Periodischer Nachschub)

```
┌────────────────────────────────────────────────────────────────┐
│                      Supplier (Thread)                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  HashMap<Cargo, Integer> cargoStorage                    │  │
│  │  (Kein Semaphore - nur eigener Thread greift zu)         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  Timer: supplyInterval_ms (z.B. 30000ms = 30 Sekunden)         │
└────────────────────────────────────────────────────────────────┘
                            │
                            │ refillCargo(Material, qty)
                            │ collectCargo(Product, qty)
                            ▼
            ┌───────────────────────────────────┐
            │        MainDepot (Station)        │
            │  🔒 cargoStorageSemaphore         │
            │  Map<Cargo, Integer> cargoStorage │
            └───────────────────────────────────┘
                            ▲
                            │ handOverCargo()
                            │ resiveCargo()
                            │
            ┌───────────────┴──────────────────┐
            │                                  │
    ┌───────────────┐              ┌──────────────────┐
    │ WarehouseClerk│              │    Supplier      │
    │  (Threads)    │              │    (Thread)      │
    └───────────────┘              └──────────────────┘
    Request-basiert                Timer-basiert
```

### 2. Cargo-Flow mit Synchronisation

```
Maschine A (Thread)                    Maschine B (Thread)
──────────────────                     ──────────────────

┌─ Produktion ─────────────────────────────────────────┐
│ 1. produceProduct()                                  │
│ 2. checkStorageStatus()                              │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Kapazitätsprüfung ──────────────────────────────────┐
│ nextMaschine.getRemainingStorageCapacity(cargo)      │
│   ├─ storageSemaphore.acquire()                      │
│   ├─ Check: storage capacity                         │────┐
│   ├─ notificationSemaphore.acquire()                 │    │
│   ├─ Check: cargosOnTransit                          │    │ Thread-safe
│   ├─ notificationSemaphore.release()                 │    │ Zugriff
│   └─ storageSemaphore.release()                      │────┘
└──────────────────────────────────────────────────────┘
            │
            │ Kapazität OK?
            ▼
┌─ Cargo-Transit Notification ─────────────────────────┐
│ nextMaschine.addCargoTransitNotification(cargo)      │
│                                                       │────▶ ┌─ Maschine B ──────┐
│   notificationSemaphore.acquire()                    │      │ cargosOnTransit   │
│   cargosOnTransit.add(cargo)                         │      │ .add(cargo)       │
│   notificationSemaphore.release()                    │      └───────────────────┘
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ GUI Animation (separater Thread) ───────────────────┐
│ Cargo wird visuell bewegt                            │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Handover Complete Notification ─────────────────────┐
│ nextMaschine.notifyMachineCargoHandoverCompleted()   │────▶ ┌─ Maschine B ──────┐
│                                                       │      │ notificationSem   │
│   notificationSemaphore.acquire()                    │      │ .acquire()        │
│   cargo = cargosOnTransit.poll()                     │      │                   │
│   notificationSemaphore.release()                    │      │ resiveCargo()     │
│                                                       │      │   ├─ storageSem   │
│   storageSemaphore.acquire()                         │      │   ├─ storage.put()│
│   storage.put(cargo, currentQuantity + 1)            │      │   └─ release()    │
│   storageSemaphore.release()                         │      └───────────────────┘
└──────────────────────────────────────────────────────┘
```

### 3. WarehouseClerk Request-Handling

```
WarehouseClerk Thread                  MainDepot / Maschine
─────────────────────                  ────────────────────

┌─ Request abrufen ────────────────────────────────────┐
│ currentRequest = HQ.pollRequest()                    │
│   ├─ requestQueueSemaphore.acquire()                 │
│   ├─ request = requestQueue.poll()                   │
│   └─ requestQueueSemaphore.release()                 │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Travel to Origin ───────────────────────────────────┐
│ status = TRAVEL_TO_STATION                           │
│ awaitReady()  // synchronized wait                   │◀──── GUI: setReady()
│   └─ while(!ready) wait()                            │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Cargo abholen ──────────────────────────────────────┐
│ collectCargo(cargo, quantity)                        │
│   ├─ originStation.handOverCargo(cargo, quantity)    │────▶ ┌─ MainDepot ───────┐
│   │                                                   │      │ cargoStorageSem   │
│   │                                                   │      │ .acquire()        │
│   │                                                   │      │                   │
│   │                                                   │      │ cargoStorage      │
│   │                                                   │      │ .put(...)         │
│   │                                                   │      │ .release()        │
│   │                                                   │      └───────────────────┘
│   └─ Thread.sleep(timeForTask_ms)                    │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Travel to Destination ──────────────────────────────┐
│ status = TRANSPORT_CARGO                             │
│ awaitReady()  // synchronized wait                   │◀──── GUI: setReady()
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Cargo abliefern ────────────────────────────────────┐
│ refillCargo(cargo, quantity)                         │
│   ├─ destinationStation.resiveCargo(cargo, quantity) │────▶ ┌─ Maschine ────────┐
│   │                                                   │      │ storageSemaphore  │
│   │                                                   │      │ .acquire()        │
│   │                                                   │      │                   │
│   │                                                   │      │ storage           │
│   │                                                   │      │ .put(...)         │
│   │                                                   │      │ .release()        │
│   │                                                   │      └───────────────────┘
│   └─ Thread.sleep(timeForTask_ms)                    │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Request als erledigt markieren ─────────────────────┐
│ requestedMachine.markRequestAsCompleted(cargo)       │────▶ ┌─ Maschine ────────┐
│                                                       │      │ requestedCargoTypes│
│                                                       │      │ .put(cargo, false)│
│                                                       │      └───────────────────┘
└──────────────────────────────────────────────────────┘
```

## Synchronisations-Hierarchie

```
┌─────────────────────────────────────────────────────────────┐
│                    Synchronisation-Ebenen                    │
└─────────────────────────────────────────────────────────────┘

Ebene 1: Zentrale Request-Queue
═══════════════════════════════
  🔒 ProductionHeadquarters.requestQueueSemaphore
     │
     ├─▶ addRequest()    (aufgerufen von: allen Maschinen)
     └─▶ pollRequest()   (aufgerufen von: allen WarehouseClerks)


Ebene 2: Ressourcen-Storage (pro Station)
═══════════════════════════════════════
  🔒 MainDepot.cargoStorageSemaphore
     │
     ├─▶ resiveCargo()      (aufgerufen von: WarehouseClerks, Supplier)
     └─▶ handOverCargo()    (aufgerufen von: WarehouseClerks, Supplier)

  🔒 Maschine.storageSemaphore
     │
     ├─▶ storeProduct()           (aufgerufen von: eigenem Thread)
     ├─▶ resiveCargo()            (aufgerufen von: WarehouseClerks)
     ├─▶ handOverCargo()          (aufgerufen von: WarehouseClerks)
     └─▶ getRemainingStorageCapacity() (aufgerufen von: Vorgänger-Maschine)


Ebene 3: Transit-Tracking (pro Maschine)
════════════════════════════════════════
  🔒 Maschine.notificationSemaphore
     │
     ├─▶ addCargoTransitNotification()  (aufgerufen von: Vorgänger-Maschine)
     ├─▶ notifyMachineCargoHandoverCompleted() (aufgerufen von: GUI)
     └─▶ getRemainingStorageCapacity()  (aufgerufen von: Vorgänger-Maschine)


Ebene 4: GUI-Thread Synchronisation
═══════════════════════════════════
  🔒 WarehouseClerk synchronized methods (Monitor)
     │
     ├─▶ awaitReady()  (wartet auf GUI)
     └─▶ setReady()    (aufgerufen von: GUI Thread)

  🔒 Supplier synchronized methods (Monitor)
     │
     ├─▶ awaitReady()  (wartet auf GUI)
     └─▶ setReady()    (aufgerufen von: GUI Thread)
```

## Race Condition Schutz

### Szenario 1: Mehrere Maschinen senden Requests

```
Thread 1 (Maschine A)          Thread 2 (Maschine B)
─────────────────────          ─────────────────────
addRequest(requestA)           addRequest(requestB)
    │                              │
    ▼                              ▼
acquire Semaphore              wartet...
    │                              │
    ▼                              │
requestQueue.add(A)                │
    │                              │
    ▼                              │
release Semaphore                  │
                                   ▼
                            acquire Semaphore
                                   │
                                   ▼
                            requestQueue.add(B)
                                   │
                                   ▼
                            release Semaphore

Resultat: Beide Requests korrekt in Queue ✅
```

### Szenario 2: Gleichzeitiger Storage-Zugriff

```
Thread 1 (WarehouseClerk)      Thread 2 (Maschine)
─────────────────────          ───────────────────
handOverCargo(wood, 5)         storeProduct(chair)
    │                              │
    ▼                              ▼
acquire storageSemaphore       wartet...
    │                              │
    ▼                              │
currentQty = storage.get(wood)     │
storage.put(wood, qty-5)           │
    │                              │
    ▼                              │
release storageSemaphore           │
                                   ▼
                            acquire storageSemaphore
                                   │
                                   ▼
                            currentQty = storage.get(chair)
                            storage.put(chair, qty+1)
                                   │
                                   ▼
            release Semaphore

Resultat: Keine Daten-Corruption ✅
```

### 4. Supplier Supply-Routine Flow

```
Supplier Thread (periodisch)                   MainDepot
────────────────────────────                   ─────────

┌─ Timer Trigger ──────────────────────────────────────┐
│ Thread.sleep(supplyInterval_ms) abgelaufen           │
│ → starte supplyRoutine()                             │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Cargo-Storage initialisieren ───────────────────────┐
│ int perMaterial = maxCapacity / Material.values().length │
│ for (Material m : Material.values())                 │
│   cargoStorage.put(m, perMaterial)                   │
│ cargoStorage.put(Product.SCRAP, 0)                   │
│ cargoStorage.put(Product.PACKAGE, 0)                 │
│                                                       │
│ ⚠️ KEIN Semaphore - nur Supplier-Thread hat Zugriff  │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Travel to MainDepot ────────────────────────────────┐
│ task = DELIVERING                                    │
│ idOfCurrentDestinationStation = mainDepotId          │
│ awaitReady()  // synchronized wait                   │◀──── GUI: setReady()
│   └─ while(!ready) wait()                            │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Refill & Collect ───────────────────────────────────┐
│ Thread.sleep(supplyTimer_ms)                         │
│                                                       │
│ // Materialien liefern                               │
│ for (Material m : Material.values())                 │
│   refillCargo(m, currentQuantity)                    │────▶ ┌─ MainDepot ───────┐
│     └─ mainDepot.resiveCargo(m, qty)                 │      │ cargoStorageSem   │
│                                                       │      │ .acquire()        │
│                                                       │      │                   │
│                                                       │      │ cargoStorage      │
│                                                       │      │ .put(m, qty)      │
│                                                       │      │ .release()        │
│                                                       │      └───────────────────┘
│ // Produkte abholen                                  │
│ collectCargo(Product.PACKAGE, freeCapacity)          │────▶ ┌─ MainDepot ───────┐
│   └─ mainDepot.handOverCargo(PACKAGE, qty)           │      │ cargoStorageSem   │
│                                                       │      │ .acquire()        │
│                                                       │      │                   │
│                                                       │      │ cargoStorage      │
│                                                       │      │ .put(PACKAGE,     │
│                                                       │      │      qty-removed) │
│                                                       │      │ .release()        │
│                                                       │      └───────────────────┘
│ // Schrott abholen                                   │
│ collectCargo(Product.SCRAP, freeCapacity)            │────▶ MainDepot (analog)
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Travel away ────────────────────────────────────────┐
│ task = TRANSPORTING                                  │
│ idOfCurrentDestinationStation = -1 (außerhalb)       │
│ awaitReady()  // synchronized wait                   │◀──── GUI: setReady()
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Sleep until next cycle ─────────────────────────────┐
│ Thread.sleep(supplyInterval_ms)                      │
│ → Warte z.B. 30 Sekunden                             │
└──────────────────────────────────────────────────────┘
            │
            └─▶ Loop zurück zu Timer Trigger
```

**Besonderheiten des Supplier-Threads:**

1. **Kein Request-System:**
   - Supplier arbeitet **nicht** mit der Request Queue
   - Komplett unabhängig von Maschinen-Anforderungen
   - Periodischer, vorhersehbarer Ablauf

2. **Eigener Cargo-Storage:**
   - HashMap ohne Semaphore-Schutz
   - Nur vom Supplier-Thread selbst genutzt
   - Wird bei jedem Zyklus neu initialisiert

3. **Zwei-Wege-Kommunikation mit MainDepot:**
   - **Hinweg:** Materialien liefern (refillCargo)
   - **Rückweg:** Fertigprodukte & Schrott abholen (collectCargo)
   - Beide nutzen MainDepot's `cargoStorageSemaphore`

4. **GUI-Synchronisation:**
   - Wartet zweimal auf GUI: Hinfahrt & Rückfahrt
   - Gleicher Mechanismus wie WarehouseClerk (wait/notify)
   - `idOfCurrentDestinationStation` = -1 bedeutet "außerhalb der Fabrik"

5. **Graceful Shutdown:**
   - Fängt `InterruptedException` ab
   - Setzt Status auf `STOPPED`
   - Ruft `Thread.currentThread().interrupt()` auf
   - Verlässt Loop (im Gegensatz zu anderen Threads)

---

### 5. Maschine-GUI-Synchronisation (Polling + Callback)

```
Maschine A Thread                GUI Thread                       Maschine B Thread
─────────────────                ──────────                       ─────────────────

┌─ Cargo produzieren ──────────────────────────────────┐
│ produceProduct()                                     │
│ storeProductOrDeliverToNextMachine()                 │
└──────────────────────────────────────────────────────┘
            │
            ▼
┌─ Nachfolger benachrichtigen ─────────────────────────┐
│ deliverToNextMachine(cargo)                          │
│   ├─ getRemainingStorageCapacity(cargo) ────────────────▶ storageSemaphore.acquire()
│   │                                                         check storage + cargosOnTransit
│   │                                                         storageSemaphore.release()
│   │◀──────────────────────────────────────────────────── return hasCapacity
│   │                                                   │
│   ├─ notifyNextMaschineOfCargoSending(cargo) ────────────▶ notificationSemaphore.acquire()
│   │                                                         cargosOnTransit.add(cargo)
│   │                                                         notificationSemaphore.release()
│   │                                                   │
│   └─ cargoHandoverToNextMaschineInProgress = true    │
└──────────────────────────────────────────────────────┘
            │
            │
            │            ┌─ Polling (60 FPS) ──────────────────┐
            │            │ onUpdate() (jeden Frame)            │
            │            │   ↓                                 │
            │◀───────────│ if (getCargoHandoverInProgress())   │
            │            │   ↓ (true)                          │
            │            │ cargoHandoverInProgress = false     │
            │            │   (Auto-Reset)                      │
            │            └─────────────────────────────────────┘
            │                        ↓
            │            ┌─ Animation starten ─────────────────┐
            │            │ spawnItemOnBelt(belt)               │
            │            │   ↓                                 │
            │            │ [Item-Entity erstellen]             │
            │            │   ↓                                 │
            │            │ [Item bewegt sich zu Maschine B]    │
            │            └─────────────────────────────────────┘
            │                        ↓
            │            ┌─ Kollisions-Callback ───────────────┐
            │            │ onCollision(machineB, item)         │
            │            │   ↓                                 │
            │            │ if (isDoorOpen())                   │
            │            │   ↓                                 │
            │            │ item.removeFromWorld()              │
            │            │   ↓                                 │
            │            │ machineB.notifyMachineCargoHandoverCompleted() ──▶ notificationSem.acquire()
            │            │                                                    cargo = cargosOnTransit.poll()
            │            │                                                    notificationSem.release()
            │            │                                                    ↓
            │            │                                                    resiveCargo(cargo, 1)
            │            │                                                      ├─ storageSemaphore.acquire()
            │            │                                                      ├─ storage.put(cargo, qty+1)
            │            │                                                      └─ storageSemaphore.release()
            │            └─────────────────────────────────────┘
```

**Warum Polling statt wait/notify?**

1. **Maschinen dürfen nicht blockieren:**
   - Produktion läuft kontinuierlich
   - wait() würde Thread stoppen

2. **GUI bestimmt Timing:**
   - Animation-Geschwindigkeit variabel
   - Spiel-Engine hat Kontrolle

3. **Asynchron:**
   - Maschine arbeitet weiter während Animation
   - Kein Warten auf Animations-Ende

4. **Performance:**
   - Polling ist leichtgewichtig (nur boolean-Check)
   - 60 FPS = ~16ms Intervall

**Vergleich: Maschine vs. WarehouseClerk**

| Aspekt | WarehouseClerk | Maschine |
|--------|----------------|----------|
| **GUI-Sync** | wait/notify | Polling + Callback |
| **Blockierung** | Ja (awaitReady) | Nein |
| **Pattern** | Monitor | Flag-based |
| **Initiator** | GUI (setReady) | Maschine (Flag setzen) |
| **Frequenz** | Event (1x) | Polling (60 FPS) |

```

## Deadlock-Freiheit Beweis

### Bedingung 1: Mutual Exclusion
✅ **Erfüllt** durch Semaphore mit 1 Permit

### Bedingung 2: Hold and Wait
✅ **NICHT erfüllt** - Threads halten nie ein Lock während sie auf ein anderes warten
   - Jeder Thread erwirbt maximal 1 Semaphore gleichzeitig
   - Beispiel: `getRemainingStorageCapacity()` erwirbt storageSemaphore, 
     gibt es frei, dann erwirbt notificationSemaphore

```java
// Korrekt: Keine verschachtelten Locks
storageSemaphore.acquire();
// ... Arbeit ...
storageSemaphore.release();

notificationSemaphore.acquire();
// ... Arbeit ...
notificationSemaphore.release();
```

### Bedingung 3: No Preemption
✅ **Erfüllt** - Locks können nicht weggenommen werden

### Bedingung 4: Circular Wait
✅ **NICHT erfüllt** - Keine zirkulären Abhängigkeiten
   - Request Queue → Maschinen (einseitig)
   - Maschine A → Maschine B → Maschine C (Pipeline, keine Zyklen)

**Fazit: DEADLOCK-FREI** ✅ (Hold-and-Wait & Circular-Wait werden vermieden)

## Performance-Optimierungen

### 1. Granularität der Locks

```
❌ SCHLECHT: Ein globales Lock für alles
✅ GUT: Separate Locks pro Ressource

MainDepot.cargoStorageSemaphore     ┐
Maschine1.storageSemaphore          ├─ Parallel zugreifbar
Maschine2.storageSemaphore          │  Keine Konflikte
ProductionHQ.requestQueueSemaphore  ┘
```

### 2. Try-Finally für garantierte Release

```java
semaphore.acquire();
try {
    // Kritischer Abschnitt
    // Kann Exception werfen
} finally {
    semaphore.release();  // ✅ Immer ausgeführt
}
```

### 3. Daemon Threads vermeiden Ressourcen-Leaks

```java
setDaemon(true);  // ✅ Automatisches Cleanup beim Exit
```

## Zusammenfassung

### Thread-Typen
1. **Maschinen-Threads** (ProductionMaschine, ControlMachine, PackagingMaschine)
   - Endlos-Schleife mit Produktionszyklen
   - Senden Requests bei Ressourcenmangel
   - Übergeben Cargo an Nachfolger
   - **GUI-Sync:** Polling + Callback (nicht-blockierend)

2. **WarehouseClerk-Threads**
   - Worker-Pool für Cargo-Transport
   - Holen Requests aus zentraler Queue
   - Koordinieren mit GUI via wait/notify
   - **Request-basiert** (reaktiv)
   - **GUI-Sync:** Monitor-Pattern (blockierend)

3. **Supplier-Thread**
   - Periodischer Nachschub für MainDepot
   - Liefert Materialien (Wood, Metal, Plastic, etc.)
   - Holt Produkte (PACKAGE) und Schrott (SCRAP) ab
   - Koordiniert mit GUI via wait/notify
   - **Timer-basiert** (proaktiv mit `supplyInterval_ms`)
   - Eigener Cargo-Storage ohne Semaphore (nur vom eigenen Thread genutzt)
   - **GUI-Sync:** Monitor-Pattern (blockierend)

4. **GUI-Thread** (JavaFX Application Thread)
   - Animationen
   - Ruft `setReady()` auf WarehouseClerks und Supplier
   - **Pollt** Maschinen-Flags (60 FPS)
   - Ruft `notifyMachineCargoHandoverCompleted()` auf Maschinen (Callback)

### GUI-Synchronisations-Patterns

| Thread-Typ | Pattern | Blockierung | Initiator | Frequenz |
|------------|---------|-------------|-----------|----------|
| **WarehouseClerk** | Monitor (wait/notify) | ✅ Ja | GUI (setReady) | Event (1x) |
| **Supplier** | Monitor (wait/notify) | ✅ Ja | GUI (setReady) | Event (1x) |
| **Maschine** | Polling + Callback | ❌ Nein | Maschine (Flag) | Polling (60 FPS) |

### Synchronisationsmittel
- **Semaphore** (binär, 1 Permit) - Hauptmechanismus
- **synchronized + wait/notify** - GUI-Koordination
- **volatile** - (noch nicht verwendet, könnte verbessert werden)

### Lock-Hierarchie
```
Keine Hierarchie! → Deadlock-frei
Jeder Thread erwirbt maximal 1 Lock gleichzeitig
```








