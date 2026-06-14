package de.uni.scrumboard.application;

public class ResetResult {
    private boolean success;
    private String message;
    
    // Konstruktor mit Parametern (wichtig für den Service!)
    public ResetResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Standard-Konstruktor
    public ResetResult() {}
    
    // Getter und Setter
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}