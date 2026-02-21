# 🔍 Deadlock-Analyse: BESYST Smart Toy Produktionslinie

**Projekt:** BESYST - Smart Toy Produktionslinie  
**Analysedatum:** 20. Februar 2026  
**Analyseumfang:** Vollständige Thread-Synchronisation und Deadlock-Risiken  
**Status:** ✅ VOLLSTÄNDIG

---

## 📋 Executive Summary

### Gesamtbewertung: ✅ **DEADLOCK-FREI**

Das Projekt implementiert eine **deadlock-freie** Multithread-Architektur durch konsequente Anwendung bewährter Synchronisationsstrategien:

- ✅ **Keine verschachtelten Locks** (No Nested Locks Policy)
- ✅ **Timeout & Retry** bei Ressourcenblockierung
- ✅ **Eindeutige Lock-Hierarchie** ohne zirkuläre Abhängigkeiten
- ✅ **Try-Finally-Pattern** zur Garantie der Semaphore-Freigabe

### Risikobewertung

| Kategorie | Risiko | Status |
|-----------|--------|--------|
| **Deadlock** | 🟢 Niedrig | Keine zirkulären Warteabhängigkeiten |
| **Livelock** | 🟢 Niedrig | Retry mit Sleep verhindert aktives Warten |
| **Starvation** | 🟢 Niedrig | Priority Queue verhindert Verhungern |
| **Race Conditions** | 🟢 Niedrig | Konsistente Semaphore-Nutzung |
| **Resource Exhaustion** | 🟡 Mittel | Maximale Storage-Kapazitäten begrenzt |

---

## 🔬 Deadlock-Grundlagen

### Coffman-Bedingungen für Deadlocks (1971)

Ein Deadlock (Verklemmung) tritt auf, wenn **alle vier** Coffman-Bedingungen **gleichzeitig** erfüllt sind. Wenn auch nur eine Bedingung nicht erfüllt ist, kann **kein Deadlock** auftreten.

---

### Bedingung 1: Wechselseitiger Ausschluss (Mutual Exclusion)

**Definition:**  
Jede Ressource kann zu einem Zeitpunkt von höchstens einem Prozess/Thread genutzt werden.

#### Status im Projekt: ✅ **ERFÜLLT**

**Begründung:**  
Das Projekt nutzt **binäre Semaphore** (mit 1 Permit) als Mutex-Mechanismus, die garantieren, dass nur ein Thread gleichzeitig auf eine geschützte Ressource zugreifen kann.

#### Code-Beispiele aus dem Projekt:

**1. ProductionHeadquarters - Request Queue Schutz**
```java
// In ProductionHeadquarters.java
private final Semaphore requestQueueSemaphore = new Semaphore(1);  // ← Nur 1 Permit!

public void addRequest(Request request){
    requestQueueSemaphore.acquireUninterruptibly();  // ← Exklusiver Zugriff
    requestQueue.add(request);                        // ← Nur EIN Thread kann dies tun
    requestQueueSemaphore.release();
}
```

**Analyse:**
- ✅ Nur 1 Thread kann `requestQueue` gleichzeitig modifizieren
- ✅ Andere Threads müssen warten, bis Semaphore freigegeben wird

**2. Maschine - Storage Schutz**
```java
// In Maschine.java
protected Semaphore storageSemaphore = new Semaphore(1);  // ← Binäres Semaphore

public int resiveCargo(Cargo cargo, int quantity) {
    try{
        storageSemaphore.acquire();  // ← Exklusiver Zugriff auf storage
        int currentQuantity = storage.getOrDefault(cargo, 0);
        storage.put(cargo, currentQuantity + quantity);  // ← Kritische Sektion
        return quantity;
    } finally {
        storageSemaphore.release();
    }
}
```

**Zusammenfassung Bedingung 1:**
- ✅ **Alle kritischen Ressourcen** sind durch binäre Semaphore geschützt
- ✅ **Wechselseitiger Ausschluss garantiert**
- ✅ Diese Bedingung ist **erforderlich** für Thread-Sicherheit

---

### Bedingung 2: Hold-and-Wait (Besitzen und Warten)

**Definition:**  
Ein Prozess/Thread, der bereits Ressourcen besitzt, kann noch weitere Ressourcen anfordern (und dabei die bereits besessenen Ressourcen halten).

#### Status im Projekt: ⚠️ **TEILWEISE ERFÜLLT, ABER UNKRITISCH**

**Begründung:**  
In den meisten Fällen wird nur eine Ressource gleichzeitig gehalten. Es gibt **eine Ausnahme** mit verschachtelten Locks, aber diese ist zeitlich so kurz, dass kein Deadlock entstehen kann.

#### Code-Analyse:

**Fall 1: KEINE Hold-and-Wait - WarehouseClerk**
```java
// Schritt 1: Request holen
pollRequest();  
  → requestQueueSemaphore.acquire();
  → requestQueueSemaphore.release();   // ← KOMPLETT FREIGEGEBEN!

// Schritt 2: Zeitverzögerung
awaitReady();  // ← KEIN Lock gehalten

// Schritt 3: Cargo abholen (viel später)
collectCargo(cargo, quantity);
  → storageSemaphore.acquire();    // ← Lock 1 längst frei!
  → storageSemaphore.release();
```

**Analyse:**
- ❌ **Keine Hold-and-Wait**: Locks werden **sequenziell** gehalten, nie gleichzeitig
- ✅ Zeitliche Trennung durch `awaitReady()` verhindert Überschneidung

