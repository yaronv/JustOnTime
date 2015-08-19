package yv.jot.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import yv.jot.Models.Alarm;
import yv.jot.R;
import yv.jot.db.DbManager;

/**
 * Created by yaron on 26/07/15.
 */
public class AlarmsManager {

    private static AlarmsManager instance = null;

    public static AlarmsManager instance() {
        if(instance == null) {
            instance = new AlarmsManager();
        }
        return instance;
    }

    private AlarmsManager() {

    }

    public void updateAlarmWakeupTime(Alarm alarm, View holder, Calendar calendar) {
        if(holder == null) {
            return;
        }

        View alarmView = holder.findViewById((int) alarm.getID());

        if(alarmView == null) {
            return;
        }

        TextView wakeupTime = (TextView)alarmView.findViewById(R.id.alarm_item_time_wakeup);

        if(wakeupTime == null) {
            return;
        }


        wakeupTime.setText(String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));
    }

    public View addAlarmToList(Alarm alarm, TableLayout holder) {
        LayoutInflater layoutInflater = LayoutInflater.from(DbManager.getContext());

        View inflatedView = holder.findViewById((int) alarm.getID());

        View inflatedLineSep = null;

        boolean exists = true;

        if(inflatedView == null) {
            inflatedView = layoutInflater.inflate(R.layout.alarm_list_item, holder, false);
            inflatedView.setId((int) alarm.getID());

            inflatedLineSep = layoutInflater.inflate(R.layout.alarm_list_item_separator, holder, false);

            exists = false;
        }

        TextView destinationElm = (TextView)inflatedView.findViewById(R.id.alarm_item_destination);
        TextView timeElm = (TextView)inflatedView.findViewById(R.id.alarm_item_time);


        destinationElm.setText(alarm.getDestination());
        timeElm.setText(alarm.getTime());


        ImageView sign = (ImageView) inflatedView.findViewById(R.id.alarm_sign);

        if (alarm.isActive()) {
            sign.setImageDrawable(DbManager.getContext().getResources().getDrawable(R.drawable.ic_notifications_white_24dp));
        } else {
            sign.setImageDrawable(DbManager.getContext().getResources().getDrawable(R.drawable.ic_notifications_off_white_24dp));
        }

        if(!exists) {
            holder.addView(inflatedView);
            holder.addView(inflatedLineSep);

            inflatedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int id = v.getId();

                    Alarm alarm = DbManager.instance().getAlarm(id);

                    // switch active state
                    alarm.setActive(!alarm.isActive());

                    DbManager.instance().updateAlarm(alarm);

                    ImageView sign = (ImageView) v.findViewById(R.id.alarm_sign);

                    if (alarm.isActive()) {
                        sign.setImageDrawable(DbManager.getContext().getResources().getDrawable(R.drawable.ic_notifications_white_24dp));
                        Toast.makeText(DbManager.getContext(), "Alarm enabled", Toast.LENGTH_SHORT).show();
                    } else {
                        sign.setImageDrawable(DbManager.getContext().getResources().getDrawable(R.drawable.ic_notifications_off_white_24dp));
                        AlarmManagerHelper.cancelAlarm(DbManager.getContext(), alarm);
                        Toast.makeText(DbManager.getContext(), "Alarm disabled", Toast.LENGTH_SHORT).show();
                    }

                    AlarmManagerHelper.setAlarms(DbManager.getContext());
                }
            });
        }
        return inflatedView;
    }


    public void setInactiveAlarm(Alarm alarm, View holder) {
        if(holder == null) {
            return;
        }

        View alarmView = holder.findViewById((int) alarm.getID());

        if(alarmView == null) {
            return;
        }

        TextView wakeupTime = (TextView)alarmView.findViewById(R.id.alarm_item_time_wakeup);

        if(wakeupTime == null) {
            return;
        }

        wakeupTime.setText(R.string.alarm_not_available);
    }

    public void addEmptyNoteToAlarmList(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(DbManager.getContext());

        View inflatedView = layoutInflater.inflate(R.layout.alarm_list_empty, parent, false);

        parent.addView(inflatedView);
    }
}
