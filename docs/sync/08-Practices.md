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