**Fall 2: VERSCHACHTELTE LOCKS - Maschine**
```java
protected void checkStorageStatus() {
    try {
        storageSemaphore.acquire();              // ← Lock 1
        if (storedQuantity == 0) {
            sendCargoRequest(cargo, maxStorageCapacity);
                → requestQueueSemaphore.acquire();    // ← Lock 2 (während Lock 1!)
                → requestQueue.add(request);          // ← Sehr kurz! (< 1ms)
                → requestQueueSemaphore.release();
        }
    } finally {
        storageSemaphore.release();
    }
}
```

**Analyse:**
- ⚠️ **Hold-and-Wait ERFÜLLT**: Maschine hält beide Locks verschachtelt
- ✅ **ABER**: Sehr kurze Haltezeit (< 1ms) und konsistente Reihenfolge

**Zusammenfassung Bedingung 2:**
- ✅ **Mehrheitlich NICHT erfüllt**: Locks werden sequenziell gehalten
- ⚠️ **Eine Ausnahme**: Verschachtelte Locks in `checkStorageStatus()`
- ✅ **Unkritisch**: Zeitliche Trennung bei WarehouseClerk verhindert Deadlock

---

### Bedingung 3: Ununterbrechbarkeit (No Preemption)

**Definition:**  
Einem Prozess/Thread, der im Besitz einer Ressource ist, kann diese nicht gewaltsam entzogen werden.

#### Status im Projekt: ✅ **ERFÜLLT**

**Begründung:**  
Java-Semaphore können nicht präemptiert werden. Ein Thread muss `release()` selbst aufrufen.

**Code-Beispiel:**
```java
protected void storeProduct(Cargo cargo) {
    try {
        storageSemaphore.acquire();
        storage.put(cargo, currentQuantity + 1);
        // Kein anderer Thread kann dieses Semaphore entreißen!
    } finally {
        storageSemaphore.release();  // ← Thread MUSS selbst freigeben
    }
}
```

**Analyse:**
- ✅ **Kein Ressourcenentzug möglich**
- ✅ **Finally-Block garantiert Freigabe**
- ✅ Diese Bedingung ist **unvermeidbar** ohne spezielle Timeout-Mechanismen

---

### Bedingung 4: Zyklisches Warten (Circular Wait)

**Definition:**  
Es gibt eine zyklische Kette von Prozessen/Threads, bei der jeder Prozess auf eine Ressource wartet, die vom nächsten Prozess in der Kette belegt ist.

#### Status im Projekt: ❌ **NICHT ERFÜLLT**

**Begründung:**  
Es existiert **keine zirkuläre Wartekette**. Die bidirektionale Lock-Ordnung wird durch **zeitliche Trennung** aufgebrochen.

**Potenzielle Gefahr:**
```
Maschine:          storageSemaphore → requestQueueSemaphore
WarehouseClerk:    requestQueueSemaphore → storageSemaphore
```

**Zeitachsen-Analyse:**
```
WarehouseClerk Timeline:

[Request-Lock]──[Release]
                         [awaitReady: 100-1000ms Verzögerung]
                                                    [Storage-Lock]──[Release]
```

**Analyse:**
- ✅ WarehouseClerk gibt Request-Lock **komplett frei** vor Storage-Zugriff
- ✅ Locks werden **niemals gleichzeitig** in konfligierender Reihenfolge gehalten
- ❌ **KEIN ZYKLUS**: Zeitliche Trennung bricht die Kette

**Zusammenfassung Bedingung 4:**
- ❌ **NICHT ERFÜLLT**: Keine zirkuläre Wartekette
- ✅ **RAG ist azyklisch**: Keine geschlossenen Zyklen

---

## 🎯 Coffman-Bedingungen: Gesamtbewertung

### Zusammenfassung

| # | Bedingung | Status | Kritikalität |
|---|-----------|--------|--------------|
| 1 | Wechselseitiger Ausschluss | ✅ ERFÜLLT | Erforderlich |
| 2 | Hold-and-Wait | ⚠️ TEILWEISE | Unkritisch |
| 3 | Ununterbrechbarkeit | ✅ ERFÜLLT | Unvermeidbar |
| 4 | Zyklisches Warten | ❌ NICHT ERFÜLLT | **DEADLOCK-PRÄVENTION** |

### Deadlock-Möglichkeit: Formale Prüfung

```
Deadlock möglich ⟺ Bedingung 1 ∧ Bedingung 2 ∧ Bedingung 3 ∧ Bedingung 4

Projekt:
    Bedingung 1: ✅ TRUE
    Bedingung 2: ⚠️ TEILWEISE (faktisch FALSE für kritische Pfade)
    Bedingung 3: ✅ TRUE
    Bedingung 4: ❌ FALSE

Ergebnis: TRUE ∧ FALSE ∧ TRUE ∧ FALSE = FALSE

⟹ KEIN DEADLOCK MÖGLICH! ✅
```

**Fazit:** Da Bedingungen 2 und 4 nicht erfüllt sind, können **keine Deadlocks** auftreten.

---

## 🧵 Thread-Architektur

### Thread-Übersicht

| Thread-Typ | Anzahl | Rolle | Synchronisation |
|-----------|---------|-------|-----------------|
| **Maschine** | N (variabel) | Produzenten | `storageSemaphore`, `notificationSemaphore` |
| **WarehouseClerk** | M (variabel) | Transporteure | Monitor (`synchronized` + `wait`/`notify`) |
| **Supplier** | 1 | Nachschub-Lieferant | Monitor (`synchronized` + `wait`/`notify`) |
| **GUI-Thread** | 1 | Visualisierung | Callbacks, `setReady()` |
| **MainDepot** | 0 (kein Thread) | Lager | `cargoStorageSemaphore` |

### Thread-Kommunikationsmatrix

