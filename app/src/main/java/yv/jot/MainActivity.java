package yv.jot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import yv.jot.Models.Alarm;
import yv.jot.Properties.Constants;
import yv.jot.activities.AddAlarmActivity;
import yv.jot.activities.SettingsActivity;
import yv.jot.db.DbManager;
import yv.jot.utils.AlarmManagerHelper;
import yv.jot.utils.AlarmsManager;
import yv.jot.utils.LocationsUtils;


public class MainActivity extends AppCompatActivity {

    private static long DEFAULT = -1;

    private Bundle savedInstanceState = null;
    private long selectedAlarmContextMenu = DEFAULT;

    // Declaring Your View and Variables
    private Toolbar toolbar;
//    private ViewPager pager;
//    private ViewPagerAdapter adapter;
//    private SlidingTabLayout tabs;
//    private CharSequence Titles[] = {"Alarms", "Settings"};
//    private int Numboftabs = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DbManager.initialize(this);

        this.savedInstanceState = savedInstanceState;

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

//        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);
//
//        pager = (ViewPager) findViewById(R.id.pager);
//        pager.setAdapter(adapter);
//
//        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
//        tabs.setDistributeEvenly(true);
//
//        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//            @Override
//            public int getIndicatorColor(int position) {
//                return getResources().getColor(R.color.TabsScrollColor);
//            }
//        });
//
//        // Setting the ViewPager For the SlidingTabsLayout
//        tabs.setViewPager(pager);
//
//        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener(){
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//
//            }
//        };
//        tabs.setOnPageChangeListener(pageChangeListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, Constants.SETTINGS_REQUEST);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.ADD_ALARM_REQUEST) {
                int extraMinutes = data.getIntExtra(Constants.TOTAL_TIME_EXTRA, 0);
                String to = data.getStringExtra(Constants.TO_ADDRESS_EXTRA);
                String time = data.getStringExtra(Constants.TIME_EXTRA);
                long currentAlarmId = data.getLongExtra(Constants.ALARM_ID_EXTRA, DEFAULT);

                Alarm alarm = new Alarm(time, to, extraMinutes, true);
                alarm.setActive(true);
                alarm.setTime(time);
                alarm.setDestination(to);
                alarm.setExtra(extraMinutes);
                alarm.setID(currentAlarmId);
                
                alarm = addLongLatToAlarm(alarm);

                if(alarm == null) {
                    Toast.makeText(getApplicationContext(), "Unable to get location details", Toast.LENGTH_LONG).show();
                    return;
                }

                if(currentAlarmId == DEFAULT) {
                    DbManager.instance().insertAlarm(alarm);
                }
                else {
                    DbManager.instance().updateAlarm(alarm);
                }
            }
            else if (requestCode == Constants.SETTINGS_REQUEST) {

            }
        }
    }

    private Alarm addLongLatToAlarm(Alarm alarm) {

        Pair<Double, Double> coord = LocationsUtils.instance().getAddressCoordinates(getApplicationContext(), alarm.getDestination());

        if(coord == null) {
            return null;
        }

        alarm.setLongtitude(coord.first);
        alarm.setLatitude(coord.second);

        return alarm;
    }


    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

//        ContextMenu ctxMenu = menu;

//        AdapterView.AdapterContextMenuInfo aInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        selectedAlarmContextMenu = v.getId();

        menu.setHeaderTitle("Choose Action");
        menu.add(1, 1, 1, "Edit Alarm");
        menu.add(1, 2, 2, "Delete Alarm");
        menu.add(1, 3, 3, "Cancel");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(selectedAlarmContextMenu == DEFAULT) {
            return false;
        }

        // case edit alarm selected
        if(item.getItemId() == 1) {
            Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
            intent.putExtra(Constants.ALARM_ID_EXTRA, selectedAlarmContextMenu);
            startActivityForResult(intent, Constants.ADD_ALARM_REQUEST);
        }
        // case delete alarm selected
        else if(item.getItemId() == 2) {
            Alarm alarm = DbManager.instance().getAlarm((int)selectedAlarmContextMenu);
            if(alarm != null) {
                AlarmManagerHelper.cancelAlarm(this, alarm);
                DbManager.instance().deleteAlarm(selectedAlarmContextMenu);
                View row = findViewById((int) selectedAlarmContextMenu);
                if (row != null) {

                    ViewGroup parent = ((ViewGroup) row.getParent());
                    parent.removeView(row);
                    Toast.makeText(getApplicationContext(), "Alarm Removed", Toast.LENGTH_SHORT).show();

                    if (parent.getChildCount() == 0) {
                        AlarmsManager.instance().addEmptyNoteToAlarmList(parent);
                    }
                }
                AlarmManagerHelper.setAlarms(this);
            }
        }
        // case cancel selected
        else if(item.getItemId() == 3) {
            closeContextMenu();
        }

        selectedAlarmContextMenu = DEFAULT;

        return true;
    }

//    public void setCurrentTab(int tab) {
//        pager.setCurrentItem(tab, true);
//    }
}
