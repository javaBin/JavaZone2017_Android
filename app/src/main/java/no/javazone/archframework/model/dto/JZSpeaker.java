package no.javazone.archframework.model.dto;

public class JZSpeaker {
   private String navn;
    private String bildeUri;

    public String getNavn() {
        return navn;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public String getBildeUri() {
        return bildeUri;
    }

    public void setBildeUri(String bildeUri) {
        this.bildeUri = bildeUri;
    }
}
