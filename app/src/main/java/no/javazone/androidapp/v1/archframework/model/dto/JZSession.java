package no.javazone.androidapp.v1.archframework.model.dto;

import java.util.ArrayList;
import java.util.List;

public class JZSession {
    private String title;
    private String format;
    private String starter;
    private String stopper;
    private ArrayList<JZSpeaker> foredragsholdere;
    private String sprak;
    private String niva;
    private ArrayList<JZLink> links;
    private String rom;
    private ArrayList<String> nokkelord;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStarter() {
        return starter;
    }

    public void setStarter(String starter) {
        this.starter = starter;
    }

    public String getStopper() {
        return stopper;
    }

    public void setStopper(String stopper) {
        this.stopper = stopper;
    }

    public ArrayList<JZSpeaker> getForedragsholdere() {
        return foredragsholdere;
    }

    public void setForedragsholdere(ArrayList<JZSpeaker> foredragsholdere) {
        this.foredragsholdere = foredragsholdere;
    }

    public String getSprak() {
        return sprak;
    }

    public void setSprak(String sprak) {
        this.sprak = sprak;
    }

    public String getNiva() {
        return niva;
    }

    public void setNiva(String niva) {
        this.niva = niva;
    }

    public ArrayList<JZLink> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<JZLink> links) {
        this.links = links;
    }

    public String getRom() {
        return rom;
    }

    public void setRom(String rom) {
        this.rom = rom;
    }

    public ArrayList<String> getNokkelord() {
        return nokkelord;
    }

    public void setNokkelord(ArrayList<String> nokkelord) {
        this.nokkelord = nokkelord;
    }

    public JZLink getLink(final String rel) {
        for (JZLink link : links) {
            if (rel.equalsIgnoreCase(link.getRel())) {
                return link;
            }
        }
        return null;
    }

    public List<JZLink> getLinkList(final String rel) {
        List<JZLink> relevantLinks = new ArrayList<>();
        for (JZLink link : links) {
            if (rel.equalsIgnoreCase(link.getRel())) {
                relevantLinks.add(link);
            }
        }
        return relevantLinks;
    }

    public String getLinkHref(final String rel) {
        JZLink link = getLink(rel);
        if (link != null) {
            return link.getRel();
        } else {
            return null;
        }
    }

    public List<JZLink> getLinkHrefList(final String rel) {
        List<JZLink> getAllRelevantHrefList = new ArrayList<>();
        getAllRelevantHrefList.addAll(getLinkList(rel));
        return getAllRelevantHrefList;
    }
}
