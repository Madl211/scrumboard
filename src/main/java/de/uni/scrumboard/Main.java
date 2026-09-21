package de.uni.scrumboard;

import de.uni.scrumboard.application.App;

public class Main {
    public static void main(String[] args) {
        App app = new App();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        app.stop();
        }));
        
        new App().run(8080);
    }
    
}
