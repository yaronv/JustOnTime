package yv.jot.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import yv.jot.db.DbManager;
import yv.jot.utils.AlarmManagerHelper;

public class LocationCheckerService extends Service {

    public static String TAG = LocationCheckerService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        DbManager.initialize(this);

        AlarmManagerHelper.setAlarms(this);

        return super.onStartCommand(intent, flags, startId);
    }
}
