package yv.jot.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

import yv.jot.MainActivity;
import yv.jot.Models.Alarm;
import yv.jot.Properties.Constants;
import yv.jot.Properties.Defaults;
import yv.jot.Properties.Prefs;
import yv.jot.R;
import yv.jot.common.RingType;
import yv.jot.db.DbManager;
import yv.jot.location.TrafficTimeCalculatorTask;
import yv.jot.utils.AlarmManagerHelper;
import yv.jot.utils.PreferenceUtils;

public class AlarmScreenActivity extends ActionBarActivity {

    public final String TAG = this.getClass().getSimpleName();

    private PowerManager.WakeLock mWakeLock;
    private MediaPlayer mPlayer;

    private static final int WAKELOCK_TIMEOUT = 60 * 1000;

    private ImageButton confirmButton = null;
    private ImageButton snoozeButton = null;
    private ImageButton alarmBell = null;
    private TextView tvTime = null;
    private long alarmId = -1;
    private  Alarm alarm;
    private TextView trafficTime = null;
    private TextView subject = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_screen);

        DbManager.initialize(this);

        this.alarmId = getIntent().getLongExtra(Constants.ALARM_ID_EXTRA, -1);
        String extra = getIntent().getStringExtra(Constants.TOTAL_TIME_EXTRA);
        String destination = getIntent().getStringExtra(Constants.TO_ADDRESS_EXTRA);
        RingType ringType = RingType.formCode(getIntent().getIntExtra(Constants.RING_TYPE, 0));

        if(alarmId != -1) {
            this.alarm = DbManager.instance().getAlarm((int) alarmId);
        }

        this.alarmBell = (ImageButton)findViewById(R.id.alarm_notification_sound);

        tvTime = (TextView) findViewById(R.id.alarm_ring_time);
        Calendar now = Calendar.getInstance();
        tvTime.setText(String.format("%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)));

        subject = (TextView) findViewById(R.id.alarm_screen_subject);

        trafficTime = (TextView) findViewById(R.id.alarm_traffic_time_value);

        alarmBell.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlayer.stop();
            }
        });

        snoozeButton = (ImageButton) findViewById(R.id.alarm_snooze_button);
        confirmButton = (ImageButton) findViewById(R.id.alarm_confirm_button);

        if(ringType == RingType.GET_READY && alarm.getExtra() > 0) {
            initGetReadyScreen();
        }
        else {
            initGoOutScreen();
        }

        //Play alarm tone
        mPlayer = new MediaPlayer();
        try {
            Uri toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if (toneUri != null) {
                mPlayer.setDataSource(this, toneUri);
                mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mPlayer.setLooping(true);
                mPlayer.prepare();
                mPlayer.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Ensure wakelock release
        Runnable releaseWakelock = new Runnable() {

            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
        };

        new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);

        TrafficTimeCalculatorTask tc = new TrafficTimeCalculatorTask(this, alarm, trafficTime);
        tc.execute(alarm);
    }

    private void initGetReadyScreen() {

        subject.setText(R.string.alarm_go_get_ready);

        confirmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlayer.stop();

                PendingIntent pIntent = AlarmManagerHelper.createPendingIntent(AlarmScreenActivity.this, alarm, RingType.GO_OUT);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(alarm.getTime().split(":")[0]));
                cal.set(Calendar.MINUTE, Integer.valueOf(alarm.getTime().split(":")[1]));
                cal.set(Calendar.SECOND, 0);
                AlarmManagerHelper.cancelAlarm(AlarmScreenActivity.this, alarm);
                AlarmManagerHelper.setAlarm(AlarmScreenActivity.this, cal, pIntent, alarm);

                finish();

//                Intent intent = new Intent(AlarmScreenActivity.this, MainActivity.class);
//                setResult(RESULT_OK, intent);
//
//                if (alarm != null) {
//                    alarm.setActive(false);
//                    DbManager.instance().updateAlarm(alarm);
//                }
//
//                AlarmScreenActivity.this.startActivityForResult(intent, Constants.ALARMS_VIEW_REQUEST);
            }
        });

        snoozeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlayer.stop();

                Intent intent = new Intent(AlarmScreenActivity.this, MainActivity.class);
                setResult(RESULT_OK, intent);

                if(alarm != null) {
                    int hours = Integer.valueOf(alarm.getTime().split(":")[0]);
                    int minutes = Integer.valueOf(alarm.getTime().split(":")[1]);

                    Calendar alarmCalendar = Calendar.getInstance();
                    alarmCalendar.set(Calendar.HOUR_OF_DAY, hours);
                    alarmCalendar.set(Calendar.MINUTE, minutes);
                    alarmCalendar.set(Calendar.SECOND, 00);

                    alarmCalendar.add(Calendar.MINUTE, Integer.valueOf(PreferenceUtils.readPreferenceValue(getApplicationContext(), Prefs.SNOOZE_DELAY, Defaults.SNOOZE_DELAY)));

                    int updatedHour = alarmCalendar.get(Calendar.HOUR_OF_DAY);
                    int updatedMinutes = alarmCalendar.get(Calendar.MINUTE);

                    String updatedHourStr = String.valueOf(updatedHour);
                    updatedHourStr = updatedHour < 10 ? "0" + updatedHourStr : updatedHourStr;

                    String updatedMinutesStr = String.valueOf(updatedMinutes);
                    updatedMinutesStr = updatedMinutes < 10 ? "0" + updatedMinutesStr : updatedMinutesStr;

                    alarm.setTime(updatedHourStr + ":" + updatedMinutesStr);

                    DbManager.instance().updateAlarm(alarm);
                }

                AlarmScreenActivity.this.startActivityForResult(intent, Constants.ALARMS_VIEW_REQUEST);
            }
        });

    }

    private void initGoOutScreen() {

        snoozeButton.setVisibility(View.INVISIBLE);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(confirmButton.getLayoutParams());

        params.setGravity(Gravity.CENTER_HORIZONTAL);

        confirmButton.setLayoutParams(params);

        subject.setText(R.string.alarm_time_to_go_out);

        confirmButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mPlayer.stop();

                Intent intent = new Intent(AlarmScreenActivity.this, MainActivity.class);
                setResult(RESULT_OK, intent);

                if (alarm != null) {
                    alarm.setActive(false);
                    DbManager.instance().updateAlarm(alarm);
                }

                AlarmScreenActivity.this.startActivityForResult(intent, Constants.ALARMS_VIEW_REQUEST);
            }
        });

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        // Set the window to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Acquire wakelock
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
        }

        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.i(TAG, "Wakelock aquired!!");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
