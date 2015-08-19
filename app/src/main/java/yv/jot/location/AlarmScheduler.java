package yv.jot.location;

import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Calendar;

import yv.jot.Models.Alarm;
import yv.jot.common.RingType;
import yv.jot.utils.AlarmManagerHelper;
import yv.jot.utils.TrafficUtils;

public class AlarmScheduler extends AsyncTask<Alarm, Void, Calendar> {

    private final Context context;
    private final Alarm alarm;

    public AlarmScheduler(Context context, Alarm alarm) {
        this.context = context;
        this.alarm = alarm;
    }

    @Override
    protected Calendar doInBackground(Alarm... params) {

        Alarm alarm = params[0];

        // set initial Calendar with alarm values
        Calendar calendar = Calendar.getInstance();
        int hours = Integer.parseInt(alarm.getTime().split(":")[0]);
        int minutes = Integer.parseInt(alarm.getTime().split(":")[1]);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 00);


        // decrease the get ready time
        calendar.add(Calendar.MINUTE, alarm.getExtra() * -1);

        // decrease the calculated driving time
        int drivingTime = TrafficUtils.instance().getDrivingTime(context, alarm);
        calendar.add(Calendar.MINUTE, drivingTime * -1);

        Calendar now = Calendar.getInstance();

        // if the alarm time is before the current time, we set it for the next day
        if(calendar.before(now)) {
            calendar.add(Calendar.DATE, 1);
        }

        return calendar;
    }

    @Override
    protected void onPreExecute() {}


    @Override
    protected void onPostExecute(Calendar result) {
        PendingIntent pIntent = AlarmManagerHelper.createPendingIntent(context, alarm, RingType.GET_READY);

        AlarmManagerHelper.setAlarm(context, result, pIntent, alarm);
    }

    @Override
    protected void onProgressUpdate(Void... values) {}

}