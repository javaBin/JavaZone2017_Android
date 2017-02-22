/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package no.javazone.androidapp.v1.archframework.model;

import android.os.Bundle;
import android.support.annotation.Nullable;

import no.javazone.androidapp.v1.archframework.presenter.Presenter;
import no.javazone.androidapp.v1.database.QueryEnum;
import no.javazone.androidapp.v1.archframework.view.UserActionEnum;

public interface Model<Q extends QueryEnum, UA extends UserActionEnum> {

    /**
     * @return an array of {@link QueryEnum} that can be processed by the model
     */
    public Q[] getQueries();

    /**
     * @return an array of {@link UserActionEnum} that can be processed by the model
     */
    public UA[] getUserActions();

    /**
     * Delivers a user {@code action} and associated {@code args} to the Model, which typically will
     * run a data update. The Model then notify the {@link Presenter} it is done with the user
     * action via the {@code callback}.
     * <p/>
     * Add the constants used to store values in the bundle to the Model implementation class as
     * final static protected strings.
     */
    public void deliverUserAction(UA action, @Nullable Bundle args, UserActionCallback callback);

    /**
     * Requests the Model to load data for the given {@code query}, then notify the data query was
     * completed via the {@code callback}. Typically, this is called to initialise the model with
     * the data needed to display the UI when loading.
     */
    public void requestData(Q query, DataQueryCallback callback);

    public void cleanUp();

    /**
     * A callback used to notify the {@link Presenter} that the update for a given {@link QueryEnum}
     * has completed, either successfully or with error.
     */
    public interface DataQueryCallback<M extends Model, Q extends QueryEnum> {

        public void onModelUpdated(M model, Q query);

        public void onError(Q query);
    }

    /**
     * A callback used to notify the {@link Presenter} that the update for a given {@link
     * UserActionEnum} has completed, either successfully or with error.
     */
    public interface UserActionCallback<M extends Model, UA extends UserActionEnum> {

        public void onModelUpdated(M model, UA userAction);

        public void onError(UA userAction);

    }
}
