package yv.jot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import yv.jot.Properties.Defaults;
import yv.jot.Properties.Prefs;
import yv.jot.R;
import yv.jot.common.Gender;
import yv.jot.utils.PreferenceUtils;
import yv.jot.utils.TimeUtils;

public class TabSettings extends Fragment {

    private ImageButton submitButton;
    private EditText fullName;
    private Spinner extraTime;
    private Spinner snoozeDelay;
    private  Spinner genderSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_settings, container, false);

        this.submitButton = (ImageButton)v.findViewById(R.id.save_settings_submit);

        this.fullName = (EditText)v.findViewById(R.id.settings_full_name);

        this.extraTime = (Spinner)v.findViewById(R.id.settings_extra_time);

        this.snoozeDelay = (Spinner)v.findViewById(R.id.settings_snooze_delay);

        this.genderSpinner = (Spinner)v.findViewById(R.id.gender_select_spinner);

        putExistingData();

        bindSubmitHandler();

        return v;
    }

    private void putExistingData() {
        String name = PreferenceUtils.readPreferenceValue(getActivity().getApplicationContext(), Prefs.FULL_NAME);
        String extra = PreferenceUtils.readPreferenceValue(getActivity().getApplicationContext(), Prefs.GET_READY_TIME);
        String snooze = PreferenceUtils.readPreferenceValue(getActivity().getApplicationContext(), Prefs.SNOOZE_DELAY);
        int gender = PreferenceUtils.readPreferenceValue(getActivity().getApplicationContext(), Prefs.GENDER, 0);

        if(name != null) {
            fullName.setText(name);
        }

        if(extra != null) {
            extraTime.setSelection(TimeUtils.instance().getTimeToIndex().get(extra));
        }
        else {
            extraTime.setSelection(TimeUtils.instance().getTimeToIndex().get(Defaults.GET_READY_TIME));
        }


        if(snooze != null) {
            snoozeDelay.setSelection(TimeUtils.instance().getSnoozeToIndex().get(snooze));
        }
        else {
            snoozeDelay.setSelection(TimeUtils.instance().getSnoozeToIndex().get(Defaults.SNOOZE_DELAY));
        }

        genderSpinner.setSelection(gender);

    }

    private void bindSubmitHandler() {
        submitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        String name = fullName.getText().toString();
                        String extra = extraTime.getSelectedItem().toString();
                        String snooze = snoozeDelay.getSelectedItem().toString();
                        Gender g = Gender.fromString(genderSpinner.getSelectedItem().toString());
                        int gender = 0;
                        if(g != null) {
                            gender = g.getCode();
                        }

                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.FULL_NAME, name);
                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.GET_READY_TIME, extra);
                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.GENDER, gender);
                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.SNOOZE_DELAY, snooze);

                        Toast.makeText(getActivity().getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();

//                        ((MainActivity)getActivity()).setCurrentTab(0);
                    }
                }
        );
    }
}