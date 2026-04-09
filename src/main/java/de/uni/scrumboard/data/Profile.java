package de.uni.scrumboard.data;

public class Profile {
    
    private String name;
    private String eMail;
    private String telefonnummer;
    private String postleitzahl;
    private String ort;
    private String strasse;
    private String hausnummer;
    
    public Profile(String name, String eMail, String telefonnummer, String postleitzahl, String ort, String strasse, String hausnummer) {
        this.name = name;
        this.eMail = eMail;
        this.telefonnummer = telefonnummer;
        this.postleitzahl = postleitzahl;
        this.ort = ort;
        this.strasse = strasse;
        this.hausnummer = hausnummer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getTelefonnummer() {
        return telefonnummer;
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    public String getPostleitzahl() {
        return postleitzahl;
    }

    public void setPostleitzahl(String postleitzahl) {
        this.postleitzahl = postleitzahl;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getHausnummer() {
        return hausnummer;
    }

    public void setHausnummer(String hausnummer) {
        this.hausnummer = hausnummer;
    }

}
