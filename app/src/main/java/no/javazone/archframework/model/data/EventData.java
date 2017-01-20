package no.javazone.archframework.model.data;

import java.util.ArrayList;

public class EventData {
    private ArrayList<EventCard> mCards = new ArrayList<>();
    private String mTitle;

    public EventData() {}

    public void addEventCard(EventCard card) {
        mCards.add(card);
    }

    public ArrayList<EventCard> getCards() {
        return mCards;
    }

    public String getTitle() {
        return mTitle;
    }
}
