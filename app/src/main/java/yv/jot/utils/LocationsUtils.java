package yv.jot.utils;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;

public class LocationsUtils {

    private static LocationsUtils instance = null;

    public static LocationsUtils instance() {
        if(instance == null) {
            instance = new LocationsUtils();
        }
        return instance;
    }

    private LocationsUtils() {

    }

    public Pair<Double, Double> getAddressCoordinates(Context context, String address) {
        Geocoder coder = new Geocoder(context);
        Pair<Double, Double> coord = null;
        try {
            ArrayList<Address> adresses = (ArrayList<Address>) coder.getFromLocationName(address, 1);
            for(Address add : adresses){

                double longitude = add.getLongitude();
                double latitude = add.getLatitude();

                coord = new Pair<>(longitude, latitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return coord;
    }

    public Pair<Double, Double> getCurrentLocationCoordinates(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = 34.7824710;
        double latitude = 32.0802910;
        if(location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        return new Pair<>(longitude, latitude);
    }
}
