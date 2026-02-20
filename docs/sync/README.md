# Synchronisationsmodell - Dokumentation

**BESYST - Smart Toy Produktionslinie**  
**Datum:** 20. Februar 2026

---

## 📚 Dokumentations-Struktur

Die Analyse des Synchronisationsmodells ist in folgende Dokumente aufgeteilt:

### 1. [Übersicht](01-Uebersicht.md) 📋
- Architektur-Übersicht
- Thread-Hierarchie
- Kommunikationsmuster
- Kritische Abschnitte
- Deadlock-Vermeidung

### 2. [Semaphore](02-Semaphore.md) 🔒
- ProductionHeadquarters Request Queue
- Maschine Storage-Synchronisation
- Maschine CargoOnTransit-Synchronisation
- MainDepot Storage-Synchronisation
- Semaphore-Patterns & Best Practices

### 3. [Monitor-Pattern](03-Monitor.md) 👁️
- WarehouseClerk Animation-Synchronisation
- Supplier Animation-Synchronisation
- wait/notify Mechanismus
- GUI-Thread-Koordination

### 4. [Maschinen](04-Maschinen.md) ⚙️
- Thread-Lebenszyklus
- Produktionszyklus
- Cargo-Übergabe
- Request-System

### 5. [WarehouseClerk](05-WarehouseClerk.md) 🚚
- Worker-Pool-Pattern
- Request-Verarbeitung
- Task-Zyklus
- Cargo-Operationen

### 6. [Supplier](06-Supplier.md) 📦
- Timer-basierte Arbeitsweise
- Supply-Routine
- MainDepot-Interaktion
- Unterschiede zum WarehouseClerk

### 7. [Thread-Interaktionen](07-Interaktionen.md) 🔄
- Producer-Consumer-Flow
- Pipeline-Flow
- Timer-basierter Flow
- GUI-Synchronisation
- Race-Condition-Schutz
- Deadlock-Freiheit-Beweis

### 8. [Best Practices](08-Practices.md) ⭐
- Implementierte Patterns
- Verbesserungspotenzial
- Design-Patterns
- Performance-Optimierungen
- Synchronisations-Checkliste

---

## 🎯 Schnellzugriff

### Thread-Typen verstehen?
→ [01-Uebersicht.md](01-Uebersicht.md#thread-rollen)

### Wie funktionieren Semaphore?
→ [02-Semaphore.md](02-Semaphore.md)

### Wie synchronisiert die GUI?
→ [03-Monitor.md](03-Monitor.md)

### Wie arbeiten Maschinen zusammen?
→ [04-Maschinen.md](04-Maschinen.md) + [07-Interaktionen.md](07-Interaktionen.md)

### Unterschied WarehouseClerk vs. Supplier?
→ [06-Supplier.md](06-Supplier.md#unterschied-zum-warehouseclerk)

### Ist das System deadlock-frei?
→ [07-Interaktionen.md](07-Interaktionen.md#deadlock-freiheit)

### Was kann verbessert werden?
→ [08-Practices.md](08-Practices.md#verbesserungspotenzial)

---

## 🔑 Kernkonzepte

### Synchronisationsmechanismen
1. **Semaphore (binär, 1 Permit)** - Ressourcen-Schutz
2. **Monitor (synchronized + wait/notify)** - Event-Koordination

### Thread-Rollen
- **Maschinen:** Kontinuierlich, Produktion, **Polling-basierte GUI-Sync**
- **WarehouseClerk:** Request-basiert, Cargo-Transport, **Monitor-basierte GUI-Sync**
- **Supplier:** Timer-basiert, Depot-Nachschub, **Monitor-basierte GUI-Sync**
- **GUI:** Event-driven, Animationen, **Polling + Callbacks**

### Design-Patterns
- Singleton (ProductionHeadquarters)
- Producer-Consumer (Request Queue)
- Pipeline (Maschinen-Kette)
- Worker Pool (WarehouseClerk)
- Monitor (GUI-Sync)

---

## 📊 Statistik

- **Threads:** ~10-20 (konfigurierbar)
- **Semaphore:** 4-5 pro Maschine + 1 global
- **Monitor-Pattern:** 2 Implementierungen
- **Deadlock-Risiko:** ❌ Keine (bewiesen)
- **Race Conditions:** ✅ Geschützt

---

## 🏗️ Architektur-Diagramm

```
ProductionHeadquarters (Singleton)
    │
    ├─ Maschinen (Threads)
    │   ├─ ProductionMaschine ──┐
    │   ├─ ControlMachine       ├─▶ Pipeline
    │   └─ PackagingMaschine ───┘
    │
    ├─ WarehouseClerk (Worker Pool)
    │   └─ Poll Requests → Transport Cargo
    │
    ├─ Supplier (Timer-based)
    │   └─ Periodisch → MainDepot auffüllen
    │
    └─ MainDepot (Station)
        └─ Zentrale Lagerverwaltung
```

---

## 📝 Legende

| Symbol | Bedeutung |
|--------|-----------|
| ✅ | Implementiert / Korrekt |
| 🟡 | Verbesserungspotenzial |
| ❌ | Problem / Nicht vorhanden |
| 🔒 | Thread-safe / Synchronisiert |
| ⚠️ | Achtung / Wichtig |

---

## 🚀 Empfohlene Lesereihenfolge

**Für Einsteiger:**
1. Übersicht (01)
2. Maschinen (04)
3. WarehouseClerk (05)
4. Interaktionen (07)

**Für Synchronisations-Details:**
1. Übersicht (01)
2. Semaphore (02)
3. Monitor-Pattern (03)
4. Best Practices (08)

**Für Vollständiges Verständnis:**
1. → 2. → 3. → 4. → 5. → 6. → 7. → 8. (sequenziell)

---

## 📞 Weitere Ressourcen

- **Hauptdokumentation:** `../readme.md`
- **Programm-Manual:** `../programmManuel.md`
- **Git-Guide:** `../guides/git_guide.md`

---

**Viel Erfolg beim Verstehen des Synchronisationsmodells! 🎓**