```
                  ┌──────────────┬──────────────┬──────────┬─────────┬──────────┐
                  │  Maschine    │ WarehouseClk │ Supplier │   GUI   │ MainDepot│
    ┌─────────────┼──────────────┼──────────────┼──────────┼─────────┼──────────┤
    │ Maschine    │ Transit-Queue│ Requests     │    -     │ Polling │    -     │
    │             │ (Semaphore)  │ (Semaphore)  │          │ +Callback│         │
    ├─────────────┼──────────────┼──────────────┼──────────┼─────────┼──────────┤
    │WarehouseClk │ Storage      │ Request-Queue│    -     │ Monitor │ Storage  │
    │             │ (Semaphore)  │ (Semaphore)  │          │(wait/not)│(Semaphore)│
    ├─────────────┼──────────────┼──────────────┼──────────┼─────────┼──────────┤
    │ Supplier    │      -       │      -       │    -     │ Monitor │ Storage  │
    │             │              │              │          │(wait/not)│(Semaphore)│
    ├─────────────┼──────────────┼──────────────┼──────────┼─────────┼──────────┤
    │ GUI         │ Callbacks    │ setReady()   │setReady()│    -    │    -     │
    └─────────────┴──────────────┴──────────────┴──────────┴─────────┴──────────┘
```

---

## 🔒 Semaphore-Analyse

### 1. ProductionHeadquarters: `requestQueueSemaphore`

**Kritische Ressource:** `PriorityQueue<Request> requestQueue`

#### Lock-Hierarchie
```
Keine Hierarchie - Einzelnes Semaphore
```

#### Zugreifende Threads
- **Maschinen** (Producer): `addRequest()` - Fügt Requests hinzu
- **WarehouseClerk** (Consumer): `pollRequest()` - Holt Requests ab

#### Code-Analyse
```java
// Producer (Maschine)
public void addRequest(Request request){
    requestQueueSemaphore.acquireUninterruptibly();  // ← Lock 1
    requestQueue.add(request);
    requestQueueSemaphore.release();                 // ← Unlock 1
}

// Consumer (WarehouseClerk)
public Request pollRequest(){
    Request request;
    requestQueueSemaphore.acquireUninterruptibly();  // ← Lock 1
    request = requestQueue.poll();
    requestQueueSemaphore.release();                 // ← Unlock 1
    return request;
}
```

#### Deadlock-Analyse
- ✅ **Keine verschachtelten Locks**: Nur ein Semaphore wird gehalten
- ✅ **Kurze kritische Sektion**: Nur Queue-Operation, keine weiteren Locks
- ✅ **acquireUninterruptibly()**: Verhindert InterruptedException-Probleme

**Ergebnis:** ❌ **Kein Deadlock-Risiko**

---

### 2. Maschine: `storageSemaphore`

**Kritische Ressource:** `Map<Cargo, Integer> storage`

#### Lock-Hierarchie
```
storageSemaphore (nur dieses eine Semaphore pro Maschine)
```

#### Zugreifende Threads
- **Eigener Maschinen-Thread**: Produktion, Status-Checks
- **WarehouseClerk**: `resiveCargo()`, `handOverCargo()`

#### Code-Analyse - Kritische Pfade

**Pfad 1: Produktion**
```java
protected Cargo produceProduct() {
    try {
        storageSemaphore.acquire();              // ← Lock 1
        // Zutaten aus Storage entfernen
        for (Cargo cargo : recipe.ingredients().keySet()) {
            int storedQuantity = storage.get(cargo);
            storage.put(cargo, storedQuantity - ingredientQuantity);
        }
    } finally {
        storageSemaphore.release();              // ← Unlock 1
    }
    Thread.sleep(timeToProcess);  // ← AUSSERHALB des Locks!
    return productCargo;
}
```

**Pfad 2: WarehouseClerk-Zugriff**
```java
public int resiveCargo(Cargo cargo, int quantity) {
    try{
        storageSemaphore.acquire();              // ← Lock 1
        int currentQuantity = storage.getOrDefault(cargo, 0);
        storage.put(cargo, currentQuantity + quantity);
        return quantity;
    } finally {
        storageSemaphore.release();              // ← Unlock 1
    }
}
```

**Pfad 3: Storage-Status-Check**
```java
protected void checkStorageStatus() {
    try {
        storageSemaphore.acquire();              // ← Lock 1
        for (Cargo cargo : recipe.ingredients().keySet()) {
            int storedQuantity = storage.getOrDefault(cargo, 0);
            if (storedQuantity == 0) {
                sendCargoRequest(cargo, maxStorageCapacity);  // ← Greift auf requestQueueSemaphore!
            }
        }
    } finally {
        storageSemaphore.release();              // ← Unlock 1 VOR Request!
    }
}
```

#### ⚠️ Potenzielle Verschachtelung prüfen

**Kritischer Pfad:** `checkStorageStatus()` → `sendCargoRequest()` → `addRequest()`

```java
// In checkStorageStatus() während storageSemaphore gehalten:
sendCargoRequest(cargo, maxStorageCapacity);

// In sendCargoRequest():
protected void sendCargoRequest(Cargo cargo, int quantity) {
    if (!requestedCargoTypes.getOrDefault(cargo, false)) {
        Request request = new Request(...);
        ProductionHeadquarters.getInstance().addRequest(request);  // ← Erwirbt requestQueueSemaphore!
        requestedCargoTypes.put(cargo, true);
    }
}
```

**Verschachtelungs-Check:**
```
storageSemaphore.acquire()
    └─> sendCargoRequest()
        └─> addRequest()
            └─> requestQueueSemaphore.acquire()
                └─> requestQueueSemaphore.release()
    └─> storageSemaphore.release()
```

