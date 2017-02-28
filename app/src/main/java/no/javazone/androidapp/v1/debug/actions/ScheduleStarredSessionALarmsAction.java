package no.javazone.androidapp.v1.debug.actions;

import android.content.Context;
import android.content.Intent;

import no.javazone.androidapp.v1.debug.DebugAction;
import no.javazone.androidapp.v1.service.SessionAlarmService;

public class ScheduleStarredSessionAlarmsAction implements DebugAction {

    @Override
    public void run(Context context, Callback callback) {
        Intent intent = new Intent(
                SessionAlarmService.ACTION_SCHEDULE_ALL_STARRED_BLOCKS,
                null, context, SessionAlarmService.class);
        context.startService(intent);
    }

    @Override
    public String getLabel() {
        return "Schedule session notifications";
    }
}
