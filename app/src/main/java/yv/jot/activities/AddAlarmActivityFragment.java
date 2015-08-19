package yv.jot.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import yv.jot.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddAlarmActivityFragment extends Fragment {

    public AddAlarmActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_alarm, container, false);
    }
}