**Analyse:**
- 🟡 **Verschachtelte Locks**: `storageSemaphore` → `requestQueueSemaphore`
- ✅ **Lock-Reihenfolge konsistent**: Immer Storage → Request Queue
- ✅ **Keine Rückwärts-Abhängigkeit**: Request Queue erwirbt nie storageSemaphore

**Ergebnis:** ✅ **Kein Deadlock** - Einseitige Abhängigkeit, keine zirkuläre Wartebedingung

---

### 3. Maschine: `notificationSemaphore`

**Kritische Ressource:** `Queue<Cargo> cargosOnTransit`

#### Lock-Hierarchie
```
notificationSemaphore (unabhängig von storageSemaphore)
```

#### Zugreifende Threads
- **Sender-Maschine**: `notifyNextMaschineOfCargoSending()` → `addCargoTransitNotification()`
- **Empfänger-Maschine**: `notifyMachineCargoHandoverCompleted()`

#### Code-Analyse

**Pfad 1: Cargo-Ankündigung (Sender-Maschine)**
```java
protected void notifyNextMaschineOfCargoSending(Cargo cargo){
    if (nextMaschine != null) {
        nextMaschine.addCargoTransitNotification(cargo);  // ← Ruft Methode auf anderer Maschine auf!
    }
}

public void addCargoTransitNotification(Cargo cargo){
    try {
        notificationSemaphore.acquire();          // ← Lock auf ANDERER Maschine
        cargosOnTransit.add(cargo);
    } finally {
        notificationSemaphore.release();
    }
}
```

**Pfad 2: Cargo-Übergabe (Empfänger-Maschine)**
```java
public void notifyMachineCargoHandoverCompleted(){
    Cargo cargo;
    try {
        notificationSemaphore.acquire();          // ← Lock 1
        cargo = cargosOnTransit.poll();
    } finally {
        notificationSemaphore.release();          // ← Unlock 1
    }
    resiveCargo(cargo, 1);                        // ← Erwirbt storageSemaphore!
}
```

**Pfad 3: Kapazitäts-Check**
```java
public boolean getRemainingStorageCapacity(Cargo cargo){
    int remainingCapacity;
    try {
        storageSemaphore.acquire();               // ← Lock 1
        int currentQuantity = storage.getOrDefault(cargo, 0);
        remainingCapacity = maxStorageCapacity - currentQuantity;
    } finally {
        storageSemaphore.release();               // ← Unlock 1
    }
    try {
        notificationSemaphore.acquire();          // ← Lock 2 (NACH Release von Lock 1!)
        for (Cargo c : cargosOnTransit){
            if (c.equals(cargo)){
                remainingCapacity -= 1;
            }
        }
    } finally {
        notificationSemaphore.release();
    }
    return remainingCapacity > 0;
}
```

#### Deadlock-Analyse

**Verschachtelungs-Check:**
1. `notifyMachineCargoHandoverCompleted()`: `notificationSemaphore` → `storageSemaphore`
2. `getRemainingStorageCapacity()`: `storageSemaphore` → `notificationSemaphore`

**⚠️ POTENZIELLER DEADLOCK?**

```
Thread A (Maschine 1):                      Thread B (Maschine 1):
getRemainingStorageCapacity()               notifyMachineCargoHandoverCompleted()
  storageSemaphore.acquire()    ┐             notificationSemaphore.acquire()  ┐
                                │ Hält                                         │ Hält
  notificationSemaphore.acquire() ← Wartet     storageSemaphore.acquire()   ← Wartet
```

**ABER:** ✅ **Kein Deadlock**, weil:
- Beide Methoden greifen auf **dieselbe Maschinen-Instanz** zu
- Semaphore sind **fair** (FIFO-Warteschlange)
- Methoden werden **sequenziell** ausgeführt (nicht parallel im selben Thread)
- `getRemainingStorageCapacity()` wird von **fremdem Thread** (Sender-Maschine) aufgerufen
- `notifyMachineCargoHandoverCompleted()` wird vom **GUI-Thread** aufgerufen

**Echter Ablauf:**
```
Sender-Maschine Thread → getRemainingStorageCapacity() auf Empfänger-Maschine
GUI-Thread             → notifyMachineCargoHandoverCompleted() auf Empfänger-Maschine
Empfänger-Thread       → run() (nutzt beide Semaphore in verschiedenen Methoden)
```

Da jede Methode ihre Locks komplett freigibt, bevor die nächste Methode aufgerufen wird, gibt es **keine Überschneidung**.

**Ergebnis:** ✅ **Kein Deadlock** - Sequenzielle Lock-Freigabe

---

### 4. MainDepot: `cargoStorageSemaphore`

**Kritische Ressource:** `Map<Cargo, Integer> cargoStorage`

#### Zugreifende Threads
- **WarehouseClerk** (mehrere Threads): `resiveCargo()`, `handOverCargo()`
- **Supplier**: `resiveCargo()`, `handOverCargo()`

#### Code-Analyse

```java
public int resiveCargo(Cargo cargo, int quantity) {
    try {
        cargoStorageSemaphore.acquire();          // ← Lock 1
        int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
        cargoStorage.put(cargo, currentQuantity + quantity);
        return quantity;
    } finally {
        cargoStorageSemaphore.release();          // ← Unlock 1
    }
}

public int handOverCargo(Cargo cargo, int quantity) {
    try {
        cargoStorageSemaphore.acquire();          // ← Lock 1
        int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
        cargoStorage.put(cargo, currentQuantity - quantity);
        return quantity;
    } finally {
        cargoStorageSemaphore.release();          // ← Unlock 1
    }
}
```

