# WarehouseClerk-Threads

**Dokumentation:** Synchronisationsmodell  
**Fokus:** WarehouseClerk-Thread-Implementierung (Worker-Pool)

---

## Thread-Lebenszyklus

```java
@Override
public void run() {
    while (true) {
        runTaskCycle();
        Thread.sleep(timeForSleep_ms);
    }
}
```

**Eigenschaften:**
- Daemon-Thread
- Worker-Pool (mehrere Instanzen)
- Request-basiert (reaktiv)

---

## Task-Zyklus

```java
private void runTaskCycle() {
    boolean hasRequest = getRequested();  // Poll aus Request Queue
    if (hasRequest) {
        // 1. Reise zur Quelle
        awaitReady();  // GUI-Sync
        
        // 2. Cargo sammeln
        collectCargo(cargo, quantity);
        
        // 3. Reise zum Ziel
        awaitReady();  // GUI-Sync
        
        // 4. Cargo abliefern
        refillCargo(cargo, quantity);
        
        // 5. Zurück zur Zentrale
        awaitReady();  // GUI-Sync
        
        // 6. Request als erledigt markieren
        requestedMachine.markRequestAsCompleted(cargo);
    }
}
```

---

## Request-Verarbeitung

```java
private boolean getRequested() {
    currentRequest = ProductionHeadquarters.getInstance().pollRequest();
    if (currentRequest != null) {
        cargo = currentRequest.cargo();
        if (cargo.getCargoTyp() == CargoTyp.MATERIAL) {
            task = Task.DELIVERING;
            originStationId = mainDepotId;
            destinationStationId = currentRequest.stationId();
        } else {
            task = Task.EMPTYING;
            originStationId = currentRequest.stationId();
            destinationStationId = mainDepotId;
        }
        return true;
    }
    return false;
}
```

**Consumer im Producer-Consumer-Pattern**

---

## Cargo-Operationen

```java
public int refillCargo(Cargo cargo, int quantity) {
    Station destStation = ProductionHeadquarters.getInstance()
                          .getStations().get(destinationStationId);
    return destStation.resiveCargo(cargo, quantity);
}

public int collectCargo(Cargo cargo, int quantity) {
    Station originStation = ProductionHeadquarters.getInstance()
                            .getStations().get(originStationId);
    return originStation.handOverCargo(cargo, quantity);
}
```

**Nutzt Semaphore-geschützte Station-Methoden**

---

**Nächstes Dokument:** [06-Supplier.md](06-Supplier.md)

