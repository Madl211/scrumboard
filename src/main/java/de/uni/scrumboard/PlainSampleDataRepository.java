package de.uni.scrumboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlainSampleDataRepository implements SampleDataRepository {

    private Connection databaseConnection;

    public PlainSampleDataRepository(Connection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public List<SampleData> getSampleData() {
        String sqlStatement = "SELECT * FROM test;";
        try (Statement statement = databaseConnection.createStatement()) {
            List<SampleData> sampleDataSet = new ArrayList<>();

            ResultSet resultSet = statement.executeQuery(sqlStatement);
            while (resultSet.next()) {
                String field1 = resultSet.getString("field1");
                String field2 = resultSet.getString("field2");
                SampleData sampleData = new SampleData(field1, field2);
                sampleDataSet.add(sampleData);
            }

            return sampleDataSet;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
