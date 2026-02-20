# Maschinen-Threads

**Dokumentation:** Synchronisationsmodell  
**Fokus:** Maschinen-Thread-Implementierung (ProductionMaschine, ControlMachine, PackagingMaschine)

---

## Thread-Lebenszyklus

```java
@Override
public void run() {
    while (true) {
        runProductionCycle();
        Thread.sleep(timeToSleep);
    }
}
```

**Eigenschaften:**
- Daemon-Thread (automatisches Shutdown)
- Endlos-Schleife
- Sleep zwischen Zyklen

---

## Produktionszyklus

```java
private void runProductionCycle() {
    checkStorageStatus();           // Status aktualisieren
    checkIfCargoProductionIsPossible();  // Kann produziert werden?
    if (running){
        Cargo producedCargo = produceProduct();  // Produzieren
        storeProductOrDeliverToNextMachine(producedCargo);  // Weitergeben
    }
}
```

---

## Cargo-Übergabe an Nachfolger

```java
protected void deliverToNextMachine(Cargo cargo) {
    while (!cargoNotified) {
        boolean hasCapacity = nextMaschine.getRemainingStorageCapacity(cargo);
        if (!hasCapacity) {
            stopMachine();  // Warten statt blockieren
            Thread.sleep(timeToSleep);
        } else {
            notifyNextMaschineOfCargoSending(cargo);
            cargoHandoverToNextMaschineInProgress = true;
            cargoNotified = true;
        }
    }
}
```

**Deadlock-Vermeidung:** Retry statt permanenter Blockierung

---

## Request-System

```java
protected void sendCargoRequest(Cargo cargo, int quantity) {
    boolean requestedBefore = requestedCargoTypes.getOrDefault(cargo, false);
    if (!requestedBefore){
        Request request = new Request(quantity, maschinePriority, cargo, identificationNumber);
        ProductionHeadquarters.getInstance().addRequest(request);
        requestedCargoTypes.put(cargo, true);
    }
}
```

**Producer im Producer-Consumer-Pattern:** Maschine → Request Queue → WarehouseClerk

---

**Nächstes Dokument:** [05-WarehouseClerk.md](05-WarehouseClerk.md)

