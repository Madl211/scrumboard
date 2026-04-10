# Projekt-Team3-Übung3

## Konfigurationsdatei anpassen
Konfigurationsdatei: _src/main/resources/application.properties_

### Parameter
_database.url_ - JDBC Connection URL der Datenbank (z.B. _jdbc:sqlite:/home/user/projects/softwaretechnik/scrumboard.db_ oder _C:\Users\user\projects\softwaretechnik\scrumboard.db_)


## Bauen
`mvn package`

## Starten
`java -jar scrumboard-*.jar`

('*' durch aktuelle Version ersetzen, z.B '1.0-SNAPSHOT')