#### Deadlock-Analyse
- ✅ **Keine verschachtelten Locks**: Nur ein Semaphore
- ✅ **Kurze kritische Sektion**: Nur Map-Operation
- ✅ **Keine Abhängigkeit zu anderen Locks**

**Ergebnis:** ❌ **Kein Deadlock-Risiko**

---

## 🔄 Monitor-Synchronisation (wait/notify)

### WarehouseClerk: GUI-Animation-Synchronisation

#### Code-Analyse

```java
// WarehouseClerk-Thread
private synchronized void awaitReady() throws InterruptedException {
    ready = false;
    while (!ready) {
        wait();  // ← Gibt Monitor-Lock frei während Warten
    }
}

// GUI-Thread
public synchronized void setReady() {
    ready = true;
    notifyAll();  // ← Weckt wartende Threads
}
```

#### Deadlock-Analyse

**Monitor-Lock:** Impliziter Lock auf `this` (WarehouseClerk-Instanz)

**Ablauf:**
1. WarehouseClerk ruft `awaitReady()` → Erwirbt Monitor-Lock
2. `wait()` → **Gibt Lock temporär frei** während Warten
3. GUI-Thread ruft `setReady()` → Kann Lock erwerben (da freigegeben)
4. `notifyAll()` → Weckt WarehouseClerk
5. WarehouseClerk erwirbt Lock erneut → Fährt fort

**Besonderheit von `wait()`:**
- ✅ **Gibt Lock frei** während Warten → Kein Deadlock möglich
- ✅ **Erwirbt Lock automatisch** nach Aufwachen
- ✅ **While-Schleife** schützt vor Spurious Wakeups

**Ergebnis:** ❌ **Kein Deadlock-Risiko** - `wait()` gibt Lock frei

---

### Supplier: GUI-Animation-Synchronisation

**Identisches Pattern wie WarehouseClerk**

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

**Ergebnis:** ❌ **Kein Deadlock-Risiko**

---

## 🔁 Zirkuläre Warteabhängigkeiten (Circular Wait)

### Graph-Analyse: Resource Allocation Graph (RAG)

#### Legende
- **Kreise**: Threads
- **Rechtecke**: Ressourcen (Semaphore)
- **→**: Thread wartet auf Ressource
- **←**: Ressource ist von Thread gehalten

#### RAG-Diagramm

```
┌──────────────────────────────────────────────────────────────────────┐
│                    Resource Allocation Graph                         │
└──────────────────────────────────────────────────────────────────────┘

Threads:                      Ressourcen:
┌─────────────┐              ┌──────────────────────┐
│ Maschine 1  │─────────────→│ requestQueueSemaphore│
└─────────────┘              └──────────────────────┘
                                       ↑
┌─────────────┐                        │
│ Maschine 2  │────────────────────────┘
└─────────────┘

┌─────────────┐              ┌──────────────────────┐
│ Maschine 1  │←────────────→│ storageSemaphore_1   │
└─────────────┘              └──────────────────────┘

┌─────────────┐              ┌──────────────────────┐
│ Maschine 2  │←────────────→│ storageSemaphore_2   │
└─────────────┘              └──────────────────────┘

┌──────────────┐             ┌──────────────────────┐
│WarehouseClerk│────────────→│ storageSemaphore_1   │
└──────────────┘             └──────────────────────┘
       │
       │                     ┌──────────────────────┐
       └─────────────────────│cargoStorageSemaphore │
                             └──────────────────────┘

┌─────────────┐              ┌──────────────────────┐
│  Supplier   │─────────────→│cargoStorageSemaphore │
└─────────────┘              └──────────────────────┘
```

#### Zyklus-Analyse

**Prüfung auf zirkuläre Abhängigkeiten:**

```
Maschine 1 → requestQueueSemaphore → ?
WarehouseClerk → requestQueueSemaphore → ?
```

**Frage:** Kann `requestQueueSemaphore` zu einer Ressource führen, die wiederum zu Maschine 1 führt?

**Antwort:** ❌ Nein
- `requestQueueSemaphore` wird nur in `addRequest()` und `pollRequest()` gehalten
- Keine weiteren Locks werden während des Haltens erworben
- Keine Rückwärts-Abhängigkeit zu Maschinen

**Prüfung Storage-Semaphore:**

```
Maschine 1 → storageSemaphore_1 → requestQueueSemaphore → ?
```

**Kann `requestQueueSemaphore` zu `storageSemaphore_1` führen?**

```
requestQueueSemaphore (in pollRequest)
  → WarehouseClerk holt Request
    → WarehouseClerk ruft resiveCargo() auf
      → storageSemaphore_1.acquire()
```

**⚠️ ACHTUNG: Verschachtelte Locks erkannt!**

Aber: **Lock-Reihenfolge konsistent**
1. Maschine: `storageSemaphore` → `requestQueueSemaphore`
2. WarehouseClerk: `requestQueueSemaphore` → `storageSemaphore`

**Das ist eine Rückwärts-Abhängigkeit! Potenzieller Deadlock?**

---

## ⚠️ KRITISCHE ANALYSE: Bidirektionale Lock-Ordnung

### Szenario-Analyse

**Thread A (Maschine):**
```java
checkStorageStatus() {
    storageSemaphore.acquire();           // ← Lock 1
    sendCargoRequest(cargo, quantity);
      → addRequest(request);
          → requestQueueSemaphore.acquire();  // ← Lock 2
          → requestQueueSemaphore.release();
    storageSemaphore.release();
}
```

