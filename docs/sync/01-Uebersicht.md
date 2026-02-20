# Synchronisationsmodell - Übersicht

**Projekt:** BESYST - Smart Toy Produktionslinie  
**Datum:** 20. Februar 2026

---

## Einleitung

Dieses Dokument bietet eine Übersicht über das Synchronisationsmodell der Produktionslinie. Das System implementiert eine **Multithread-Produktionslinie** mit komplexen Synchronisationsmechanismen zur Koordinierung paralleler Prozesse.

---

## Architektur-Übersicht

### Thread-Hierarchie

```
ProductionHeadquarters (Singleton - Zentrale Verwaltung)
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

## Haupt-Synchronisationsmechanismen

### 1. Semaphore (binär, 1 Permit)

**Primärer Synchronisationsmechanismus** für kritische Ressourcen:

| Komponente | Semaphore | Zweck |
|------------|-----------|-------|
| ProductionHeadquarters | `requestQueueSemaphore` | Schutz der Request Queue |
| Maschine | `storageSemaphore` | Schutz des Cargo-Storage |
| Maschine | `notificationSemaphore` | Schutz der Transit-Queue |
| MainDepot | `cargoStorageSemaphore` | Schutz des Depot-Storage |

### 2. Monitor-Pattern (synchronized + wait/notify)

**GUI-Thread-Koordination:**

- **WarehouseClerk:** Wartet auf GUI-Bestätigung nach Bewegungsanimation
- **Supplier:** Wartet auf GUI-Bestätigung nach Bewegungsanimation

---

## Thread-Rollen

| Thread-Typ | Arbeitsweise | Hauptaufgabe |
|------------|--------------|--------------|
| **Maschine** | Kontinuierlich (Produktionszyklen) | Produktion, Cargo-Übergabe, Requests senden |
| **WarehouseClerk** | Request-basiert (reaktiv) | Cargo-Transport zwischen Stationen |
| **Supplier** | Timer-basiert (proaktiv) | MainDepot auffüllen & entleeren |
| **GUI** | Event-driven | Animationen & User-Interaktion |

---

## Kommunikationsmuster

### 1. Producer-Consumer
```
Maschinen (Producer) → Request Queue → WarehouseClerk (Consumer)
```

### 2. Pipeline
```
ProductionMaschine → ControlMachine → PackagingMaschine
```

### 3. Timer-basiert
```
Supplier --(periodisch)--> MainDepot
```

### 4. Polling + Callback (Maschine ↔ GUI)
```
Maschine A → cargoHandoverInProgress = true → GUI (Polling) → Animation → Callback → Maschine B
```

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

## Deadlock-Vermeidung

### Strategie 1: Keine verschachtelten Locks
- Jeder Thread erwirbt **maximal 1 Semaphore gleichzeitig**
- Keine Situationen, in denen Thread A auf B wartet während Thread B auf A wartet

### Strategie 2: Timeout & Retry
- Bei Ressourcen-Blockierung stoppt der Thread und wartet
- Retry-Mechanismus verhindert permanente Blockierungen

### Strategie 3: Priority Queue
- Verhindert Starvation durch Prioritäts-basierte Request-Verarbeitung

---

## Dokumentations-Struktur

Die vollständige Dokumentation ist in folgende Dokumente aufgeteilt:

1. **01-Uebersicht.md** (dieses Dokument) - Gesamtübersicht
2. **02-Semaphore.md** - Detaillierte Semaphore-Synchronisation
3. **03-Monitor-Pattern.md** - Wait/Notify GUI-Koordination
4. **04-Maschinen.md** - Maschinen-Thread-Implementierung
5. **05-WarehouseClerk.md** - WarehouseClerk-Thread-Implementierung
6. **06-Supplier.md** - Supplier-Thread-Implementierung
7. **07-Thread-Interaktionen.md** - Thread-Kommunikation & Flows
8. **08-Best-Practices.md** - Design-Patterns & Verbesserungen

---

## Zusammenfassung

### Stärken
✅ Konsistente Semaphore-Verwendung  
✅ Deadlock-frei durch No-Nested-Locks  
✅ Klare Thread-Verantwortlichkeiten  
✅ Hybrides Cargo-Management (request-basiert + timer-basiert)  
✅ Robuste GUI-Integration

### Verbesserungspotenzial
🟡 Singleton nicht thread-safe (könnte Double-Checked Locking verwenden)  
🟡 Busy-waiting bei Maschinen (könnte durch Condition Variables ersetzt werden)

---

**Nächste Schritte:** Siehe detaillierte Dokumentation in den verlinkten Dateien.


