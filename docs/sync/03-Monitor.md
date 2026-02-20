# Monitor-Pattern (synchronized + wait/notify)

**Dokumentation:** Synchronisationsmodell  
**Fokus:** GUI-Thread-Koordination mit Monitor-Pattern

---

## Übersicht

Das **Monitor-Pattern** wird für die Synchronisation zwischen Worker-Threads (WarehouseClerk, Supplier) und dem GUI-Thread verwendet.

**Zweck:** Worker-Threads warten auf Bestätigung, dass GUI-Animationen abgeschlossen sind, bevor sie fortfahren.

**Mechanismus:** Java's eingebautes Monitor-Konzept mit `synchronized`, `wait()` und `notifyAll()`

---

## Was ist ein Monitor?

Ein **Monitor** ist ein Synchronisationskonstrukt, das:
- **Mutual Exclusion** (gegenseitiger Ausschluss) garantiert
- **Condition Variables** (Bedingungsvariablen) bereitstellt
- **Automatisches Lock-Management** bietet

### In Java:
```java
synchronized (object) {
    // Nur ein Thread kann diesen Block gleichzeitig ausführen
    // Der Monitor ist das 'object'
}
```

Jedes Java-Objekt kann als Monitor dienen!

---

## 1. WarehouseClerk - Animation-Synchronisation

### Verwendete Methoden

#### awaitReady() - Worker wartet auf GUI

```java
// In WarehouseClerk.java
private synchronized void awaitReady() throws InterruptedException {
    ready = false;
    while (!ready) {
        wait();  // Gibt Monitor frei und wartet
    }
}
```

**Was passiert:**
1. `synchronized` → Erwirbt Monitor auf `this` (WarehouseClerk-Instanz)
2. `ready = false` → Setzt Bedingung
3. `while (!ready)` → Prüft Bedingung (Schutz vor Spurious Wakeups)
4. `wait()` → Gibt Monitor frei UND blockiert Thread

**Wichtig:** 
- Thread gibt den Monitor frei während wait()
- Andere Threads können jetzt `setReady()` aufrufen
- Thread wartet bis `notifyAll()` aufgerufen wird

#### setReady() - GUI weckt Worker

```java
// In WarehouseClerk.java
public synchronized void setReady() {
    ready = true;
    notifyAll();  // Weckt alle wartenden Threads
}
```

**Was passiert:**
1. `synchronized` → Erwirbt Monitor auf `this`
2. `ready = true` → Bedingung erfüllt
3. `notifyAll()` → Weckt alle Threads die auf diesem Monitor warten
4. Verlässt synchronized → Monitor wird freigegeben

**Warum notifyAll() statt notify()?**
- `notify()` weckt nur EINEN wartenden Thread (unvorhersehbar welchen)
- `notifyAll()` weckt ALLE wartenden Threads (sicherer)

---

### Vollständiger Flow

```
WarehouseClerk Thread                GUI Thread (JavaFX)
─────────────────────                ───────────────────

runTaskCycle()
    ↓
status = TRAVEL_TO_STATION
idOfCurrentDestination = stationId
    ↓
awaitReady()                         [Wartet auf Worker]
    ↓                                     ↓
synchronized (this)                  [Worker bewegt sich]
    ↓                                     ↓
ready = false                        [Animation startet]
    ↓                                     ↓
while (!ready)                       [Animation läuft...]
    ↓                                     ↓
wait()                               [Animation läuft...]
    ↓                                     ↓
[Monitor freigegeben]                Animation fertig!
[Thread BLOCKIERT]                        ↓
    ↓                                setReady()
    ↓                                     ↓
    ↓                                synchronized (this)
    ↓                                     ↓
    ↓                                ready = true
    ↓                                     ↓
    ↓                                notifyAll()
    ↓                                     ↓
[wird geweckt] ◀─────────────────────[Monitor freigegeben]
    ↓
[versucht Monitor zu erwerben]
    ↓
[Monitor erworben]
    ↓
while (!ready) → false (ready ist true!)
    ↓
[verlässt while-Schleife]
    ↓
[verlässt synchronized]
    ↓
[verlässt awaitReady()]
    ↓
Fortsetzung der Arbeit...
```

---

### Verwendung im WarehouseClerk-Zyklus

