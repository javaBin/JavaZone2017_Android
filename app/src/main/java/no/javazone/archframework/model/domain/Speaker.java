package no.javazone.archframework.model.domain;

import no.javazone.util.HashUtils;

public class Speaker {
    public String id;
    public String publicPlusId;
    public String bio;
    public String name;
    public String company;
    public String plusoneUrl;
    public String twitterUrl;
    public String thumbnailUrl;

    public String getImportHashcode() {
        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id == null ? "" : id)
                .append("publicPlusId").append(publicPlusId == null ? "" : publicPlusId)
                .append("bio").append(bio == null ? "" : bio)
                .append("name").append(name == null ? "" : name)
                .append("company").append(company == null ? "" : company)
                .append("plusoneUrl").append(plusoneUrl == null ? "" : plusoneUrl)
                .append("twitterUrl").append(twitterUrl == null ? "" : twitterUrl)
                .append("thumbnailUrl").append(thumbnailUrl == null ? "" : thumbnailUrl);
        return HashUtils.computeWeakHash(sb.toString());
    }
}
