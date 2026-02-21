# 🔒 Analyse: Threads mit mehrfachen kritischen Ressourcen

**Datum:** 21. Februar 2026  
**Zweck:** Identifikation aller Threads und Methoden, die mehr als eine kritische Ressource gleichzeitig halten können

---

## 📋 Zusammenfassung

Das System hat **einen kritischen Fall** und **einen unbedenklichen Fall** von mehrfachem Lock-Halten:

### ⚠️ KRITISCH: 
1. **Maschine-Threads** in `checkStorageStatus()` - Halten `storageSemaphore` + `requestQueueSemaphore` gleichzeitig

### ✅ UNBEDENKLICH:
1. **Maschine-Threads** in `getRemainingStorageCapacity()` - Halten Locks **sequenziell** (niemals gleichzeitig)

---

## 🔍 Detaillierte Analyse

### 1️⃣ Maschine-Thread: `checkStorageStatus()` ⚠️

**Thread-Typ:** `Maschine extends Thread` (pro Maschine M1-M9)  
**Methode:** `ProductionMaschine.checkStorageStatus()`  
**Datei:** `src/main/java/org/betriebssysteme/model/stations/ProductionMaschine.java:48`

#### Gehaltene Ressourcen gleichzeitig:

```java
@Override
protected void checkStorageStatus() {
    try {
        storageSemaphore.acquire();              // ← LOCK 1: storageSemaphore
        for (Cargo cargo : recipe.ingredients().keySet()) {
            int storedQuantity = storage.getOrDefault(cargo, 0);
            if (storedQuantity == 0) {
                if (cargo.getCargoTyp() == CargoTyp.MATERIAL){
                    sendCargoRequest(cargo, maxStorageCapacity);  // ← Ruft auf:
                        // → ProductionHeadquarters.addRequest()
                        //     → requestQueueSemaphore.acquire()  // ← LOCK 2!
                }
            }
        }
    } finally {
        storageSemaphore.release();
    }
}
```

#### Lock-Reihenfolge:
1. **storageSemaphore** wird erworben (`storageSemaphore.acquire()`)
2. Während storageSemaphore gehalten wird:
   - `sendCargoRequest()` aufgerufen
   - Diese Methode ruft `ProductionHeadquarters.addRequest()` auf
   - **requestQueueSemaphore** wird erworben (`requestQueueSemaphore.acquireUninterruptibly()`)
3. requestQueueSemaphore wird freigegeben (sehr kurz, nur Queue-Operation)
4. storageSemaphore wird freigegeben

#### Risiko-Bewertung:
- **Verschachtelungstiefe:** 2 Locks gleichzeitig
- **Hold-Zeit:** Sehr kurz (~1ms für Queue-Operation)
- **Deadlock-Risiko:** ⚠️ MITTEL (siehe Deadlock-Analyse-Dokument)
- **Grund für Unbedenklichkeit:** Zeitliche Trennung auf WarehouseClerk-Seite verhindert Deadlock

---

### 2️⃣ Maschine-Thread: `getRemainingStorageCapacity()` ✅

**Thread-Typ:** `Maschine extends Thread` (pro Maschine M1-M9)  
**Methode:** `Maschine.getRemainingStorageCapacity()`  
**Datei:** `src/main/java/org/betriebssysteme/model/stations/Maschine.java:323`

#### Gehaltene Ressourcen SEQUENZIELL (nicht gleichzeitig):

```java
public boolean getRemainingStorageCapacity(Cargo cargo){
    int remainingCapacity;
    try {
        storageSemaphore.acquire();              // ← LOCK 1
        int currentQuantity = storage.getOrDefault(cargo, 0);
        remainingCapacity = maxStorageCapacity - currentQuantity;
    } finally {
        storageSemaphore.release();              // ← UNLOCK 1 ✓
    }
    // ← LOCK 1 ist FREIGEGEBEN!
    try {
        notificationSemaphore.acquire();         // ← LOCK 2 (NACH Freigabe von Lock 1!)
        for (Cargo c : cargosOnTransit){
            if (c.equals(cargo)){
                remainingCapacity -= 1;
            }
        }
    } finally {
        notificationSemaphore.release();         // ← UNLOCK 2 ✓
    }
    return remainingCapacity > 0;
}
```

#### Lock-Reihenfolge:
1. **storageSemaphore** wird erworben
2. **storageSemaphore** wird freigegeben ✓
3. **notificationSemaphore** wird erworben (NACH Freigabe von Lock 1!)
4. **notificationSemaphore** wird freigegeben ✓

#### Risiko-Bewertung:
- **Verschachtelungstiefe:** 0 (nur 1 Lock zur Zeit!)
- **Deadlock-Risiko:** ❌ KEIN RISIKO
- **Grund:** Sequenzielle Lock-Verwaltung - Locks überschneiden sich niemals

---

## 🚫 Nicht-Problematische Fälle (Nur EIN Lock)

Die folgenden Methoden halten jeweils nur EINE kritische Ressource zur Zeit:

### WarehouseClerk-Thread:
- `awaitReady()` - Nur Monitor-Lock auf `this` (WarehouseClerk-Instanz)
- `setReady()` - Nur Monitor-Lock auf `this`
- Indirekte Semaphore-Zugriffe über `collectCargo()` / `refillCargo()`:
  - Rufen `MainDepot.handOverCargo()` / `Maschine.resiveCargo()` auf
  - Diese halten nur **ein** Semaphore (cargoStorageSemaphore bzw. storageSemaphore)