```java
private void runTaskCycle() {
    boolean hasRequest = getRequested();
    if (hasRequest) {
        try {
            // 1. Reise zur Quelle
            idOfCurrentDestinationStation = originStationId;
            status = StatusInfo.TRAVEL_TO_STATION;
            awaitReady();  // ← Monitor: Warte auf GUI-Animation
            
            // 2. Cargo sammeln
            status = StatusInfo.COLLECT_CARGO;
            int quantity = collectCargo(cargo, maxCargoCapacity);
            Thread.sleep(timeForTask_ms);
            
            // 3. Reise zum Ziel
            status = StatusInfo.TRANSPORT_CARGO;
            idOfCurrentDestinationStation = destinationStationId;
            awaitReady();  // ← Monitor: Warte auf GUI-Animation
            
            // 4. Cargo abliefern
            status = StatusInfo.DELIVER_CARGO;
            refillCargo(cargo, quantity);
            Thread.sleep(timeForTask_ms);
            
            // 5. Zurück zur Zentrale
            status = StatusInfo.TRAVEL_TO_HEADQUARTERS;
            idOfCurrentDestinationStation = headquartersId;
            awaitReady();  // ← Monitor: Warte auf GUI-Animation
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

**3 Wartepunkte pro Request-Zyklus!**

---

## 2. Supplier - Animation-Synchronisation

### Verwendete Methoden

#### awaitReady() - Supplier wartet auf GUI

```java
// In Supplier.java
private synchronized void awaitReady() throws InterruptedException {
    ready = false;
    while (!ready) {
        wait();  // Gibt Monitor frei und wartet
    }
}
```

**Identisch zu WarehouseClerk** - gleicher Mechanismus

#### setReady() - GUI weckt Supplier

```java
// In Supplier.java
public synchronized void setReady() {
    ready = true;
    notifyAll();  // Weckt wartenden Thread
}
```

**Identisch zu WarehouseClerk** - gleicher Mechanismus

---

### Vollständiger Flow

```
Supplier Thread                      GUI Thread (JavaFX)
───────────────                      ───────────────────

supplyRoutine()
    ↓
// Cargo initialisieren
for (Material m : Materials) {
    cargoStorage.put(m, quantity)
}
    ↓
// Hinfahrt zum MainDepot
task = DELIVERING
idOfCurrentDestination = mainDepotId
    ↓
awaitReady()                         [Wartet auf Supplier]
    ↓                                     ↓
synchronized (this)                  [Supplier fährt los]
    ↓                                     ↓
ready = false                        [Animation startet]
    ↓                                     ↓
while (!ready)                       [Fährt zu MainDepot...]
    ↓                                     ↓
wait()                               [Animation läuft...]
    ↓                                     ↓
[Monitor freigegeben]                Animation fertig!
[Thread BLOCKIERT]                        ↓
    ↓                                setReady()
    ↓                                     ↓
    ↓                                synchronized (this)
    ↓                                     ↓
    ↓                                ready = true
    ↓                                     ↓
    ↓                                notifyAll()
    ↓                                     ↓
[wird geweckt] ◀─────────────────────[Monitor freigegeben]
    ↓
[Monitor erworben]
    ↓
while (!ready) → false
    ↓
[verlässt awaitReady()]
    ↓
// Am MainDepot angekommen
refillDepotAndCollectCargo()
    ↓
Thread.sleep(supplyTimer_ms)
    ↓
// Materialien liefern
for (Material m : Materials) {
    refillCargo(m, quantity)
}
    ↓
// Produkte abholen
collectCargo(Product.PACKAGE, freeCapacity)
collectCargo(Product.SCRAP, freeCapacity)
    ↓
// Rückfahrt
task = TRANSPORTING
idOfCurrentDestination = -1 (außerhalb)
    ↓
awaitReady()                         [Gleicher Ablauf]
    ↓                                     ↓
[wait() → notifyAll()]              [Animation → setReady()]
    ↓
// Zurück, warte bis nächster Zyklus
Thread.sleep(supplyInterval_ms)
```

**2 Wartepunkte pro Supply-Zyklus:** Hinfahrt + Rückfahrt

---

## 3. Monitor-Pattern Komponenten

### 3.1 Monitor-Objekt

```java
public class WarehouseClerk extends Thread {
    // Das WarehouseClerk-Objekt SELBST ist der Monitor
    // 'this' wird als Monitor verwendet
}
```

### 3.2 Bedingungsvariable

```java
private boolean ready = false;
```

**Zweck:** Gibt an, ob die Bedingung erfüllt ist (Animation fertig)

### 3.3 Mutual Exclusion

```java
private synchronized void awaitReady() { ... }
public synchronized void setReady() { ... }
```

**Garantiert:** Nur ein Thread kann diese Methoden gleichzeitig ausführen

### 3.4 Condition Wait

```java
while (!ready) {
    wait();  // Wartet auf Bedingung
}
```

**Wichtig:** `while`-Schleife statt `if` → Schutz vor Spurious Wakeups

### 3.5 Condition Signal

```java
notifyAll();  // Signalisiert: Bedingung erfüllt
```

**Weckt:** Alle wartenden Threads auf diesem Monitor

---

## 4. Monitor vs. Semaphore

### Wann Monitor-Pattern?

✅ **Verwende Monitor (synchronized + wait/notify) wenn:**
- Event-basierte Synchronisation nötig
- "Warte auf Bedingung" Szenarien
- GUI-Koordination (Animation fertig)
- Single Producer - Single/Multiple Consumer
- Bedingung muss geprüft werden

**Beispiel:** WarehouseClerk wartet auf "Animation fertig"

### Wann Semaphore?

✅ **Verwende Semaphore wenn:**
- Ressourcen-Schutz (kritische Abschnitte)
- Multiple Producer - Multiple Consumer
- Zählen von Ressourcen
- Keine komplexe Bedingung

**Beispiel:** Schutz von Storage-Map in Maschinen

---

## 5. Best Practices

### ✅ 1. While-Schleife (NICHT if)

```java
// ✅ KORREKT
while (!ready) {
    wait();
}

