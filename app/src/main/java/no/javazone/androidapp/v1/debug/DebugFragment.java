package no.javazone.androidapp.v1.debug;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import no.javazone.androidapp.v1.R;
import no.javazone.androidapp.v1.debug.actions.ForceSyncNowAction;
import no.javazone.androidapp.v1.debug.actions.ScheduleStarredSessionAlarmsAction;
import no.javazone.androidapp.v1.debug.actions.ShowSessionNotificationDebugAction;
import no.javazone.androidapp.v1.debug.actions.TestScheduleHelperAction;
import no.javazone.androidapp.v1.service.SessionAlarmService;
import no.javazone.androidapp.v1.settings.ConfMessageCardUtils;
import no.javazone.androidapp.v1.ui.activity.ExploreSessionsActivity;
import no.javazone.androidapp.v1.ui.widget.DrawShadowFrameLayout;
import no.javazone.androidapp.v1.util.SettingsUtils;
import no.javazone.androidapp.v1.util.TimeUtils;
import no.javazone.androidapp.v1.util.UIUtils;
import no.javazone.androidapp.v1.util.WiFiUtils;

import static no.javazone.androidapp.v1.util.LogUtils.LOGW;
import static no.javazone.androidapp.v1.util.LogUtils.makeLogTag;

public class DebugFragment extends Fragment {

    private static final String TAG = makeLogTag(DebugFragment.class);

    /**
     * Area of screen used to display log log messages.
     */
    private TextView mLogArea;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.debug_frag, null);
        mLogArea = (TextView) rootView.findViewById(R.id.logArea);
        ViewGroup tests = (ViewGroup) rootView.findViewById(R.id.debug_action_list);
        tests.addView(createTestAction(new ForceSyncNowAction()));
        tests.addView(createTestAction(new TestScheduleHelperAction()));
        tests.addView(createTestAction(new ScheduleStarredSessionAlarmsAction()));
        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(final Context context, final Callback callback) {
                final String sessionId = SessionAlarmService.DEBUG_SESSION_ID;
                final String sessionTitle = "Debugging with Placeholder Text";

                Intent intent = new Intent(
                        SessionAlarmService.ACTION_NOTIFY_SESSION_FEEDBACK,
                        null, context, SessionAlarmService.class);
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_ID, sessionId);
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_START, System.currentTimeMillis()
                        - 30 * 60 * 1000);
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_END, System.currentTimeMillis());
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_TITLE, sessionTitle);
                context.startService(intent);
                Toast.makeText(context, "Showing DEBUG session feedback notification.", Toast.LENGTH_LONG).show();
            }

            @Override
            public String getLabel() {
                return "Show session feedback notification";
            }
        }));
        tests.addView(createTestAction(new ShowSessionNotificationDebugAction()));

        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                SettingsUtils.markTosAccepted(context, false);
                SettingsUtils.markConductAccepted(context, false);
                SettingsUtils.markAnsweredLocalOrRemote(context, false);
                ConfMessageCardUtils.unsetStateForAllCards(context);
            }

            @Override
            public String getLabel() {
                return "Reset Welcome Flags";
            }
        }));
        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                Intent intent = new Intent(context, ExploreSessionsActivity.class);
                intent.putExtra(ExploreSessionsActivity.EXTRA_FILTER_TAG, "TOPIC_ANDROID");
                context.startActivity(intent);
            }

            @Override
            public String getLabel() {
                return "Show Explore Sessions Activity (Android Topic)";
            }
        }));
        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                LOGW(TAG, "Unsetting all Explore message card answers.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(context, null);
                ConfMessageCardUtils.setConfMessageCardsEnabled(context, null);
                ConfMessageCardUtils.unsetStateForAllCards(context);
            }

            @Override
            public String getLabel() {
                return "Unset all Explore based card answers";
            }
        }));
        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfConference(context, -TimeUtils.HOUR * 3);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours before Conf";
            }
        }));
        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfConference(context, -TimeUtils.DAY);
            }

            @Override
            public String getLabel() {
                return "Set time to Day Before Conf";
            }
        }));

        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfConference(context, TimeUtils.HOUR * 3);

                LOGW(TAG, "Unsetting all Explore card answers and settings.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(context, null);
                ConfMessageCardUtils.setConfMessageCardsEnabled(context, null);
                SettingsUtils.markDeclinedWifiSetup(context, false);
                WiFiUtils.uninstallConferenceWiFi(context);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours after Conf start";
            }
        }));
        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfSecondDayOfConference(context,
                        TimeUtils.HOUR * 3);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours after 2nd day start";
            }
        }));
        tests.addView(createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToEndOfConference(context, TimeUtils.HOUR * 3);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours after Conf end";
            }
        }));

        return rootView;
    }

    protected View createTestAction(final DebugAction test) {
        Button testButton = new Button(this.getActivity());
        testButton.setText(test.getLabel());
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long start = System.currentTimeMillis();
                mLogArea.setText("");
                test.run(view.getContext(), new DebugAction.Callback() {
                    @Override
                    public void done(boolean success, String message) {
                        logTimed((System.currentTimeMillis() - start),
                                (success ? "[OK] " : "[FAIL] ") + message);
                    }
                });
            }
        });
        return testButton;
    }

    protected void logTimed(long time, String message) {
        message = "[" + time + "ms] " + message;
        Log.d(TAG, message);
        mLogArea.append(message + "\n");
    }

    private void setContentTopClearance(int clearance) {
        if (getView() != null) {
            getView().setPadding(getView().getPaddingLeft(), clearance,
                    getView().getPaddingRight(), getView().getPaddingBottom());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int actionBarSize = UIUtils.calculateActionBarSize(getActivity());
        DrawShadowFrameLayout drawShadowFrameLayout =
                (DrawShadowFrameLayout) getActivity().findViewById(R.id.main_content);
        if (drawShadowFrameLayout != null) {
            drawShadowFrameLayout.setShadowTopOffset(actionBarSize);
        }
        setContentTopClearance(actionBarSize
                + getResources().getDimensionPixelSize(R.dimen.explore_grid_padding));
    }
}