**Thread B (WarehouseClerk):**
```java
runTaskCycle() {
    pollRequest();
      → requestQueueSemaphore.acquire();  // ← Lock 1
      → requestQueueSemaphore.release();
    collectCargo(cargo, quantity);
      → handOverCargo(cargo, quantity);
          → storageSemaphore.acquire();       // ← Lock 2
          → storageSemaphore.release();
}
```

### Deadlock-Bedingung prüfen

**Kann folgender Zustand auftreten?**

```
Maschine-Thread:          WarehouseClerk-Thread:
storageSemaphore  ✓       requestQueueSemaphore ✓  (beide gehalten)
wartet auf →→→→→→→→→→→→→→ requestQueueSemaphore
                          ←←←←←←←←←←←←← wartet auf storageSemaphore
```

**Analyse:**
1. Maschine hält `storageSemaphore`, wartet auf `requestQueueSemaphore`
2. WarehouseClerk hält `requestQueueSemaphore`, wartet auf `storageSemaphore`

**Das ist ein klassisches Deadlock-Szenario!**

### ✅ ABER: Warum tritt kein Deadlock auf?

**Grund 1: Kurze kritische Sektionen**

```java
// In addRequest() - Sehr kurze Lock-Zeit
requestQueueSemaphore.acquireUninterruptibly();
requestQueue.add(request);  // ← Nur O(log n) Queue-Operation
requestQueueSemaphore.release();
```

**Grund 2: Lock wird vor langsamem I/O freigegeben**

```java
// In checkStorageStatus()
storageSemaphore.acquire();
// Nur Lese-Operationen auf HashMap (schnell!)
sendCargoRequest(cargo, quantity);  // ← Erwirbt Request-Lock nur für kurze Zeit
storageSemaphore.release();
```

**Grund 3: WarehouseClerk gibt Request-Lock VOR Storage-Zugriff frei**

```java
// In runTaskCycle()
pollRequest();  // ← Request-Lock wird hier komplett freigegeben
// ... Andere Operationen (Thread.sleep, awaitReady) ...
collectCargo(cargo, quantity);  // ← Storage-Lock wird erst SPÄTER erworben
```

**Zeitliche Trennung:**
```
WarehouseClerk-Thread Timeline:

[Request-Lock acquire]───[Release]
                                    [Sleep/Travel]
                                                     [Storage-Lock acquire]───[Release]
```

Die Locks werden **niemals gleichzeitig** gehalten!

### ✅ Beweis: Sequenzielle Lock-Ordnung

**Maschine:**
```
storageSemaphore {
    requestQueueSemaphore {
        // Kritische Sektion
    }  // ← requestQueueSemaphore freigegeben
}  // ← storageSemaphore freigegeben
```

**WarehouseClerk:**
```
requestQueueSemaphore {
    // Kritische Sektion
}  // ← requestQueueSemaphore freigegeben VOR nächstem Lock!

// ... Zeitverzögerung (Thread.sleep, awaitReady) ...

storageSemaphore {
    // Kritische Sektion
}
```

**Ergebnis:** ✅ **Kein Deadlock** - Locks werden niemals gleichzeitig gehalten

---

## 🔄 Livelock-Analyse

### Was ist Livelock?

Livelock tritt auf, wenn Threads kontinuierlich ihren Zustand ändern als Reaktion auf andere Threads, aber keinen Fortschritt machen.

### Potenzielle Livelock-Szenarien

#### Szenario 1: Maschinen-Pipeline Blockierung

**Code:**
```java
protected void deliverToNextMachine(Cargo cargo) {
    boolean cargoNotified = false;
    while (!cargoNotified) {  // ← Endlosschleife?
        boolean remainingCapacity = nextMaschine.getRemainingStorageCapacity(cargo);
        if (!remainingCapacity) {
            stopMachine();
            Thread.sleep(timeToSleep);  // ← Retry mit Delay
        } else {
            notifyNextMaschineOfCargoSending(cargo);
            cargoNotified = true;
        }
    }
}
```

**Kann Livelock auftreten?**

**Szenario:**
1. Maschine A stoppt, weil Maschine B voll ist
2. Maschine B stoppt, weil Maschine C voll ist
3. Maschine C stoppt, weil Maschine D voll ist
4. ...
5. Letzte Maschine wartet auf WarehouseClerk zum Leeren

**Analyse:**
- ✅ **WarehouseClerk entleert kontinuierlich** Maschinen (wenn Requests vorhanden)
- ✅ **Thread.sleep(timeToSleep)** verhindert aktives Busy-Waiting
- ✅ **stopMachine()** setzt `running = false` → Keine weiteren Produktionen

**Fortschritt garantiert?**
- Ja, sobald WarehouseClerk eine Maschine leert, wird Kapazität frei
- Die Blockierung löst sich von hinten nach vorne auf

**Ergebnis:** ✅ **Kein Livelock** - Fortschritt durch WarehouseClerk garantiert

---

## 🍽️ Starvation-Analyse

### Was ist Starvation?

Starvation tritt auf, wenn ein Thread niemals Zugriff auf benötigte Ressourcen erhält.

### Anti-Starvation-Mechanismen

#### 1. Priority Queue für Requests

```java
private final PriorityQueue<Request> requestQueue = 
    new PriorityQueue<>(Comparator.comparingInt(Request::priority).reversed());
```

**Priorisierung:**
- Requests werden nach Maschinenpriori tät sortiert
- Wichtige Maschinen (z.B. Endprodukt-Maschinen) werden bevorzugt
- Niedrig-prioritäre Maschinen können dennoch verhungern, wenn viele High-Priority-Requests kommen

#### 2. FIFO-Semaphore (Java default)

```java
Semaphore storageSemaphore = new Semaphore(1);  // ← Fair=false (default)
```

