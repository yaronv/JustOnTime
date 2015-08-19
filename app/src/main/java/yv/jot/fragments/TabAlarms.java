package yv.jot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import java.util.List;

import yv.jot.Models.Alarm;
import yv.jot.Properties.Constants;
import yv.jot.R;
import yv.jot.activities.AddAlarmActivity;
import yv.jot.db.DbManager;
import yv.jot.utils.AlarmManagerHelper;
import yv.jot.utils.AlarmsManager;

public class TabAlarms extends Fragment {

    private FloatingActionButton FAB;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.tab_alarms,container,false);

        initFAB(v);

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        DbManager.initialize(getActivity());

        TableLayout holder = (TableLayout)(getActivity().findViewById(R.id.alarms_holder));

        setAlarmsInsideView(holder);

        AlarmManagerHelper.setAlarms(getActivity());
    }

    private void initFAB(View v) {
        FAB = (FloatingActionButton) v.findViewById(R.id.fab);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddAlarmActivity.class);
                getActivity().startActivityForResult(intent, Constants.ADD_ALARM_REQUEST);
            }
        });
    }

    public void setAlarmsInsideView(TableLayout holder) {
        List<Alarm> alarms = DbManager.instance().getAllAlarms();

        if(alarms != null && alarms.size() > 0) {
            View emptyRow = holder.findViewById(R.id.empty_alarm_list_row);
            if(emptyRow != null) {
                ((ViewGroup)emptyRow.getParent()).removeView(emptyRow);
            }
            for(Alarm alarm : alarms) {
                View v = AlarmsManager.instance().addAlarmToList(alarm, holder);
                if(v != null) {
                    unregisterForContextMenu(v);
                    registerForContextMenu(v);
                }
            }
        }
        else {
            View emptyRow = holder.findViewById(R.id.empty_alarm_list_row);
            if(emptyRow != null) {
                return;
            }
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v = inflater.inflate(R.layout.alarm_list_empty, holder, false);
            holder.addView(v);
        }
    }
}