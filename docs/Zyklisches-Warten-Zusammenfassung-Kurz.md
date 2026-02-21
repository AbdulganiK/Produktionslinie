# 🎯 Zyklisches Warten - Zusammenfassung in Kürze

## Hauptergebnis

**Das System ist vollständig DEADLOCK-FREI bezüglich zyklischem Warten.**

## Begründung

Die Resource Allocation Graph (RAG) Analyse zeigt, dass **keine geschlossenen Zyklen** existieren, weil:

1. **Zeitliche Trennung (WarehouseClerk):** Der WarehouseClerk gibt `requestQueueSemaphore` vollständig frei, bevor er `storageSemaphore` anfordert. Zwischen beiden Locks liegt zusätzlich eine `awaitReady()`-Wartezeit, die eine zeitliche Trennung erzwingt und potenzielle bidirektionale Lock-Abhängigkeiten zwischen Maschinen und WarehouseClerks unterbricht.

2. **Ressourcen-Isolation (Maschinen):** Jede Maschine greift ausschließlich auf ihre eigenen Semaphore (`this.storageSemaphore`, `this.notificationSemaphore`) zu. Es gibt keine kreuzweisen Zugriffe zwischen Maschinen M1, M2, ..., M9, wodurch Maschine-zu-Maschine-Zyklen unmöglich sind.

3. **Sequenzielle Lock-Verwaltung:** In kritischen Methoden wie `getRemainingStorageCapacity()` werden Locks sequenziell verwaltet: `storageSem.acquire()` → `release()` → `notificationSem.acquire()` → `release()`. Die Locks überschneiden sich nie, was Hold-and-Wait innerhalb einer Maschine verhindert.

4. **Monitor-Pattern (wait/notify):** Die `wait()`-Methode gibt den Monitor-Lock temporär frei, während der Thread schläft. Dadurch kann der GUI-Thread den Lock erwerben, `notifyAll()` aufrufen und den wartenden Thread aufwecken, ohne dass es zu einer Blockade kommt.

## Mathematischer Beweis

Ein Widerspruchsbeweis durch systematische Tiefensuche (DFS) im RAG zeigt, dass alle vier möglichen Zyklus-Fälle (requestQueue-involviert, Maschinen-intern, Maschinen-übergreifend, Monitor-involviert) zu logischen Widersprüchen führen, wenn man die Code-Semantik und zeitlichen Abläufe berücksichtigt.

## Praktische Validierung

Stress-Tests mit 9 Maschinen, mehreren WarehouseClerks unter maximaler Produktionslast zeigen kontinuierliche Aktivität ohne blockierte Threads.

---

**Fazit:** Keine Änderungen erforderlich. Das System ist optimal gegen Deadlocks durch zyklisches Warten geschützt.

**Datum:** 21. Februar 2026