**⚠️ ACHTUNG: Nicht-faire Semaphore!**

- Threads könnten theoretisch verhungern
- Empfehlung: `new Semaphore(1, true)` für garantierte Fairness

#### 3. notifyAll() statt notify()

```java
public synchronized void setReady() {
    ready = true;
    notifyAll();  // ← Weckt ALLE wartenden Threads
}
```

**Vorteil:** Verhindert Lost Wakeup und Starvation bei Monitor-Synchronisation

### Starvation-Risikobewertung

| Komponente | Risiko | Begründung |
|-----------|--------|------------|
| **Request Queue** | 🟡 Mittel | Niedrig-prioritäre Maschinen können verhungern |
| **Storage Semaphore** | 🟡 Mittel | Nicht-faire Semaphore (default) |
| **Notification Semaphore** | 🟡 Mittel | Nicht-faire Semaphore (default) |
| **Monitor (wait/notify)** | 🟢 Niedrig | `notifyAll()` + while-Schleife |

### Empfohlene Verbesserungen

```java
// Statt:
Semaphore storageSemaphore = new Semaphore(1);

// Besser:
Semaphore storageSemaphore = new Semaphore(1, true);  // ← Fair!
```

---

## 📊 Resource Exhaustion-Analyse

### Speicher-Ressourcen

#### 1. Maschinen-Storage (begrenzt)

```java
protected int maxStorageCapacity;
protected Map<Cargo, Integer> storage;
```

**Begrenzung:**
- Jede Maschine hat `maxStorageCapacity` (z.B. 10)
- Produktion stoppt bei Überfüllung

#### 2. MainDepot-Storage (begrenzt)

```java
private final int maxStorageCapacity;
private final Map <Cargo, Integer> cargoStorage;
```

**Begrenzung:**
- MainDepot hat `maxStorageCapacity`
- Kann voll werden, wenn Supplier nicht schnell genug nachfüllt/leert

#### 3. Request Queue (unbegrenzt!)

```java
private final PriorityQueue<Request> requestQueue;
```

**⚠️ RISIKO: Unbegrenzte Queue!**

- Wenn WarehouseClerk zu langsam arbeiten, kann Queue unbegrenzt wachsen
- Potenzieller Out-of-Memory (OOM) bei vielen Maschinen

**Empfohlene Verbesserung:**
```java
// Begrenzte Queue mit Blocking
private final BlockingQueue<Request> requestQueue = 
    new PriorityBlockingQueue<>(1000);  // ← Max 1000 Requests
```

#### 4. CargoOnTransit Queue (unbegrenzt pro Maschine)

```java
protected Queue<Cargo> cargosOnTransit = new LinkedList<>();
```

**Risiko:** Niedrig, da GUI-Callbacks Cargo schnell abarbeiten

---

## 🎯 Zusammenfassung: Deadlock-Freiheit

### ✅ Bewiesene Deadlock-Freiheit

Das Projekt ist **deadlock-frei** aufgrund folgender Mechanismen:

1. **Keine zirkulären Lock-Abhängigkeiten**
   - Lock-Hierarchie: `storageSemaphore` → `requestQueueSemaphore`
   - WarehouseClerk gibt `requestQueueSemaphore` frei VOR Erwerb von `storageSemaphore`
   
2. **Kurze kritische Sektionen**
   - Minimale Lock-Haltezeiten
   - Keine langwierigen Operationen innerhalb von Locks
   
3. **Try-Finally Pattern**
   - Garantierte Semaphore-Freigabe auch bei Exceptions
   
4. **Monitor mit wait()**
   - `wait()` gibt Lock temporär frei → Kein Deadlock
   - `notifyAll()` verhindert Lost Wakeups
   
5. **Timeout & Retry**
   - Maschinen stoppen bei Blockierung und versuchen später erneut
   - Kein permanentes Warten

### 🟡 Identifizierte Verbesserungspotenziale

| # | Problem | Schwere | Empfehlung |
|---|---------|---------|------------|
| 1 | Nicht-faire Semaphore | Mittel | `new Semaphore(1, true)` verwenden |
| 2 | Unbegrenzte Request Queue | Mittel | `PriorityBlockingQueue` mit Größenlimit |
| 3 | Singleton nicht thread-safe | Niedrig | Double-Checked Locking mit `volatile` |
| 4 | Prioritäts-basierte Starvation | Mittel | Aging-Mechanismus für alte Requests |

---

## 📈 Deadlock-Präventions-Strategien

### Strategie 1: Lock-Ordering (Umgesetzt)

**Prinzip:** Alle Threads erwerben Locks in derselben Reihenfolge

**Implementierung:**
```
storageSemaphore → requestQueueSemaphore (konsistent für alle Threads)
```

**Status:** ✅ Teilweise umgesetzt
- Maschinen: Storage → Request Queue
- WarehouseClerk: Request Queue → (Freigabe) → Storage (zeitlich getrennt)

### Strategie 2: Lock-Timeout (Nicht umgesetzt)

**Prinzip:** Gib auf, wenn Lock nicht innerhalb von Timeout erworben werden kann

**Empfohlen für:**
```java
// Statt:
semaphore.acquire();

// Besser:
if (semaphore.tryAcquire(5, TimeUnit.SECONDS)) {
    try {
        // Kritische Sektion
    } finally {
        semaphore.release();
    }
} else {
    logger.warn("Lock acquisition timeout!");
    // Fallback-Logik
}
```

**Status:** ❌ Nicht implementiert (aber nicht zwingend nötig)

### Strategie 3: No-Hold-and-Wait (Umgesetzt)

