package no.javazone.androidapp.v1.testutils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.javazone.androidapp.v1.archframework.model.Model;
import no.javazone.androidapp.v1.injection.ModelProvider;
import no.javazone.androidapp.v1.settings.ConfMessageCardUtils;
import no.javazone.androidapp.v1.util.SettingsUtils;

public class BaseActivityTestRule<T extends Activity> extends IntentsTestRule<T> {
    private Model mModel;

    public BaseActivityTestRule(final Class<T> activityClass) {
        super(activityClass);
    }

    public BaseActivityTestRule(final Class<T> activityClass, Model model) {
        super(activityClass);
        mModel = model;
    }

    @Override
    protected void beforeActivityLaunched() {
        ModelProvider.setStubModel(mModel);
    }

    private void bypassTOsAndConduct() {
        SettingsUtils.markTosAccepted(InstrumentationRegistry.getTargetContext(), true);
        SettingsUtils.markConductAccepted(InstrumentationRegistry.getTargetContext(), true);
    }


    private void disableConferenceMessages() {
        ConfMessageCardUtils
                .markAnsweredConfMessageCardsPrompt(InstrumentationRegistry.getTargetContext(),
                        true);
    }
}
