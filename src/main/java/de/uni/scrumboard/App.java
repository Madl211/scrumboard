package de.uni.scrumboard;

import java.sql.Connection;
import java.sql.DriverManager;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;

public class App {

    public static void main(String[] args) {
        try {
            Connection databaseConnection = DriverManager
                    .getConnection("jdbc:sqlite:/home/zellmer/projects/softwaretechnik/projekt-team3-uebung3/test.db");
            SampleDataRepository sampleDataRepository = new PlainSampleDataRepository(databaseConnection);
            SampleService sampleService = new SampleService(sampleDataRepository);
            SampleController sampleController = new SampleController(sampleService);

            Javalin app = Javalin.create(config -> config.fileRenderer(new JavalinMustache()))
                    .get("/", sampleController::getSampleData)
                    .start(8080);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
