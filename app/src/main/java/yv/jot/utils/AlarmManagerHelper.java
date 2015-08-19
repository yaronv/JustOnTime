package yv.jot.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import java.util.Calendar;
import java.util.List;

import yv.jot.Models.Alarm;
import yv.jot.Properties.Constants;
import yv.jot.Properties.Defaults;
import yv.jot.R;
import yv.jot.common.RingType;
import yv.jot.db.DbManager;
import yv.jot.location.AlarmScheduler;
import yv.jot.services.AlarmService;
import yv.jot.services.LocationCheckerService;

public class AlarmManagerHelper extends BroadcastReceiver {

    private static String CHECKER_SERVICE = "alarm_checked";
    private static int CHECKER_SERVICE_ID = 999;

    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context);
    }

    public static void setAlarms(Context context) {
        cancelAlarms(context);

        List<Alarm> alarms =  DbManager.instance().getAllAlarms();

        boolean hasActiveAlarm = false;
        for (Alarm alarm : alarms) {
            if (alarm.isActive()) {
                hasActiveAlarm = true;

                AlarmScheduler tc = new AlarmScheduler(context, alarm);
                tc.execute(alarm);
            }
            else {
                setInactiveAlarm(context, alarm);
            }
        }

        // set the updater intent
//        if(hasActiveAlarm) {
//            PendingIntent intent = createAlarmUpdaterIntent(context);
//            setUpdater(context, intent);
//        }
//        else {
//            PendingIntent intent = createAlarmUpdaterIntent(context);
//            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            alarmManager.cancel(intent);
//        }
    }

    private static void setInactiveAlarm(Context context, Alarm alarm) {
        View holder = ((Activity)context).findViewById(R.id.alarms_holder);

        AlarmsManager.instance().setInactiveAlarm(alarm, holder);
    }

    @SuppressLint("NewApi")
    private static void setUpdater(Context context, PendingIntent pIntent) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, Defaults.UPDATER_INTERVAL_MINUTES);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    @SuppressLint("NewApi")
    public static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent, Alarm alarm) {

        View holder = null;

//        if(context instanceof Activity) {
            holder = ((Activity)context).findViewById(R.id.alarms_holder);
//        }


        AlarmsManager.instance().updateAlarmWakeupTime(alarm, holder, calendar);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }

    public static PendingIntent createPendingIntent(Context context, Alarm model, RingType ringType) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(Constants.ALARM_ID_EXTRA, model.getID());
        intent.putExtra(Constants.TO_ADDRESS_EXTRA, model.getDestination());
        intent.putExtra(Constants.TIME_EXTRA, model.getTime());
        intent.putExtra(Constants.TOTAL_TIME_EXTRA, model.getExtra());
        intent.putExtra(Constants.RING_TYPE, ringType);

        return PendingIntent.getService(context, (int) model.getID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public static void cancelAlarms(Context context) {
        List<Alarm> alarms =  DbManager.instance().getAllAlarms();
        if (alarms != null) {
            for (Alarm alarm : alarms) {
                if (alarm.isActive()) {
                    cancelAlarm(context, alarm);
                }
            }
        }
    }

    private static PendingIntent createAlarmUpdaterIntent(Context context) {
        Intent intent = new Intent(context, LocationCheckerService.class);

        return PendingIntent.getService(context, CHECKER_SERVICE_ID, intent, PendingIntent.FLAG_NO_CREATE);
    }

    public static void cancelAlarm(Context context, Alarm alarm) {
        PendingIntent pIntent = createPendingIntent(context, alarm, null);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);
    }
}
