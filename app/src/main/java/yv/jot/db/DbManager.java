package yv.jot.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import yv.jot.Models.Alarm;
import yv.jot.exceptions.NotValidException;

public class DbManager {

    private static DbManager instance = null;

    private static Context context;

    public static DbManager instance() {
        if(instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    public static Context getContext() {
        return context;
    }

    public static void initialize(Context c) {
        context = c;
    }

    private DbManager() {

    }

    public long insertAlarm(Alarm alarm) {
        try {

            ContentValues values = getContentValues(alarm);

            long id = AppDatabaseHelper.getInstance(context).insert(AppDatabaseHelper.TABLE_ALARMS, values);

            return id;

        } catch (NotValidException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Alarm getAlarm(int id) {

        SQLiteDatabase db = AppDatabaseHelper.getInstance(context).getReadableDatabase();

        Cursor c = AppDatabaseHelper.getInstance(context).query(db, AppDatabaseHelper.TABLE_ALARMS, null, AppDatabaseHelper.COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, AppDatabaseHelper.COL_ID);

        c.moveToFirst();

        Alarm alarm = createAlarmFromCursor(c);

        c.close();

        db.close();

        return alarm;
    }

    public int updateAlarm(Alarm alarm) {

        ContentValues values = getContentValues(alarm);

        try {
            return AppDatabaseHelper.getInstance(context).update(AppDatabaseHelper.TABLE_ALARMS, alarm.getID(), values);
        } catch (NotValidException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int deleteAlarm(long id) {
        return AppDatabaseHelper.getInstance(context).delete(AppDatabaseHelper.TABLE_ALARMS, id);
    }

    private ContentValues getContentValues(Alarm alarm) {
        ContentValues values = new ContentValues(4);

        values.put(AppDatabaseHelper.COL_EXTRA, alarm.getExtra());
        values.put(AppDatabaseHelper.COL_ACTIVE, alarm.isActive() ? 1 : 0);

        values.put(AppDatabaseHelper.COL_TIME, alarm.getTime());
        values.put(AppDatabaseHelper.COL_DESTINATION, alarm.getDestination());
        values.put(AppDatabaseHelper.COL_LONGTITUDE, alarm.getLongtitude());
        values.put(AppDatabaseHelper.COL_LATITUDE, alarm.getLatitude());

        return values;
    }

    public List<Alarm> getAllAlarms() {
        List<Alarm> alarmsList = new ArrayList<Alarm>();

        SQLiteDatabase db = AppDatabaseHelper.getInstance(context).getReadableDatabase();

        Cursor cursor = AppDatabaseHelper.getInstance(context).query(db, AppDatabaseHelper.TABLE_ALARMS, null, null, null, null, null, AppDatabaseHelper.COL_ID);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Alarm a = createAlarmFromCursor(cursor);
                // Adding alarm to list
                alarmsList.add(a);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return alarmsList;
    }

    private Alarm createAlarmFromCursor(Cursor c) {
        Alarm alarm = new Alarm();
//        c.moveToFirst();
        alarm.setID(Long.parseLong(c.getString(0)));
        alarm.setTime(c.getString(1));
        alarm.setDestination(c.getString(2));
        alarm.setExtra(c.getInt(3));

        int activeIntVal = c.getInt(4);
        alarm.setActive(activeIntVal == 0 ? false : true);

        alarm.setLongtitude(c.getDouble(5));
        alarm.setLatitude(c.getDouble(6));

        return alarm;
    }
}
