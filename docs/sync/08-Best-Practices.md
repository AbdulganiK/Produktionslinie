# Best Practices

**Dokumentation:** Synchronisationsmodell  
**Fokus:** Design-Patterns & Verbesserungen

---

## ✅ Implementierte Best Practices

### 1. Try-Finally bei Semaphore
Garantiert Release auch bei Exceptions

### 2. Immutability bei Request
```java
public record Request(...) {}
```

### 3. Daemon Threads
Automatisches Cleanup

### 4. While-Schleife bei wait()
Schutz vor Spurious Wakeups

---

## 🟡 Verbesserungspotenzial

### 1. Singleton nicht thread-safe

**Lösung: Double-Checked Locking**
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

### 2. Busy-Waiting bei Maschinen
Könnte durch Condition Variables ersetzt werden

### 3. volatile für shared flags
```java
protected volatile boolean running;
```

**Vorteil:** Garantiert, dass Änderungen sofort sichtbar sind

---

## 📊 Design-Patterns

### 1. Singleton
- **ProductionHeadquarters**
- Zentrale Verwaltungsinstanz

### 2. Producer-Consumer
- **Producer:** Maschinen
- **Queue:** Request Queue  
- **Consumer:** WarehouseClerk

### 3. Pipeline
- **Maschine A → B → C**
- Datenfluss-Architektur

### 4. Monitor
- **WarehouseClerk, Supplier**
- GUI-Synchronisation

### 5. Worker Pool
- **WarehouseClerk (mehrere Instanzen)**
- Lastverteilung

### 6. Timer-based Worker
- **Supplier**
- Periodische Tasks

---

## 🎯 Performance-Optimierungen

### 1. Granularität der Locks

```
✅ GUT: Fein-granulare Locks
- MainDepot.cargoStorageSemaphore
- Maschine1.storageSemaphore
- Maschine2.storageSemaphore
→ Hohe Parallelität

❌ SCHLECHT: Grob-granulares Lock
- Ein globales Lock für alle Operationen
→ Serialisierung, schlechte Performance
```

### 2. Lock-Free Structures (Optional)

**Für hohe Konkurrenz:**
```java
// Statt HashMap mit Semaphore
private final ConcurrentHashMap<Cargo, Integer> storage;

// Atomare Operationen
storage.compute(cargo, (k, v) -> (v == null) ? 1 : v + 1);
```

**Wann sinnvoll:**
- Sehr viele gleichzeitige Zugriffe
- Kurze kritische Abschnitte
- Hohe Anforderungen an Durchsatz

---

## 📋 Checkliste für Synchronisation

### Bei neuem kritischen Abschnitt:

- [ ] Semaphore/Lock identifiziert?
- [ ] Try-Finally verwendet?
- [ ] Minimal kritischer Abschnitt?
- [ ] Keine verschachtelten Locks?
- [ ] Deadlock-Potential analysiert?
- [ ] Thread-safety dokumentiert?

### Bei wait/notify:

- [ ] synchronized auf selber Instanz?
- [ ] While-Schleife statt if?
- [ ] notifyAll() statt notify()?
- [ ] Bedingung klar definiert?

---

## 🏆 Stärken des Systems

1. **Konsistente Architektur** - Klare Patterns durchgehend
2. **Deadlock-frei** - Durch No-Nested-Locks
3. **Gut testbar** - Klare Thread-Verantwortlichkeiten
4. **Wartbar** - Separation of Concerns
5. **Skalierbar** - Worker-Pool-Pattern
6. **Robust** - Error-Handling mit try-finally

---

## 📚 Zusammenfassung

### Thread-Sicherheit erreicht durch:
- ✅ Semaphore für Ressourcen-Schutz
- ✅ Monitor-Pattern für Event-Koordination
- ✅ Immutable Objekte (Request)
- ✅ Defensive Programmierung

### Empfohlene Verbesserungen:
1. Thread-safe Singleton
2. Condition Variables statt Busy-Waiting
3. volatile für shared flags

### Design-Qualität:
**⭐⭐⭐⭐ (4/5)** - Solide Implementierung mit kleineren Optimierungsmöglichkeiten

---

**Ende der Dokumentation**

## Dokumentations-Index

1. [01-Uebersicht.md](01-Uebersicht.md) - Gesamtübersicht
2. [02-Semaphore.md](02-Semaphore.md) - Semaphore-Details
3. [03-Monitor.md](03-Monitor.md) - Monitor-Pattern
4. [03a-Maschine-GUI-Sync.md](03a-Maschine-GUI-Sync.md) - Maschine-GUI-Synchronisation
5. [04-Maschinen.md](04-Maschinen.md) - Maschinen-Threads
6. [05-WarehouseClerk.md](05-WarehouseClerk.md) - WarehouseClerk
7. [06-Supplier.md](06-Supplier.md) - Supplier
8. [07-Interaktionen.md](07-Interaktionen.md) - Thread-Flows
9. [08-Best-Practices.md](08-Best-Practices.md) - Dieses Dokument
protected volatile boolean running;
```

---

## Design-Patterns

1. **Singleton** - ProductionHeadquarters
2. **Producer-Consumer** - Maschinen → WarehouseClerk
3. **Pipeline** - Maschine → Maschine
4. **Monitor** - GUI-Sync
5. **Worker Pool** - WarehouseClerk
6. **Timer-based Worker** - Supplier

---

**Design-Qualität: ⭐⭐⭐⭐ (4/5)**

