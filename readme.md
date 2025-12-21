# BESYST – Smart Toy Produktionslinie

## Projektübersicht
Dieses Projekt entstand im Rahmen des Moduls 3.8 – Betriebssysteme (BESYST) an der Hochschule Bremen,
Fakultät 4: Elektrotechnik und Informatik, im Studiengang Informatik: Software- und Systemtechnik.

Ziel des Projekts ist die Modellierung und Simulation einer nebenläufigen Produktionslinie für ein sogenanntes Smart Toy (Roboter), angelehnt an den Codey Rocky.
Der Fokus liegt dabei auf Nebenläufigkeit, Synchronisation, Ressourcenverwaltung und einer grafischen Darstellung (GUI) der Produktionsprozesse.

## Projektziel
- Modellierung einer Smart-Toy-Fabrik mit mehreren Produktionsstufen
- Simulation realer Produktionsprozesse mit Threads
- Analyse kritischer Bedingungen (Race Conditions, Synchronisation)
- Visualisierung des Material- und Produktionsflusses mittels GUI

## Szenario
In der simulierten Fabrik wird ein Roboter-Spielzeug produziert, bestehend aus:
- Zentrale Steuereinheit
  - Platine (CPU, Sensoren, Display)
  - Gehäuse
- Antriebseinheit
  - Platine (Motorsteuerung)
  - Zwei Motoren
  - Gehäuse

**Beide Einheiten werden separat gefertigt, montiert, geprüft und anschließend gemeinsam verpackt.**

## Struktur der Produktionslinie

Die Produktionslinie besteht aus folgenden Hauptkomponenten:
- Lieferatn
  - Liefert Rohmaterialien
  - Holt Abfälle und versandfertige Pakete ab
- Lager
  - Materiallager
  - Abfalllager
  - Lager für versandfertige Pakete
- Lageristen
  - Transportieren Materialien zwischen Lager und Maschinen
- Produktionsmaschinen
  - Herstellung von Gehäusen
  - Herstellung von Platinen
- Montagemaschinen
  - Zusammenbau der Antriebseinheit
  - Zusammenbau der zentralen Steuereinheit
- Kontrollmaschinen
  - Qualitätsprüfung beider Einheiten
- Verpackungsmaschinen
  - Verpacken des Roboters inkl. Versandlabel

## Nebenläufigkeit & Threadrollen

Jede Komponente der Produktionslinie wird als eigener Thread modelliert:
- Lieferant
- Lagerist
- Produktionsmaschinen
- Kontrollmaschinen
- Verpackungsmaschinen

### Kritische Aspekte
- Synchronisierter Zugriff auf das Lager
- Begrenzte Lagerkapazitäten
- Deadlocks durch wegnehmen von Ressourcen beim produzieren
- Paraller Zugriff auf dieselbe Ressource

