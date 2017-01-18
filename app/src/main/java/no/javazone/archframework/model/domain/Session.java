package no.javazone.archframework.model.domain;

import java.util.Random;

public class Session {
    public String id;
    public String description;
    public String title;
    public String[] tags;
    public String startTimestamp;
    public String videoUrl;
    public String[] speakers;
    public String endTimestamp;
    public String room;
    public boolean hasStarted;
    public String color;
    public int groupingOrder;

    public String makeTagsList() {
        int i;
        if (tags.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(tags[0]);
        for (i = 1; i < tags.length; i++) {
            sb.append(",").append(tags[i]);
        }
        return sb.toString();
    }

    public boolean hasTag(String tag) {
        for (String myTag : tags) {
            if (myTag.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public String getImportHashCode() {
        return (new Random()).nextLong()+"";
    }
}
