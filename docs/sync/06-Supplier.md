# Supplier-Thread

**Dokumentation:** Synchronisationsmodell  
**Fokus:** Supplier-Thread-Implementierung (Timer-basiert)

---

## Thread-Lebenszyklus

```java
@Override
public void run() {
    status = StatusInfo.OPERATIONAL;
    while (true) {
        try {
            supplyRoutine();
            Thread.sleep(supplyInterval_ms);  // z.B. 30 Sekunden
        } catch (InterruptedException e) {
            status = StatusWarning.STOPPED;
            Thread.currentThread().interrupt();
            break;  // Graceful Shutdown
        }
    }
}
```

**Eigenschaften:**
- Daemon-Thread
- Timer-basiert (proaktiv)
- Graceful Shutdown möglich

---

## Supply-Routine

```java
private void supplyRoutine() throws InterruptedException {
    // 1. Cargo-Storage initialisieren
    int perMaterial = maxCapacity / Material.values().length;
    for (Material m : Material.values()) {
        cargoStorage.put(m, perMaterial);
    }
    cargoStorage.put(Product.SCRAP, 0);
    cargoStorage.put(Product.PACKAGE, 0);
    
    // 2. Hinfahrt zum MainDepot
    task = Task.DELIVERING;
    idOfCurrentDestinationStation = mainDepotId;
    awaitReady();  // GUI-Sync
    
    // 3. Depot befüllen & leeren
    refillDepotAndCollectCargo();
    
    // 4. Rückfahrt
    task = Task.TRANSPORTING;
    idOfCurrentDestinationStation = -1;  // Außerhalb
    awaitReady();  // GUI-Sync
}
```

---

## Depot-Interaktion

```java
private void refillDepotAndCollectCargo() throws InterruptedException {
    Thread.sleep(supplyTimer_ms);
    
    // Materialien liefern
    for (Material m : Material.values()) {
        int qty = cargoStorage.get(m);
        int delivered = refillCargo(m, qty);
        cargoStorage.put(m, qty - delivered);
    }
    
    // Produkte abholen
    int freeCapacity = maxCapacity - cargoStorage.values().stream()
                                      .mapToInt(Integer::intValue).sum();
    int collected = collectCargo(Product.PACKAGE, freeCapacity);
    freeCapacity -= collected;
    
    // Schrott abholen
    collected = collectCargo(Product.SCRAP, freeCapacity);
}
```

---

## Besonderheiten

### 1. Kein Request-System
Komplett unabhängig von Maschinen-Anforderungen

### 2. Eigener Cargo-Storage
```java
private final HashMap<Cargo, Integer> cargoStorage;
```
**Kein Semaphore nötig** - nur vom eigenen Thread genutzt

### 3. MainDepot-Zugriff
Nutzt MainDepot's `cargoStorageSemaphore` für Thread-Safety

### 4. Zwei-Wege-Kommunikation
- **Hinweg:** Materialien liefern
- **Rückweg:** Produkte & Schrott abholen

---

## Unterschied zum WarehouseClerk

| Aspekt | WarehouseClerk | Supplier |
|--------|----------------|----------|
| **Trigger** | Request-basiert | Timer-basiert |
| **Arbeitsweise** | Reaktiv | Proaktiv |
| **Frequenz** | On-demand | Periodisch |
| **GUI-Sync** | 3x pro Request | 2x pro Zyklus |
| **Shutdown** | Endlos | Graceful |

---

**Nächstes Dokument:** [07-Interaktionen.md](07-Interaktionen.md)

