package no.javazone.androidapp.v1.archframework.model;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.archframework.model.data.EventCard;
import no.javazone.androidapp.v1.archframework.model.data.EventData;
import no.javazone.androidapp.v1.archframework.model.data.ItemGroup;
import no.javazone.androidapp.v1.archframework.model.data.LiveData;
import no.javazone.androidapp.v1.archframework.model.data.MessageData;
import no.javazone.androidapp.v1.archframework.model.data.SessionData;
import no.javazone.androidapp.v1.archframework.model.domain.TagMetadata;
import no.javazone.androidapp.v1.archframework.utils.MessageCardHelper;
import no.javazone.androidapp.v1.archframework.view.UserActionEnum;
import no.javazone.androidapp.v1.database.QueryEnum;
import no.javazone.androidapp.v1.database.ScheduleContract;
import no.javazone.androidapp.v1.settings.ConfMessageCardUtils;
import no.javazone.androidapp.v1.util.SettingsUtils;
import no.javazone.androidapp.v1.util.TagUtils;
import no.javazone.androidapp.v1.util.TimeUtils;
import no.javazone.androidapp.v1.util.WiFiUtils;

import static no.javazone.androidapp.v1.util.LogUtils.LOGD;
import static no.javazone.androidapp.v1.util.LogUtils.LOGE;
import static no.javazone.androidapp.v1.util.LogUtils.LOGI;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

