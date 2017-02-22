package no.javazone.androidapp.v1.explore;

import android.os.Parcel;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.javazone.androidapp.v1.Config;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TagFilterHolderTest {
    private final static String TAG_ID_1 = "TAG_ID_1";

    private final static String TAG_ID_2 = "TAG_ID_2";

    private final static String TAG_ID_3 = "TAG_ID_3";

    private TagFilterHolder mTagFilterHolder;

    private Parcel mParcel;

    private TagFilterHolder mReadTagFilterHolder;

    @Before
    public void setUp() {
        mTagFilterHolder = new TagFilterHolder();
    }

    @Test
    public void writeToParcel_NonEmptyValues_readFromParcel() {
        mTagFilterHolder.add(TAG_ID_1, Config.Tags.CATEGORY_THEME);
        mTagFilterHolder.add(TAG_ID_2, Config.Tags.CATEGORY_THEME);
        mTagFilterHolder.add(TAG_ID_3, Config.Tags.CATEGORY_TRACK);

        writeTagFilterHolderToParcel();
        createTagFilterHolderFromParcel();
        assertThat(mReadTagFilterHolder.getSelectedFilters().size(), is(3));
        assertTrue(mReadTagFilterHolder.getSelectedFilters().contains(TAG_ID_1));
        assertTrue(mReadTagFilterHolder.getSelectedFilters().contains(TAG_ID_2));
        assertTrue(mReadTagFilterHolder.getSelectedFilters().contains(TAG_ID_3));
        assertThat(mReadTagFilterHolder.getCategoryCount(), is(2));
        assertThat(mReadTagFilterHolder.getCountByCategory(Config.Tags.CATEGORY_THEME), is(2));
        assertThat(mReadTagFilterHolder.getCountByCategory(Config.Tags.CATEGORY_TRACK), is(1));
        assertTrue(mReadTagFilterHolder.isShowLiveStreamedSessions());

    }

    @Test
    public void writeToParcel_EmptyValues_readFromParcel() {
        writeTagFilterHolderToParcel();

        createTagFilterHolderFromParcel();
        assertThat(mReadTagFilterHolder.getSelectedFilters().size(), is(0));
        assertThat(mReadTagFilterHolder.getCategoryCount(),
                is(1));
    }

    @Test
    public void add_invalidCategory_DoesNotCrash() {
        mTagFilterHolder.add("tag", "invalid category");

        assertThat(mTagFilterHolder.getSelectedFilters().size(), is(0));
        assertThat(mTagFilterHolder.getCategoryCount(),
                is(1));
    }

    private void writeTagFilterHolderToParcel() {
        mParcel = Parcel.obtain();
        mTagFilterHolder.writeToParcel(mParcel, 0);
    }

    private void createTagFilterHolderFromParcel() {
        mParcel.setDataPosition(0);
        mReadTagFilterHolder = (TagFilterHolder) TagFilterHolder.CREATOR.createFromParcel(mParcel);
    }
}
