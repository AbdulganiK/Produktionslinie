# Thread-Interaktionen

**Dokumentation:** Synchronisationsmodell  
**Fokus:** Thread-Kommunikation & Flows

---

## 1. Producer-Consumer (Maschine → WarehouseClerk)

```
Maschine (Producer)              WarehouseClerk (Consumer)
───────────────────              ─────────────────────────
sendCargoRequest()
    │
    ▼
requestQueue.add(request) ────▶ requestQueue.poll()
    (via Semaphore)                  (via Semaphore)
                                     │
                                     ▼
                                runTaskCycle()
```

---

## 2. Pipeline (Maschine → Maschine)

```
Maschine A                       Maschine B
──────────                       ──────────
produceProduct()
    │
    ▼
getRemainingStorageCapacity() ──▶ storageSemaphore.acquire()
                                  check storage + cargosOnTransit
                                  storageSemaphore.release()
    │◀─────────────────────────── return hasCapacity
    ▼
notifyNextMaschineOfCargoSending() ──▶ cargosOnTransit.add(cargo)
                                       (via notificationSemaphore)
    │
    ▼
[GUI Animation]
    │
    ▼
notifyMachineCargoHandoverCompleted() ──▶ cargosOnTransit.poll()
                                           resiveCargo(cargo, 1)
```

---

## 3. Timer-basiert (Supplier → MainDepot)

```
Supplier                         MainDepot
────────                         ─────────
Timer: sleep(supplyInterval_ms)
    │
    ▼
supplyRoutine()
    │
    ├─▶ refillCargo(Material, qty) ──▶ cargoStorageSemaphore.acquire()
    │                                   cargoStorage.put(...)
    │                                   cargoStorageSemaphore.release()
    │
    └─▶ collectCargo(Product, qty) ──▶ cargoStorageSemaphore.acquire()
                                        cargoStorage.remove(...)
                                        cargoStorageSemaphore.release()
```

---

## 4. GUI-Synchronisation

```
Worker Thread                    GUI Thread
─────────────                    ──────────
status = TRAVEL_TO_STATION
awaitReady()
  └─ synchronized(this)
  └─ ready = false
  └─ wait()                      [Animation läuft]
      ↓ (blockiert)                   ↓
      ↓                          Animation fertig
      ↓                          worker.setReady()
      ↓                            └─ synchronized(this)
      ↓                            └─ ready = true
      ↓                            └─ notifyAll()
      ↓ (wird geweckt)                 
  └─ while(!ready) → false
  └─ verlässt wait()
Fortsetzung...
```

---

## 4. Polling + Callback (Maschine ↔ GUI)

```
Maschine A Thread                GUI Thread (Polling)             Maschine B Thread
─────────────────                ────────────────────             ─────────────────
deliverToNextMachine(cargo)
    │
    ├─▶ nextMaschine.addCargoTransitNotification(cargo) ──▶ cargosOnTransit.add()
    │                                                       (in Maschine B)
    │
    └─▶ cargoHandoverInProgress = true
            │
            │                    onUpdate() (60 FPS)
            │                        ↓
            │                    if (getCargoHandoverInProgress())
            │                        ↓ (true → auto-reset)
            │                    spawnItemOnBelt(belt)
            │                        ↓
            │                    [Item-Animation]
            │                        ↓
            │                    onCollision(machine, item)
            │                        ↓
            │                    item.removeFromWorld()
            │                        ↓
            │                    maschineB.notifyMachineCargoHandoverCompleted() ──▶ cargosOnTransit.poll()
            │                                                                         resiveCargo(cargo, 1)
            │                                                                         storage.put(...)
```

**Besonderheiten:**
- **Kein wait/notify:** Maschinen-Thread blockiert nicht
- **Polling:** GUI prüft Flag jeden Frame
- **Auto-Reset:** Flag wird beim Lesen zurückgesetzt
- **Callback:** GUI informiert über Animations-Ende
- **Semaphore:** Thread-safe für Transit-Queue und Storage

---

## Race Condition Schutz

### Szenario: Mehrere Threads greifen auf MainDepot zu

```
Thread 1 (WarehouseClerk)      Thread 2 (Supplier)
─────────────────────          ───────────────────
handOverCargo(wood, 5)         refillCargo(metal, 10)
    │                              │
    ▼                              ▼
acquire cargoStorageSem        wartet...
    │                              │
    ▼                              │
storage.put(wood, qty-5)           │
    │                              │
    ▼                              │
release cargoStorageSem            │
                                   ▼
                            acquire cargoStorageSem
                                   │
                                   ▼
                            storage.put(metal, qty+10)
                                   │
                                   ▼
                            release cargoStorageSem

✅ Resultat: Keine Daten-Corruption
```

---

## Deadlock-Freiheit

### Beweis

**Bedingung 1: Mutual Exclusion**  
✅ Erfüllt durch Semaphore

**Bedingung 2: Hold and Wait**  
✅ NICHT erfüllt - Threads halten nie mehrere Locks gleichzeitig

**Bedingung 3: No Preemption**  
✅ Erfüllt

**Bedingung 4: Circular Wait**  
✅ NICHT erfüllt - Keine zirkulären Abhängigkeiten

**Fazit: DEADLOCK-FREI** ✅

---

**Nächstes Dokument:** [08-Best-Practices.md](08-Best-Practices.md)


