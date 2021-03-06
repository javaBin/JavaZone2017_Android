package no.javazone.androidapp.v1.archframework.model.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Random;

public class Session {
    @SerializedName(value= "sessionId", alternate = {"id"})
    public String id;
    @SerializedName(value = "abstract", alternate = {"description"})
    public String description;
    public String title;
    @SerializedName(value = "keywords", alternate={"tags"})
    public String[] tags;

    public String startTimestamp;
    public String videoUrl;
    public String[] speakers;
    public String endTimestamp;
    public String room;

    public String format;
    public String language;
    public String published;
    public String color;
    public int groupingOrder;
    public boolean starred;

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
