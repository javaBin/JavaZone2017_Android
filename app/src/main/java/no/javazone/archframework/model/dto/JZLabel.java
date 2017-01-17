package no.javazone.archframework.model.dto;

import java.net.URL;

public class JZLabel {
    public String displayName;
    public URL iconUrl;
    public String id;

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        JZLabel jzLabel = (JZLabel) o;

        if (!id.equals(jzLabel.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public JZLabel(final String pString) {
        displayName=pString;
        id=pString;


    }
}