public class ExploreModel extends ModelWithLoaderManager<ExploreModel.ExploreQueryEnum,
        ExploreModel.ExploreUserActionEnum> {

    private static final String TAG = makeLogTag(ExploreModel.class);

    private final Context mContext;
    @NonNull
    private EventData mEventData = new EventData();
    private SessionData mKeynoteData;
    private LiveData mLiveData;
    private List<ItemGroup> mOrderedTracks;
    private Uri mSessionsUri;
    private TagMetadata mTagMetadata;
    /**
     * Theme groups loaded from the database pre-randomly filtered and stored by topic name. Not
     * shown in current design.
     */
    private Map<String, ItemGroup> mThemes = new HashMap<>();
    /**
     * Topic groups loaded from the database pre-randomly filtered and stored by topic name.
     */
    private Map<String, ItemGroup> mTracks = new HashMap<>();

    public ExploreModel(Context context, Uri sessionsUri, LoaderManager loaderManager) {
        super(ExploreQueryEnum.values(), ExploreUserActionEnum.values(), loaderManager);
        mContext = context;
        mSessionsUri = sessionsUri;
    }

    @Override
    public void cleanUp() {
        mThemes.clear();
        mThemes = null;
        mTracks.clear();
        mTracks = null;
        mOrderedTracks.clear();
        mOrderedTracks = null;
        mKeynoteData = null;
        mLiveData = null;
    }

    @Override
    public Loader<Cursor> createCursorLoader(final ExploreQueryEnum query, final Bundle args) {
        CursorLoader loader = null;

        switch (query) {
            case SESSIONS:
                // Create and return the Loader.
                loader = getCursorLoaderInstance(mContext, mSessionsUri,
                        ExploreQueryEnum.SESSIONS.getProjection(), null, null,
                        ScheduleContract.Sessions.SORT_BY_TYPE_THEN_TIME);
                break;
            case TAGS:
                LOGI(TAG, "Starting sessions tag query");
                loader = TagMetadata.createCursorLoader(mContext);
                break;
            case CARDS:
                String currentTime = TimeUtils.getCurrentTime(mContext) + "";
                LOGI(TAG, "Starting cards query: " + currentTime);
                loader = getCursorLoaderInstance(mContext, ScheduleContract.Cards.CONTENT_URI,
                        ExploreQueryEnum.CARDS.getProjection(),
                        " ? > " + ScheduleContract.Cards.DISPLAY_START_DATE + " AND  ? < " +
                                ScheduleContract.Cards.DISPLAY_END_DATE + " AND " +
                                ScheduleContract.Cards.ACTION_TYPE + " IN ('" +
                                EventCard.ACTION_TYPE_LINK + "', '" +
                                EventCard.ACTION_TYPE_MAP + "', '" +
                                EventCard.ACTION_TYPE_SESSION + "')",
                        new String[]{currentTime, currentTime},
                        ScheduleContract.Cards.CARD_ID);
                break;
        }

        return loader;
    }

    @VisibleForTesting
    public CursorLoader getCursorLoaderInstance(Context context, Uri uri, String[] projection,
                                                String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @NonNull
    public EventData getEventData() {
        return mEventData;
    }

    public SessionData getKeynoteData() { return mKeynoteData; }

    public LiveData getLiveData() { return mLiveData; }

    /**
     * Get the list of {@link MessageData} to be displayed to the user, based upon time, location
     * etc.
     *
     * @return messages to be displayed.
     */
    public List<MessageData> getMessages() {
        final List<MessageData> messagesToDisplay = new ArrayList<>();
        if (shouldShowCard(ConfMessageCardUtils.ConfMessageCard.SESSION_NOTIFICATIONS)) {
            messagesToDisplay.add(MessageCardHelper.getNotificationsOptInMessageData());
        }

        if (SettingsUtils.isAttendeeAtVenue(mContext)) {
            if (ConfMessageCardUtils.isConfMessageCardsEnabled(mContext)) {
                LOGD(TAG, "Conf cards Enabled");
                // User has answered they want to opt in AND the message cards are enabled.
                ConfMessageCardUtils.enableActiveCards(mContext);

                // Note that for these special cards, we'll never show more than one at a time
                // to prevent overloading the user with messagesToDisplay.
                // We want each new message to be notable.
                if (shouldShowCard(ConfMessageCardUtils.ConfMessageCard.WIFI_PRELOAD)) {
                    // Check whether a wifi setup card should be offered.
                    if (WiFiUtils.shouldOfferToSetupWifi(mContext, true)) {
                        // Build card asking users whether they want to enable wifi.
                        messagesToDisplay.add(MessageCardHelper.getWifiSetupMessageData());
                        return messagesToDisplay;
                    }
                }

                if (messagesToDisplay.size() < 1) {
                    LOGD(TAG, "Simple cards");
                    List<ConfMessageCardUtils.ConfMessageCard> simpleCards =
                            ConfMessageCardUtils.ConfMessageCard.getActiveSimpleCards(mContext);
                    // Only show a single card at a time.
                    if (simpleCards.size() > 0) {
                        messagesToDisplay.add(MessageCardHelper.getSimpleMessageCardData(
                                simpleCards.get(0)));
                    }
                }
            }
        }
        return messagesToDisplay;
    }

    /**
     * @return the tracks ordered alphabetically. The ordering can only happen if the query {@link
     * com.google.samples.apps.iosched.explore.ExploreIOModel.ExploreIOQueryEnum#TAGS} has returned,
     * which can be checked by calling {@link #getTagMetadata()}.
     */
    public Collection<ItemGroup> getOrderedTracks() {
        if (mOrderedTracks != null) {
            return mOrderedTracks;
        }
        mOrderedTracks = new ArrayList<ItemGroup>(getTracks());
        for (ItemGroup item : mOrderedTracks) {
            if (item.getTitle() == null) {
                item.formatTitle(mTagMetadata);
            }
        }

        // Order the tracks by title.
        Collections.sort(mOrderedTracks, new Comparator<ItemGroup>() {
            @Override
            public int compare(final ItemGroup lhs, final ItemGroup rhs) {
                if (lhs.getTitle() == null) {
                    return 1;
                } else if (rhs.getTitle() == null) {
                    return -1;
                }
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        return mOrderedTracks;
    }

    public TagMetadata getTagMetadata() { return mTagMetadata; }

    @Override
    public void processUserAction(final ExploreUserActionEnum action,
                                  @Nullable final Bundle args, final UserActionCallback callback) {
        /**
         * The only user action in this model fires off a query (using {@link #KEY_RUN_QUERY_ID},
         * so this method isn't used.
         */
    }

    @Override
    public boolean readDataFromCursor(final Cursor cursor, final ExploreQueryEnum query) {
        switch (query) {
            case SESSIONS:
                readDataFromSessionsCursor(cursor);
                return true;
            case TAGS:
                readDataFromTagsCursor(cursor);
                return true;
            case CARDS:
                readDataFromCardsCursor(cursor);
                return true;
        }
        return false;
    }

    private void addPhotoUrlToTopicsAndThemes() {
        if (mTracks != null) {
            for (ItemGroup topic : mTracks.values()) {
                if (mTagMetadata != null && mTagMetadata.getTag(topic.getTitleId()) != null) {
                    topic.setPhotoUrl(mTagMetadata.getTag(topic.getTitleId()).getPhotoUrl());
                }
            }
        }
        if (mThemes != null) {
            for (ItemGroup theme : mThemes.values()) {
                if (mTagMetadata != null && mTagMetadata.getTag(theme.getTitleId()) != null) {
                    theme.setPhotoUrl(mTagMetadata.getTag(theme.getTitleId()).getPhotoUrl());
                }
            }
        }
    }

    private Collection<ItemGroup> getThemes() {
        return mThemes.values();
    }

    private Collection<ItemGroup> getTracks() {
        return mTracks.values();
    }

    /**
     * A session missing title, description, id, or image isn't eligible for the Explore screen.
     */
    private boolean isSessionDataInvalid(SessionData session) {
        return TextUtils.isEmpty(session.getSessionName()) ||
                TextUtils.isEmpty(session.getDetails()) ||
                TextUtils.isEmpty(session.getSessionId());
    }

    private void populateSessionFromCursorRow(SessionData session, Cursor cursor) {
        session.updateData(mContext,
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_TITLE)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_ABSTRACT)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_ID)),
                cursor.getLong(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_START)),
                cursor.getLong(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_END)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_VIDEO_URL)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_TAGS)),
                cursor.getLong(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_STARRED)) == 1L);
    }

    private void readDataFromCardsCursor(Cursor cursor) {
        LOGD(TAG, "Cards query loaded");
        mEventData = new EventData();
        if (cursor != null && cursor.moveToFirst()) {
            LOGD(TAG, "Read card data");
            do {
                EventCard card = EventCard.fromCursorRow(cursor);
                if (card != null) {
                    mEventData.addEventCard(card);
                }
            } while (cursor.moveToNext());
            LOGI(TAG, "Cards loaded: " + mEventData.getCards().size());
        } else {
            LOGE(TAG, "No Cards data");
        }
    }

    /**
     * As we go through the session query results we will be collecting X numbers of session data
     * per Topic and Y numbers of sessions per Theme. When new topics or themes are seen a group
     * will be created.
     * <p/>
     * As we iterate through the list of sessions we are also watching out for the keynote and any
     * live sessions streaming right now.
     */
    private void readDataFromSessionsCursor(Cursor cursor) {
        LOGD(TAG, "Reading session data from cursor.");

        boolean atVenue = SettingsUtils.isAttendeeAtVenue(mContext);

        LiveData liveData = new LiveData();
        Map<String, ItemGroup> trackGroups = new HashMap<>();
        Map<String, ItemGroup> themeGroups = new HashMap<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                SessionData session = new SessionData();
                populateSessionFromCursorRow(session, cursor);

                if (isSessionDataInvalid(session)) {
                    continue;
                }

                if (!atVenue &&
                        (!session.IsLive(mContext)) && !session.isVideoAvailable()) {
                    // Skip the opportunity to present the session for those not on site
                    // since it won't be viewable as there is neither a live stream nor video
                    // available.
                    continue;
                }

                String tags = session.getTags();

                if (session.IsLive(mContext)) {
                    liveData.addSessionData(session);
                }

                if (!TextUtils.isEmpty(tags)) {
                    StringTokenizer tagsTokenizer = new StringTokenizer(tags, ",");
                    while (tagsTokenizer.hasMoreTokens()) {
                        String rawTag = tagsTokenizer.nextToken();
                        if (TagUtils.isTrackTag(rawTag)) {
                            ItemGroup trackGroup = trackGroups.get(rawTag);
                            if (trackGroup == null) {
                                trackGroup = new ItemGroup();
                                trackGroup.setTitleId(rawTag);
                                trackGroup.setId(rawTag);
                                if (mTagMetadata != null && mTagMetadata.getTag(rawTag) != null) {
                                    trackGroup
                                            .setPhotoUrl(mTagMetadata.getTag(rawTag).getPhotoUrl());
                                }
                                trackGroups.put(rawTag, trackGroup);
                            }
                            trackGroup.addSessionData(session);

                        } else if (TagUtils.isThemeTag(rawTag)) {
                            ItemGroup themeGroup = themeGroups.get(rawTag);
                            if (themeGroup == null) {
                                themeGroup = new ItemGroup();
                                themeGroup.setTitleId(rawTag);
                                themeGroup.setId(rawTag);
                                if (mTagMetadata != null && mTagMetadata.getTag(rawTag) != null) {
                                    themeGroup
                                            .setPhotoUrl(mTagMetadata.getTag(rawTag).getPhotoUrl());
                                }
                                themeGroups.put(rawTag, themeGroup);
                            }
                            themeGroup.addSessionData(session);
                        }
                    }
                }
            } while (cursor.moveToNext());
        }

        if (liveData.getSessions().size() > 0) {
            mLiveData = liveData;
        }
        mThemes = themeGroups;
        mTracks = trackGroups;
        mOrderedTracks = null;
    }

    private void readDataFromTagsCursor(Cursor cursor) {
        LOGD(TAG, "TAGS query loaded");
        if (cursor != null && cursor.moveToFirst()) {
            mTagMetadata = new TagMetadata(cursor);
        }

        addPhotoUrlToTopicsAndThemes();
    }

    private void rewriteKeynoteDetails(SessionData keynoteData) {
        long startTime, endTime, currentTime;
        currentTime = TimeUtils.getCurrentTime(mContext);
        if (keynoteData.getStartDate() != null) {
            startTime = keynoteData.getStartDate().getTimeInMillis();
        } else {
            LOGD(TAG, "Keynote start time wasn't set");
            startTime = 0;
        }
        if (keynoteData.getEndDate() != null) {
            endTime = keynoteData.getEndDate().getTimeInMillis();
        } else {
            LOGD(TAG, "Keynote end time wasn't set");
            endTime = Long.MAX_VALUE;
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (currentTime >= startTime && currentTime < endTime) {
            stringBuilder.append(mContext.getString(R.string
                    .live_now));
        } else {
            stringBuilder.append(
                    TimeUtils.formatShortDateTime(mContext, keynoteData.getStartDate().getTime()));
        }
        keynoteData.setDetails(stringBuilder.toString());
    }

    private boolean shouldShowCard(ConfMessageCardUtils.ConfMessageCard card) {
        return ConfMessageCardUtils.shouldShowConfMessageCard(mContext, card) &&
                !ConfMessageCardUtils.hasDismissedConfMessageCard(mContext, card);
    }

    public static enum ExploreQueryEnum implements QueryEnum {
        SESSIONS(0x1, new String[]{
                ScheduleContract.Sessions.SESSION_ID,
                ScheduleContract.Sessions.SESSION_TITLE,
                ScheduleContract.Sessions.SESSION_ABSTRACT,
                ScheduleContract.Sessions.SESSION_TAGS,
                ScheduleContract.Sessions.SESSION_START,
                ScheduleContract.Sessions.SESSION_END,
                ScheduleContract.Sessions.SESSION_VIDEO_URL,
                ScheduleContract.Sessions.SESSION_STARRED,
                ScheduleContract.Sessions.SESSION_START,
                ScheduleContract.Sessions.SESSION_END,
        }),

        TAGS(0x2, new String[]{
                ScheduleContract.Tags.TAG_ID,
                ScheduleContract.Tags.TAG_NAME,
        }),

        CARDS(0x3, new String[]{
                ScheduleContract.Cards.CARD_ID,
                ScheduleContract.Cards.TITLE,
                ScheduleContract.Cards.TEXT_COLOR,
                ScheduleContract.Cards.MESSAGE,
                ScheduleContract.Cards.DISPLAY_END_DATE,
                ScheduleContract.Cards.ACTION_COLOR,
                ScheduleContract.Cards.ACTION_URL,
                ScheduleContract.Cards.ACTION_TEXT,
                ScheduleContract.Cards.BACKGROUND_COLOR,
                ScheduleContract.Cards.DISPLAY_START_DATE,
                ScheduleContract.Cards.ACTION_TYPE,
                ScheduleContract.Cards.ACTION_EXTRA
        });


        private int id;

        private String[] projection;

        ExploreQueryEnum(int id, String[] projection) {
            this.id = id;
            this.projection = projection;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return projection;
        }
    }

    public static enum ExploreUserActionEnum implements UserActionEnum {
        RELOAD(1);

        private int id;

        ExploreUserActionEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

    }
}
