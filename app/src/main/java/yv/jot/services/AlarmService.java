package yv.jot.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import yv.jot.activities.AlarmScreenActivity;
import yv.jot.db.DbManager;

public class AlarmService extends Service {

    public static String TAG = AlarmService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DbManager.initialize(this);
        Intent alarmIntent = new Intent(getBaseContext(), AlarmScreenActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtras(intent);
        getApplication().startActivity(alarmIntent);

        return super.onStartCommand(intent, flags, startId);
    }
}
