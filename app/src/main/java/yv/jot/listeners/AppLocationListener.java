package yv.jot.listeners;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

/**
 * Created by yaron on 17/07/15.
 */
public class AppLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
        location.getLatitude();
        location.getLongitude();

        String myLocation = "Latitude = " + location.getLatitude() + " Longitude = " + location.getLongitude();

        //I make a log to see the results
        Log.e("MY CURRENT LOCATION", myLocation);
    }
}