// ❌ FALSCH
if (!ready) {
    wait();
}
```

**Grund:** Schutz vor **Spurious Wakeups** (spontanes Aufwachen ohne notify)

**Was sind Spurious Wakeups?**
- Thread kann spontan aus wait() aufwachen
- Ohne dass notify()/notifyAll() aufgerufen wurde
- While-Schleife prüft Bedingung erneut
- If würde weiterlaufen auch wenn Bedingung nicht erfüllt

---

### ✅ 2. Synchronized auf selber Instanz

```java
// Beide Methoden müssen synchronized auf 'this' sein
private synchronized void awaitReady() { ... }  // Monitor: this
public synchronized void setReady() { ... }     // Monitor: this
```

**Grund:** `wait()` und `notifyAll()` funktionieren nur auf dem Monitor-Objekt

**Was passiert bei Fehler:**
```java
// ❌ FALSCH
private void awaitReady() {
    synchronized (someOtherObject) {
        wait();  // IllegalMonitorStateException!
    }
}
```

---

### ✅ 3. notifyAll() statt notify()

```java
// ✅ KORREKT
public synchronized void setReady() {
    ready = true;
    notifyAll();  // Weckt alle
}

// 🟡 FUNKTIONIERT, aber unsicherer
public synchronized void setReady() {
    ready = true;
    notify();  // Weckt nur einen (welchen?)
}
```

**Grund:** 
- `notify()` weckt nur EINEN wartenden Thread (JVM entscheidet welchen)
- `notifyAll()` weckt ALLE wartenden Threads (sicherer)
- Bei nur einem wartenden Thread funktioniert beides
- Bei mehreren: notifyAll() ist sicherer

---

## 6. Vergleich: Monitor vs. Maschine-GUI-Sync

| Aspekt | Monitor (WarehouseClerk/Supplier) | Polling (Maschine) |
|--------|-----------------------------------|-------------------|
| **Pattern** | synchronized + wait/notify | Flag + Polling + Callback |
| **Blockierung** | ✅ Ja (wait) | ❌ Nein |
| **Initiator** | GUI (setReady) | Maschine (Flag setzen) |
| **Frequenz** | Event-basiert (1x) | Polling (60 FPS) |
| **Zweck** | Bewegungs-Animation | Cargo-Übergabe-Animation |
| **Synchronisation** | Monitor (this) | Semaphore |
| **Thread-Safety** | synchronized | Semaphore |

---

## Zusammenfassung

### Monitor-Pattern in diesem Projekt:

1. **WarehouseClerk:** 3 Wartepunkte pro Request (3 Reisen)
2. **Supplier:** 2 Wartepunkte pro Zyklus (Hin + Zurück)
3. **Mechanismus:** synchronized + wait + notifyAll
4. **Monitor-Objekt:** `this` (WarehouseClerk/Supplier-Instanz)
5. **Bedingung:** `ready` (boolean)
6. **Initiator:** GUI-Thread ruft `setReady()`
7. **Zweck:** Synchronisation mit Bewegungs-Animationen

### Kern-Konzepte:

- **Monitor** = Objekt mit Lock + Condition Variables
- **wait()** = Gib Lock frei und warte
- **notifyAll()** = Wecke alle wartenden Threads
- **synchronized** = Erwirb Monitor-Lock automatisch
- **while-Schleife** = Schutz vor Spurious Wakeups

### Pattern-Name:

**"Monitor Pattern"** oder **"Wait/Notify Pattern"** oder **"Condition Synchronization Pattern"**

---

**Nächstes Dokument:** [03a-Maschine-GUI-Sync.md](03a-Maschine-GUI-Sync.md) - Vergleich mit Polling-Pattern

