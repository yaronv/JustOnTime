package yv.jot.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import yv.jot.exceptions.NotValidException;


public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static AppDatabaseHelper sInstance;

    public static final String TABLE_ALARMS = "alarms";

    public static final String COL_ID = BaseColumns._ID;

    public static final String COL_TIME = "time";

    public static final String COL_DESTINATION = "destination";

    public static final String COL_EXTRA = "extra";

    public static final String COL_ACTIVE = "active";

    public static final String COL_LONGTITUDE = "longtitude";

    public static final String COL_LATITUDE = "latitude";

    private static final String DATABASE_NAME = "my_app.db";

    private static final int DATABASE_VERSION = 5;

    public static synchronized AppDatabaseHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new AppDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private AppDatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override

    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_ALARMS + " ("

                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

                + COL_TIME + " TEXT NOT NULL, "

                + COL_DESTINATION + " TEXT NOT NULL, "

                + COL_EXTRA + " INTEGER NOT NULL, "

                + COL_ACTIVE + " INTEGER NOT NULL, "

                + COL_LONGTITUDE + " REAL NOT NULL, "

                + COL_LATITUDE + " REAL NOT NULL"

                + ");");

    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS + ";");

        onCreate(db);

    }

    public long insert(String tableName, ContentValues values) throws NotValidException {

        validate(values);

        SQLiteDatabase db = getWritableDatabase();

        long id = db.insert(tableName, null, values);

        db.close();

        return id;

    }

    public int update(String tableName, long id, ContentValues values) throws NotValidException {

        validate(values);

        String selection = COL_ID + " = ?";

        String[] selectionArgs = {String.valueOf(id)};

        SQLiteDatabase db = getWritableDatabase();

        int ret = db.update(tableName, values, selection, selectionArgs);

        db.close();

        return ret;

    }

    public int delete(String tableName, long id) {

        String selection = COL_ID + " = ?";

        String[] selectionArgs = {String.valueOf(id)};

        SQLiteDatabase db = getWritableDatabase();

        int ret = db.delete(tableName, selection, selectionArgs);

        db.close();

        return ret;

    }

    protected void validate(ContentValues values) throws NotValidException {

        if ((!values.containsKey(COL_TIME) || values.getAsString(COL_TIME) == null || values.getAsString(COL_TIME).isEmpty()) ||
            (!values.containsKey(COL_DESTINATION) || values.getAsString(COL_DESTINATION) == null || values.getAsString(COL_DESTINATION).isEmpty()) ||
            (!values.containsKey(COL_EXTRA) || values.getAsString(COL_EXTRA) == null || values.getAsString(COL_EXTRA).isEmpty())) {

            throw new NotValidException("Error: Fields must be set");

        }

    }

    public Cursor query(SQLiteDatabase db, String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderedBy) {

        if(columns == null) {
            columns = new String[]{COL_ID, COL_TIME, COL_DESTINATION, COL_EXTRA, COL_ACTIVE, COL_LONGTITUDE, COL_LATITUDE};
        }

        Cursor c = db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderedBy);

        return c;
    }
}
