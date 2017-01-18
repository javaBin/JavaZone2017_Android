/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.javazone.sync.handler;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;

import no.javazone.archframework.model.domain.Tag;
import no.javazone.database.ScheduleContract;
import no.javazone.database.ScheduleContractHelper;
import no.javazone.schedule.JsonHandler;

import static no.javazone.util.LogUtils.makeLogTag;

public class TagsHandler extends JsonHandler {
    private static final String TAG = makeLogTag(TagsHandler.class);

    private HashMap<String, Tag> mTags = new HashMap<String, Tag>();

    public TagsHandler(Context context) {
        super(context);
    }

    @Override
    public void process(JsonElement element) {
        for (Tag tag : new Gson().fromJson(element, Tag[].class)) {
            mTags.put(tag.tag, tag);
        }
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Tags.CONTENT_URI);

        // since the number of tags is very small, for simplicity we delete them all and reinsert
        list.add(ContentProviderOperation.newDelete(uri).build());
        for (Tag tag : mTags.values()) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
            builder.withValue(ScheduleContract.Tags.TAG_ID, tag.tag);
            builder.withValue(ScheduleContract.Tags.TAG_CATEGORY, tag.category);
            builder.withValue(ScheduleContract.Tags.TAG_NAME, tag.name);
            builder.withValue(ScheduleContract.Tags.TAG_ORDER_IN_CATEGORY, tag.order_in_category);
            builder.withValue(ScheduleContract.Tags.TAG_ABSTRACT, tag._abstract);
            builder.withValue(ScheduleContract.Tags.TAG_COLOR, tag.color == null ? 0
                    : Color.parseColor(tag.color));
            builder.withValue(ScheduleContract.Tags.TAG_PHOTO_URL, tag.photoUrl);
            list.add(builder.build());
        }
    }

    public HashMap<String, Tag> getTagMap() {
        return mTags;
    }
}
