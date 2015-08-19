package yv.jot.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import yv.jot.Properties.Constants;
import yv.jot.Properties.Defaults;
import yv.jot.Properties.Prefs;
import yv.jot.R;
import yv.jot.common.Gender;
import yv.jot.utils.PreferenceUtils;
import yv.jot.utils.TimeUtils;

public class SettingsActivityFragment extends Fragment {

    private ImageButton submitButton;
    private EditText fullName;
    private Spinner extraTime;
    private Spinner snoozeDelay;
    private Spinner genderSpinner;
    private EditText ringtone;

    public SettingsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        this.submitButton = (ImageButton)v.findViewById(R.id.save_settings_submit);

        this.fullName = (EditText)v.findViewById(R.id.settings_full_name);

        this.extraTime = (Spinner)v.findViewById(R.id.settings_extra_time);

        this.snoozeDelay = (Spinner)v.findViewById(R.id.settings_snooze_delay);

        this.genderSpinner = (Spinner)v.findViewById(R.id.gender_select_spinner);

        this.ringtone = (EditText)v.findViewById(R.id.settings_ringtone);

        putExistingData();

        bindSubmitHandler();

        initRingtoneHandler();

        return v;
    }

    private void initRingtoneHandler() {


        ringtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                startActivityForResult(intent, Constants.RINGTONE_REQUEST);
            }
        });
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
                        if (g != null) {
                            gender = g.getCode();
                        }

                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.FULL_NAME, name);
                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.GET_READY_TIME, extra);
                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.GENDER, gender);
                        PreferenceUtils.writePreferenceValue(getActivity().getApplicationContext(), Prefs.SNOOZE_DELAY, snooze);

                        Toast.makeText(getActivity().getApplicationContext(), "Settings Saved", Toast.LENGTH_SHORT).show();

                        getActivity().finish();
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.RINGTONE_REQUEST)
            if (data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI) != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {
                    Ringtone r = RingtoneManager.getRingtone(getActivity(), uri);
                    String ringToneName = r.getTitle(getActivity());
                    if(ringtone != null) {
                        ringtone.setText(ringToneName);
                    }
//                    RingtoneManager.setActualDefaultRingtoneUri(getActivity().getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION, uri);
                }
            }
    }
}
