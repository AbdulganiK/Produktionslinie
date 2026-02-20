# Semaphore-Synchronisation

**Dokumentation:** Synchronisationsmodell  
**Fokus:** Semaphore-basierte Synchronisation

---

## Übersicht

Das System verwendet **java.util.concurrent.Semaphore** mit **1 Permit** (binäre Semaphore) als Mutex-Ersatz für alle kritischen Ressourcen.

**Standard-Pattern:**
```java
semaphore.acquire();
try {
    // Kritischer Abschnitt
} catch (InterruptedException e) {
    throw new RuntimeException(e);
} finally {
    semaphore.release();  // Garantiert auch bei Exception
}
```

---

## 1. ProductionHeadquarters - Request Queue

### Kritische Ressource
```java
private final PriorityQueue<Request> requestQueue;
private final Semaphore requestQueueSemaphore = new Semaphore(1);
```

### Geschützte Operationen

#### addRequest() - Thread-safe Hinzufügen
```java
public void addRequest(Request request){
    requestQueueSemaphore.acquireUninterruptibly();
    requestQueue.add(request);
    requestQueueSemaphore.release();
}
```

**Aufrufer:** Alle Maschinen-Threads (bei Ressourcenmangel)

#### pollRequest() - Thread-safe Abrufen
```java
public Request pollRequest(){
    Request request;
    requestQueueSemaphore.acquireUninterruptibly();
    request = requestQueue.poll();
    requestQueueSemaphore.release();
    return request;
}
```

**Aufrufer:** Alle WarehouseClerk-Threads (Worker-Pool)

#### deleteAllData() - Thread-safe Löschen
```java
public void deleteAllData() {
    stations.clear();
    personnel.clear();
    try {
        requestQueueSemaphore.acquire();
        requestQueue.clear();
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        requestQueueSemaphore.release();
    }
}
```

### Zweck
Verhindert **Race Conditions** bei gleichzeitigem Zugriff mehrerer Threads auf die zentrale Anfrage-Queue.

### Szenarien
- **Maschine A** sendet Request → acquire → add → release
- **Maschine B** sendet Request → wartet → acquire → add → release
- **WarehouseClerk** holt Request → acquire → poll → release

---

## 2. Maschine - Storage Synchronisation

### Kritische Ressource
```java
protected Map<Cargo, Integer> storage;
protected Semaphore storageSemaphore = new Semaphore(1);
```

### Geschützte Operationen

#### storeProduct() - Produkt einlagern
```java
protected void storeProduct(Cargo cargo) {
    try {
        storageSemaphore.acquire();
        if (storage.containsKey(cargo)) {
            int currentQuantity = storage.getOrDefault(cargo, 0);
            if (currentQuantity < maxStorageCapacity) {
                storage.put(cargo, currentQuantity + 1);
            }
        }
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        logger.info("Stored product in machine storage: {}", cargo);
        storageSemaphore.release();
    }
}
```

**Aufrufer:** Eigener Maschinen-Thread (nach Produktion)

#### resiveCargo() - Cargo empfangen
```java
@Override
public int resiveCargo(Cargo cargo, int quantity) {
    try{
        storageSemaphore.acquire();
        if (storage.containsKey(cargo)) {
            int currentQuantity = storage.getOrDefault(cargo, 0);
            if (currentQuantity + quantity <= maxStorageCapacity) {
                storage.put(cargo, currentQuantity + quantity);
                return quantity;
            } else {
                int acceptedQuantity = maxStorageCapacity - currentQuantity;
                storage.put(cargo, maxStorageCapacity);
                return acceptedQuantity;
            }
        }
        else {
            return 0;
        }
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        storageSemaphore.release();
    }
}
```

**Aufrufer:** WarehouseClerk-Threads (Cargo-Lieferung)

#### handOverCargo() - Cargo übergeben
```java
@Override
public int handOverCargo(Cargo cargo, int quantity) {
    try{
        storageSemaphore.acquire();
        if (storage.containsKey(cargo)) {
            int currentQuantity = storage.getOrDefault(cargo, 0);
            if (currentQuantity >= quantity) {
                storage.put(cargo, currentQuantity - quantity);
                return quantity;
            } else {
                storage.put(cargo, 0);
                return currentQuantity;
            }
        }
        else {
            return 0;
        }
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        storageSemaphore.release();
    }
}
```

**Aufrufer:** WarehouseClerk-Threads (Cargo-Abholung)

#### getRemainingStorageCapacity() - Kapazität prüfen
```java
public boolean getRemainingStorageCapacity(Cargo cargo){
    int remainingCapacity;
    try {
        storageSemaphore.acquire();
        int currentQuantity = storage.getOrDefault(cargo, 0);
        remainingCapacity = maxStorageCapacity - currentQuantity;
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        storageSemaphore.release();
    }
    // ... weitere Berechnung mit notificationSemaphore ...
    return remainingCapacity > 0;
}
```

**Aufrufer:** Vorgänger-Maschine (vor Cargo-Übergabe)

### Zweck
Verhindert **Datenverlust und Inkonsistenzen** beim gleichzeitigen Zugriff von:
- Produktions-Thread (schreibt)
- WarehouseClerk-Threads (lesen/schreiben)
- Nachfolge-Maschinen (lesen)

