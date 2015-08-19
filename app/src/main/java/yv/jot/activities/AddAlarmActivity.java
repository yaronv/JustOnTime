package yv.jot.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import yv.jot.Models.Alarm;
import yv.jot.Properties.Constants;
import yv.jot.Properties.Defaults;
import yv.jot.Properties.Prefs;
import yv.jot.R;
import yv.jot.db.DbManager;
import yv.jot.location.PlaceAutocompleteAdapter;
import yv.jot.utils.PreferenceUtils;
import yv.jot.utils.TimeUtils;

public class AddAlarmActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final String TAG = "add-alarm-activity";

    private static long DEFAULT = -1;

    private GoogleApiClient mGoogleApiClient = null;
    private PlaceAutocompleteAdapter mToAdapter;

    private Toolbar toolbar;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private long currentAlarm = DEFAULT;

    private AutoCompleteTextView mToAutocompleteView;

//    private DatePicker mDateSelect;
    private TimePicker mTimeSelect;

//    private TextView mPlaceDetailsText;
//    private TextView mPlaceDetailsAttribution;

    private static final LatLngBounds BOUNDS = new LatLngBounds(new LatLng(-85, -180), new LatLng(85, 180));

    private Spinner extraTime;
    private ImageButton   submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        buildGoogleApiClient();

//        mDateSelect = (DatePicker)findViewById(R.id.add_alarm_form_date);
        mTimeSelect = (TimePicker)findViewById(R.id.add_alarm_form_time);

        mToAutocompleteView = (AutoCompleteTextView)findViewById(R.id.add_alarm_form_to);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Set new smart alarm");

//        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
//        mPlaceDetailsAttribution = (TextView) findViewById(R.id.place_attribution);

        extraTime = (Spinner) findViewById(R.id.add_alarm_form_extra_time);
        submitButton = (ImageButton) findViewById(R.id.add_alarm_form_submit);

        mToAutocompleteView.setOnItemClickListener(mToAutocompleteClickListener);

        mToAdapter = new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, BOUNDS, null);

        mToAutocompleteView.setAdapter(mToAdapter);

        mTimeSelect.setIs24HourView(DateFormat.is24HourFormat(this));

        bindSubmitHandler();

        String extra = PreferenceUtils.readPreferenceValue(getApplicationContext(), Prefs.GET_READY_TIME, "");

        if(extra.isEmpty()) {
            extra = Defaults.GET_READY_TIME;
        }

        extraTime.setSelection(TimeUtils.instance().getTimeToIndex().get(extra));

        long ca = getIntent().getLongExtra(Constants.ALARM_ID_EXTRA, -1);
        this.currentAlarm = ca;
        if(ca != -1) {
            setAlarmFields(ca);
        }
    }

    private void bindSubmitHandler() {
        submitButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        int totalMinutes = 0;

                        totalMinutes += Integer.parseInt(extraTime.getSelectedItem().toString());

//                        if (extraTime.getValue() != null && !extraTime.getText().toString().equals("")) {
//                            totalMinutes += Integer.parseInt(String.valueOf(extraTime.getText().toString()));
//                        }
//                        else {
//                            totalMinutes = Integer.parseInt(Defaults.GET_READY_TIME);
//                        }

                        String destination = mToAutocompleteView.getText().toString();
                        if(destination.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please insert destination", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(Constants.TOTAL_TIME_EXTRA, totalMinutes);
                        returnIntent.putExtra(Constants.TO_ADDRESS_EXTRA, destination);
                        String hours = String.valueOf(mTimeSelect.getCurrentHour());
                        hours = mTimeSelect.getCurrentHour() < 10 ? "0" + hours : hours;
                        String minutes = String.valueOf(mTimeSelect.getCurrentMinute());
                        minutes = mTimeSelect.getCurrentMinute() < 10 ? "0" + minutes : minutes;
                        returnIntent.putExtra(Constants.TIME_EXTRA, hours + ":" + minutes);
                        returnIntent.putExtra(Constants.ALARM_ID_EXTRA, currentAlarm);
                        setResult(RESULT_OK, returnIntent);

                        finish();
                    }
                }
        );
    }

    private AdapterView.OnItemClickListener mToAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a PlaceAutocomplete object from which we
             read the place ID.
              */
            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mToAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mToUpdatePlaceDetailsCallback);

//            Toast.makeText(getApplicationContext(), "Clicked: " + item.description, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + item.placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mToUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }

            // Get the Place object from the buffer.
//            final Place place = places.get(0);
//
//            // Format details of the place for display and show it in a TextView.
//            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
//                    place.getId(), place.getAddress(), place.getPhoneNumber(),
//                    place.getWebsiteUri()));

//            // Display the third party attributions if set.
//            final CharSequence thirdPartyAttribution = places.getAttributions();
//            if (thirdPartyAttribution == null) {
//                mPlaceDetailsAttribution.setVisibility(View.GONE);
//            } else {
//                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
//                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
//            }

//            Log.i(TAG, "Place details received: " + place.getName());


            places.release();
        }
    };

//    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
//                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
//        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
//                websiteUri));
//        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
//                websiteUri));
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_alarm, menu);
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, 0 /* clientId */, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connection started");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
//            ((MainActivity)getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private void setAlarmFields(long currentAlarm) {
        Alarm alarm = DbManager.instance().getAlarm((int)currentAlarm);

        if(alarm != null) {
            this.extraTime.setSelection(TimeUtils.instance().getTimeToIndex().get(String.valueOf(alarm.getExtra())));
            this.mToAutocompleteView.setText(alarm.getDestination());
            this.mTimeSelect.setCurrentHour(Integer.parseInt(alarm.getTime().split(":")[0]));
            this.mTimeSelect.setCurrentMinute(Integer.parseInt(alarm.getTime().split(":")[1]));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, Constants.SETTINGS_REQUEST);
            return true;
        }

        else if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
