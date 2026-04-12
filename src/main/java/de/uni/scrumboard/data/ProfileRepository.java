package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record ProfileRepository(Connection connection) implements IProfileRepository {

    @Override
    public Profile getProfile(int accountId) {
        try (PreparedStatement sqlStatement = connection.prepareStatement("SELECT * FROM profiles WHERE accountId = ?")) {
            sqlStatement.setInt(1, accountId);

            try (ResultSet result = sqlStatement.executeQuery()) {
                if (result.next()) {
                    return mapResultToProfile(result);
                }

                return new Profile("", "", "", "", "", "", "");
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Fehler bei der Abfrage des Profils mit der AccountID " + accountId + " aufgetreten", e);
        }
    }

    @Override
    public boolean saveProfile(Profile profile, int accountId) {
        try (PreparedStatement sqlStatement = connection.prepareStatement(
                "INSERT INTO profiles(accountId, name, eMail, telefonnummer, postleitzahl, ort, strasse, hausnummer) "
                        + "VALUES(?,?,?,?,?,?,?,?) "
                        + "ON CONFLICT(accountId) DO UPDATE SET "
                        + "name = excluded.name, "
                        + "eMail = excluded.eMail, "
                        + "telefonnummer = excluded.telefonnummer, "
                        + "postleitzahl = excluded.postleitzahl, "
                        + "ort = excluded.ort, "
                        + "strasse = excluded.strasse, "
                        + "hausnummer = excluded.hausnummer")) {

            sqlStatement.setInt(1, accountId);
            sqlStatement.setString(2, profile.name());
            sqlStatement.setString(3, profile.eMailAddr());
            sqlStatement.setString(4, profile.telNr());
            sqlStatement.setString(5, profile.plz());
            sqlStatement.setString(6, profile.ort());
            sqlStatement.setString(7, profile.strasse());
            sqlStatement.setString(8, profile.hausNr());

            sqlStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Speichern des Profils für AccountID " + accountId, e);
        }
    }

    private Profile mapResultToProfile(ResultSet result) {
        try {
            String name = result.getString("name");
            String eMail = result.getString("eMail");
            String telefonnummer = result.getString("telefonnummer");
            String postleitzahl = result.getString("postleitzahl");
            String ort = result.getString("ort");
            String strasse = result.getString("strasse");
            String hausnummer = result.getString("hausnummer");

            return new Profile(name, eMail, telefonnummer, postleitzahl, ort, strasse, hausnummer);
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Mapping des Profils aufgetreten", e);
        }
    }

}
