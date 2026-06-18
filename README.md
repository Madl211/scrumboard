# Installations- und Kompilieranweisung

Im Folgenden wird beschrieben, wie man die Anwendung kompilieren kann:

## Voraussetzungen
- Java 21 oder höher (getestet unter Windows 11 und Ubuntu 24.04)
- Apache Maven 3.9 oder höher
- SQLite (keine separate Installation nötig – der JDBC-Treiber ist im Build enthalten)

## Schritt 1 – Repository klonen
`git clone https://git.informatik.uni-rostock.de/softwaretechnik-ws-2025-26/uebung3/team3/projekt-team3-uebung3.git`

`cd projekt-team3-uebung3`


## Schritt 2 – Konfigurationsdatei anpassen
Die Datei *src/main/resources/application.properties* enthält den Parameter *database.url*.
Dieser muss auf einen gültigen Pfad zur SQLite-Datenbankdatei gesetzt werden, z. B.:

`database.url=jdbc:sqlite:/home/user/scrumboard.db`

Die Datenbankdatei wird beim ersten Start automatisch angelegt.


## Schritt 3 – Projekt bauen:
`mvn package`


## Schritt 4 – Anwendung starten:
`java -jar target/scrumboard-1.0-SNAPSHOT.jar`


Die Anwendung ist anschließend im Browser unter localhost erreichbar.
