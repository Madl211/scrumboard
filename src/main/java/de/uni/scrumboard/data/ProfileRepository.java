package de.uni.scrumboard.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public record ProfileRepository(Connection connection) implements IProfileRepository {

    @Override
    public Profile getProfile(int accountId) {
        try {
            PreparedStatement sqlStatement = connection.prepareStatement("SELECT * FROM profiles WHERE accountId = ?");
            sqlStatement.setInt(1, accountId);

            ResultSet result = sqlStatement.executeQuery();
            Profile profile = mapResultToProfile(result);

            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Fehler bei der Abfrage des Profils mit der AccountID " + accountId + " aufgetreten", e);
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

    @Override
    public boolean saveProfile() {
        return true;
    }

}
