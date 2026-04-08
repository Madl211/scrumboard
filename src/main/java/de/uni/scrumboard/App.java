package de.uni.scrumboard;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinMustache;

public class App {

    public static void main(String[] args) {
        Javalin.create(config -> config.fileRenderer(new JavalinMustache())).start(8080);
    }

}
