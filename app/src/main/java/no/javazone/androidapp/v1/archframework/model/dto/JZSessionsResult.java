package no.javazone.androidapp.v1.archframework.model.dto;

import java.util.ArrayList;

public class JZSessionsResult {
    public String title;
    public String description;
    public String format;
    public JZPreciseDate start;
    public JZPreciseDate end;

    public ArrayList<JZSpeaker> speakerList;
    public ArrayList<JZLink> videoList;
    public ArrayList<JZLink> feedbackList;
    public JZLabel[] labels;
    public String language;
    public JZLevel level;
    public String room;

    public JZSessionsResult() {
        speakerList = new ArrayList<>();
    }

    public static JZSessionsResult from(final JZSession pItem) {

        JZSessionsResult session = new JZSessionsResult();

        session.description = pItem.getLinkHref("detaljer");
        session.start = new JZPreciseDate(pItem.getStarter());
        session.end = new JZPreciseDate(pItem.getStopper());
        session.format = pItem.getFormat();
        session.labels = toJZLabels(pItem.getNokkelord());
        session.level = new JZLevel(pItem.getNiva());
        session.room = pItem.getRom();
        session.speakerList.addAll(pItem.getForedragsholdere());
        session.videoList.addAll(pItem.getLinkHrefList("video"));
        session.feedbackList.addAll(pItem.getLinkHrefList("feedback"));
        session.title = pItem.getTitle();

        return session;

    }

    private static JZLabel[] toJZLabels(final ArrayList<String> pStrings) {

        if (pStrings == null) return new JZLabel[0];

        ArrayList<JZLabel> result = new ArrayList<JZLabel>(pStrings.size());

        for (String string : pStrings) {
            result.add(new JZLabel(string));

        }
        return result.toArray(new JZLabel[result.size()]);
    }

}