---

## 3. Maschine - CargoOnTransit Synchronisation

### Kritische Ressource
```java
protected Queue<Cargo> cargosOnTransit = new LinkedList<>();
Semaphore notificationSemaphore = new Semaphore(1);
```

### Geschützte Operationen

#### addCargoTransitNotification() - Cargo-Transit ankündigen
```java
public void addCargoTransitNotification(Cargo cargo){
    try {
        notificationSemaphore.acquire();
        cargosOnTransit.add(cargo);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        notificationSemaphore.release();
    }
}
```

**Aufrufer:** Vorgänger-Maschine (vor GUI-Animation)

#### notifyMachineCargoHandoverCompleted() - Übergabe bestätigen
```java
public void notifyMachineCargoHandoverCompleted(){
    Cargo cargo;
    try {
        notificationSemaphore.acquire();
        cargo = cargosOnTransit.poll();
        if (cargo == null){
            logger.warn("No cargo found on transit");
            return;
        }
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        notificationSemaphore.release();
    }
    int warningQuantity = resiveCargo(cargo, 1);
}
```

**Aufrufer:** GUI-Thread (nach Animations-Abschluss)

#### getRemainingStorageCapacity() - Teil 2
```java
// ... nach storageSemaphore-Abschnitt ...
try {
    notificationSemaphore.acquire();
    for (Cargo c : cargosOnTransit){
        if (c.equals(cargo)){
            remainingCapacity -= 1;
        }
    }
} catch (InterruptedException e) {
    throw new RuntimeException(e);
} finally {
    notificationSemaphore.release();
}
return remainingCapacity > 0;
```

**Aufrufer:** Vorgänger-Maschine (Kapazitätsprüfung)

### Zweck
Koordiniert **Cargo-Übergaben** zwischen Maschinen und verhindert Überfüllung des Lagers durch Berücksichtigung von "unterwegs" befindlichen Gütern.

**Wichtig:** Vermeidet Deadlock durch **sequenzielle** Semaphore-Nutzung (erst storage, dann notification - nie gleichzeitig).

---

## 4. MainDepot - Storage Synchronisation

### Kritische Ressource
```java
private final Map<Cargo, Integer> cargoStorage;
private final Semaphore cargoStorageSemaphore = new Semaphore(1);
```

### Geschützte Operationen

#### resiveCargo() - Cargo annehmen
```java
public int resiveCargo(Cargo cargo, int quantity) {
    try {
        cargoStorageSemaphore.acquire();
        int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
        if (currentQuantity + quantity <= maxStorageCapacity) {
            cargoStorage.put(cargo, currentQuantity + quantity);
            checkAndUpdateStatus();
            return quantity;
        } else {
            int acceptedQuantity = maxStorageCapacity - currentQuantity;
            cargoStorage.put(cargo, maxStorageCapacity);
            checkAndUpdateStatus();
            return acceptedQuantity;
        }
    } catch (Exception e) {
        return 0;
    } finally {
        cargoStorageSemaphore.release();
    }
}
```

**Aufrufer:** 
- WarehouseClerk-Threads (Produkt-Lieferung)
- Supplier-Thread (Material-Nachschub)

#### handOverCargo() - Cargo ausgeben
```java
public int handOverCargo(Cargo cargo, int quantity) {
    try {
        cargoStorageSemaphore.acquire();
        int currentQuantity = cargoStorage.getOrDefault(cargo, 0);
        if (currentQuantity >= quantity) {
            cargoStorage.put(cargo, currentQuantity - quantity);
            checkAndUpdateStatus();
            return quantity;
        } else {
            cargoStorage.put(cargo, 0);
            checkAndUpdateStatus();
            return currentQuantity;
        }
    } catch (Exception e) {
        return 0;
    } finally {
        cargoStorageSemaphore.release();
    }
}
```

**Aufrufer:**
- WarehouseClerk-Threads (Material-Abholung)
- Supplier-Thread (Produkt/Schrott-Abholung)

### Zweck
Zentrale **Lagerverwaltung** mit Thread-sicheren Zugriffen durch:
- Mehrere WarehouseClerk-Threads (konkurrierend)
- Supplier-Thread (periodisch)

---

## Zusammenfassung

### Semaphore-Granularität
```
✅ GUT: Separate Locks pro Ressource (hohe Parallelität)
❌ SCHLECHT wäre: Ein globales Lock für alles

MainDepot.cargoStorageSemaphore     ┐
Maschine1.storageSemaphore          ├─ Parallel zugreifbar
Maschine2.storageSemaphore          │  Keine Konflikte
Maschine1.notificationSemaphore     │
ProductionHQ.requestQueueSemaphore  ┘
```

### Deadlock-Freiheit
**Garantiert durch:**
1. Maximal **1 Semaphore gleichzeitig** pro Thread
2. **Sequenzielle** Nutzung (acquire → release → acquire → release)
3. **Try-Finally** Pattern für garantierte Release

### Best Practice
```java
// ✅ Korrekt
semaphore.acquire();
try {
    // Kritischer Abschnitt
} finally {
    semaphore.release();  // Immer ausgeführt
}
```

---

**Nächstes Dokument:** [03-Monitor-Pattern.md](03-Monitor-Pattern.md) - Wait/Notify GUI-Koordination