### Supplier-Thread:
- `awaitReady()` - Nur Monitor-Lock auf `this` (Supplier-Instanz)
- `setReady()` - Nur Monitor-Lock auf `this`
- Indirekte Semaphore-Zugriffe wie WarehouseClerk

### Maschine-Threads (weitere Methoden):
- `produceProduct()` - Nur storageSemaphore
- `resiveCargo()` - Nur storageSemaphore
- `handOverCargo()` - Nur storageSemaphore
- `addCargoTransitNotification()` - Nur notificationSemaphore
- `notifyMachineCargoHandoverCompleted()` - Nur notificationSemaphore

### MainDepot (kein Thread, aber Station):
- `resiveCargo()` - Nur cargoStorageSemaphore
- `handOverCargo()` - Nur cargoStorageSemaphore

### ProductionHeadquarters (kein Thread, Singleton):
- `addRequest()` - Nur requestQueueSemaphore
- `pollRequest()` - Nur requestQueueSemaphore

---

## 📊 Übersichtstabelle

| Thread-Typ | Anzahl Instanzen | Methode | Lock 1 | Lock 2 | Gleichzeitig? | Risiko |
|------------|------------------|---------|--------|--------|---------------|--------|
| **Maschine** | 9 (M1-M9) | `checkStorageStatus()` | storageSemaphore | requestQueueSemaphore | ✅ JA | ⚠️ MITTEL |
| **Maschine** | 9 (M1-M9) | `getRemainingStorageCapacity()` | storageSemaphore | notificationSemaphore | ❌ NEIN (sequenziell) | ✅ KEIN |
| **WarehouseClerk** | N | `awaitReady()` | Monitor-Lock (this) | - | - | ✅ KEIN |
| **Supplier** | 1 | `awaitReady()` | Monitor-Lock (this) | - | - | ✅ KEIN |

---

## 🎯 Fazit

### Einziger kritischer Fall:
**`ProductionMaschine.checkStorageStatus()`** ist die **einzige Methode** im gesamten System, die mehr als eine kritische Ressource **gleichzeitig** hält:
1. `storageSemaphore` (Maschinen-intern)
2. `requestQueueSemaphore` (ProductionHeadquarters)

### Warum ist das System trotzdem Deadlock-frei?

**Zeitliche Trennung auf WarehouseClerk-Seite:**
```
WarehouseClerk.runTaskCycle():
  1. pollRequest() → requestQueueSemaphore.acquire() → release() ✓
  2. awaitReady() → Wartet auf GUI (KEIN Lock gehalten!)
  3. collectCargo() → storageSemaphore.acquire() → release() ✓
```

**Zwischen** dem Freigeben von `requestQueueSemaphore` und dem Erwerben von `storageSemaphore` liegt:
- `awaitReady()` - Wartet auf GUI-Thread
- Zeitverzögerung von mehreren Sekunden

Diese zeitliche Trennung verhindert bidirektionale Lock-Abhängigkeiten und somit Deadlocks.

### Vollständige Analyse:
Siehe:
- `docs/Deadlock-Analyse.md` - Detaillierte technische Analyse
- `docs/Zyklisches-Warten-Zusammenfassung-Kurz.md` - Mathematischer Beweis
- `docs/Deadlock-Analyse-Visuell.md` - Visuelle Diagramme

---

## 📈 Empfehlungen

### Priorität ⭐⭐ - MITTEL

**Code-Verbesserung: Lock-Granularität reduzieren**

Aktuell:
```java
protected void checkStorageStatus() {
    try {
        storageSemaphore.acquire();  // ← Lock für gesamte Schleife
        for (Cargo cargo : recipe.ingredients().keySet()) {
            if (storedQuantity == 0) {
                sendCargoRequest(cargo, maxStorageCapacity);  // ← Verschachtelt!
            }
        }
    } finally {
        storageSemaphore.release();
    }
}
```

Empfohlen (vermeidet Verschachtelung):
```java
protected void checkStorageStatus() {
    // Schritt 1: Status-Sammlung unter Lock
    Map<Cargo, Integer> snapshot;
    try {
        storageSemaphore.acquire();
        snapshot = new HashMap<>(storage);  // Snapshot erstellen
    } finally {
        storageSemaphore.release();  // ← Lock FREIGEBEN
    }
    
    // Schritt 2: Requests senden OHNE Lock
    for (Cargo cargo : recipe.ingredients().keySet()) {
        int storedQuantity = snapshot.getOrDefault(cargo, 0);
        if (storedQuantity == 0) {
            sendCargoRequest(cargo, maxStorageCapacity);  // ← KEIN Lock mehr!
        }
    }
}
```

**Vorteil:**
- ✅ Keine verschachtelten Locks mehr
- ✅ Explizit garantierte Deadlock-Freiheit
- ✅ Bessere Code-Lesbarkeit

**Nachteil:**
- ⚠️ Race Condition möglich (Storage ändert sich zwischen Snapshot und Request)
- ⚠️ Aber: Unkritisch, da nur Status-Check und Request-Logik betroffen

---

**Dokument erstellt automatisch durch Code-Analyse am 21. Februar 2026**

