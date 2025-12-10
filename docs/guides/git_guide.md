# Git und GitHub Guide

## Wie bekomme ich das Projekt auf meinen Rechner?

```bash
git clone https://github.com/AbdulganiK/Produktionslinie.git
```

## Wie erstelle ich ein Ticket?
Bestimmt gibt es hierfür auch einen Weg über das Terminal aber am einfachsten ist es
unser git repository auf Github zu öffnen. Daraufhin kann man im Project reiter im Kanbanboard
Tickets erstellen.

Projects &#8594; Produktionslinie &#8594; Neben "View" den Pfeil nach unten klicken &#8594; Board  &#8594; Add item

## Wie arbeite ich an einem Ticket?

### Wichtig!
Bevor wir mit einem Ticket anfangen sollten wir im Kanbanboard schauen ob jemand bereits das Ticket übernommen hat. 
Das kann man dadurch erkennen, dass das Ticket nicht bei **Todo** ist sondern bei **In Progress**. Wenn ihr mit einem 
Ticket anfängt zieht es bitte zu **In Progress** damit wir wissen woran wir nicht arbeiten müssen und keine Kollisionen
entstehen. Gebt unter Assignment im Ticket auch an *Wer an diesem Ticket arbeitet* damit wir gegebenfalls nachfragen 
können falls ihr nicht weiter kommt.

### Erstellen einer Lokalen Branch für das Ticket

```bash
git checkout -b tix-[TICKET-NUMMER]
```

### Änderungen mithilfe eines Commits versionieren

#### Alle Änderungen lokal hinzufügen 
```bash
git add .
```

#### Änderungen lokal speichern
```bash
git commit -m "TOLLE ÄNDERUNGEN [TICKET-NUMMER]"
```

### Wie bringe ich meine lokale branch auf GitHub?
Bevor ihr eure Sachen auf github bringen wollt, solltet ihr das Github repository nochmal pullen damit ihr später nicht
nicht mit merge konflikten kämpfen müsst.
```bash
git pull origin main
```

Wenn ihr alle Konflikte beseitigt habt und sicher seid, dass ihr mit dem master/main branch up to date seid dürft ihr
mit dem folgenden Befehl eure branch zum Github Repo bringen.
```bash
git push -u tix-[TICKET-NUMMER]
```

Nun ist es möglich über die github ui einen pull request zum mergen mit dem master/main branch zu machen.
Dannach solltet ihr **DRINGEND** euer ticket in **DONE** im kanbanboard tun.

### Wie komme ich zurück zum Master/Main Branch
```bash
git checkout main
```

Am besten macht ihr einen git pull damit ihr wieder up to date seid nach mit dem main:
```bash
git pull
```

### Wie lösche ich die lokale branch nach dem mergen mit dem main
```bash
git branch -D tix-[TICKET_NUMMER]
```