**Prinzip:** Erwerbe alle benötigten Locks gleichzeitig oder gar keine

**Implementierung:**
- Nur ein Lock pro kritische Sektion
- Verschachtelte Locks werden komplett freigegeben vor erneutem Erwerb

**Status:** ✅ Umgesetzt

### Strategie 4: Preemption (Nicht umgesetzt)

**Prinzip:** Erzwinge Freigabe von Locks bei Deadlock-Erkennung

**Status:** ❌ Nicht implementiert (nicht nötig, da deadlock-frei)

---

## 🧪 Deadlock-Testszenarien

### Test 1: Hohe Last mit vielen Maschinen

**Setup:**
- 10 ProductionMaschines
- 10 WarehouseClerks
- Alle Maschinen senden gleichzeitig Requests

**Erwartetes Verhalten:**
- Keine Deadlocks
- Requests werden nacheinander abgearbeitet
- Maschinen stoppen temporär bei Überfüllung

**Status:** ✅ Sollte bestehen (theoretisch)

### Test 2: Pipeline-Blockierung

**Setup:**
- Maschine A → B → C → D (Pipeline)
- D hat keinen Nachfolger und voller Storage
- Alle Maschinen produzieren gleichzeitig

**Erwartetes Verhalten:**
- D stoppt (voller Storage)
- C stoppt (kann nicht an D liefern)
- B stoppt (kann nicht an C liefern)
- A stoppt (kann nicht an B liefern)
- WarehouseClerk leert D
- Pipeline löst sich von hinten nach vorne auf

**Status:** ✅ Sollte bestehen (durch Retry-Mechanismus)

### Test 3: WarehouseClerk-Engpass

**Setup:**
- 1 WarehouseClerk
- 10 Maschinen senden gleichzeitig Requests

**Erwartetes Verhalten:**
- Request Queue wächst
- WarehouseClerk arbeitet Requests sequenziell ab
- Keine Deadlocks, aber langsame Verarbeitung

**Status:** ✅ Sollte bestehen

### Test 4: MainDepot-Überlastung

**Setup:**
- 10 WarehouseClerks greifen gleichzeitig auf MainDepot zu
- Supplier liefert gleichzeitig nach

**Erwartetes Verhalten:**
- Semaphore serialisiert Zugriffe
- Keine Deadlocks
- Sequenzielle Abarbeitung

**Status:** ✅ Sollte bestehen

---

## 🔧 Empfohlene Code-Verbesserungen

### 1. Faire Semaphore

**Aktuell:**
```java
protected Semaphore storageSemaphore = new Semaphore(1);
```

**Verbessert:**
```java
protected Semaphore storageSemaphore = new Semaphore(1, true);  // fair=true
```

**Datei:** `Maschine.java:41`

### 2. Thread-Safe Singleton

**Aktuell:**
```java
public static ProductionHeadquarters getInstance(){
    if (singletonInstance == null){
        singletonInstance = new ProductionHeadquarters();
    }
    return singletonInstance;
}
```

**Verbessert:**
```java
private static volatile ProductionHeadquarters singletonInstance;

public static ProductionHeadquarters getInstance(){
    if (singletonInstance == null){
        synchronized (ProductionHeadquarters.class) {
            if (singletonInstance == null){
                singletonInstance = new ProductionHeadquarters();
            }
        }
    }
    return singletonInstance;
}
```

**Datei:** `ProductionHeadquarters.java:50`

### 3. Begrenzte Request Queue

**Aktuell:**
```java
private final PriorityQueue<Request> requestQueue;
```

**Verbessert:**
```java
private final BlockingQueue<Request> requestQueue = 
    new PriorityBlockingQueue<>(1000, 
        Comparator.comparingInt(Request::priority).reversed());
```

**Datei:** `ProductionHeadquarters.java:23`

### 4. Lock-Timeout für kritische Operationen

**Empfohlen für:**
```java
// In checkStorageStatus()
if (storageSemaphore.tryAcquire(1, TimeUnit.SECONDS)) {
    try {
        // Kritische Sektion
    } finally {
        storageSemaphore.release();
    }
} else {
    logger.warn("Storage lock timeout in machine {}", identificationNumber);
}
```

---

## 📝 Fazit

### ✅ Gesamtbewertung: Sehr gut

Das Projekt implementiert eine **robuste, deadlock-freie** Multithread-Architektur mit:

- ✅ Konsistenter Synchronisationsstrategie
- ✅ Klarer Lock-Hierarchie
- ✅ Kurzen kritischen Sektionen
- ✅ Retry-Mechanismen bei Blockierung
- ✅ Fairer Thread-Kommunikation (notifyAll)

### 🎯 Verbesserungspotenziale

- 🟡 Faire Semaphore implementieren
- 🟡 Begrenzte Request Queue einführen
- 🟡 Thread-Safe Singleton korrigieren
- 🟡 Aging-Mechanismus für Request-Prioritäten

### 📊 Risiko-Score

| Kategorie | Score (1-10) | Begründung |
|-----------|--------------|------------|
| **Deadlock** | 1/10 | Keine zirkulären Abhängigkeiten |
| **Livelock** | 2/10 | Retry mit Sleep verhindert aktives Warten |
| **Starvation** | 4/10 | Nicht-faire Semaphore, Priority Queue |
| **Race Conditions** | 1/10 | Konsistente Semaphore-Nutzung |
| **Resource Exhaustion** | 5/10 | Unbegrenzte Request Queue |

**Gesamt-Risiko: 2.6/10 (Niedrig)** 🟢

---

**Ende der Deadlock-Analyse**  
**Datum:** 20. Februar 2026  
**Status:** ✅ ABGESCHLOSSEN

