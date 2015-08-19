package yv.jot.utils;


import android.content.Context;
import android.util.Pair;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import yv.jot.Models.Alarm;
import yv.jot.Properties.RequestsUrls;
import yv.jot.location.ResponseJsonFields;

public class TrafficUtils {

    private static TrafficUtils instance = null;

    public static TrafficUtils instance() {
        if(instance == null) {
            instance = new TrafficUtils();
        }
        return instance;
    }

    private TrafficUtils() {

    }

    public int getDrivingTime(Context context, Alarm alarm) {

        try {

            String url = getUrl(context, alarm);

            // setting the client and the request
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGetReq = new HttpGet(url);

            httpGetReq.addHeader("Accept", "application/json");

            HttpResponse httpresponse = null;

            httpresponse = httpclient.execute(httpGetReq);

            HttpEntity httpEntity = httpresponse.getEntity();
            String result = EntityUtils.toString(httpEntity);

            JSONObject json = new JSONObject(result);

            String status = json.getString(ResponseJsonFields.FIELD_STATUS);
            if(status != null && status.equalsIgnoreCase("ok")) {

                JSONArray rows = json.getJSONArray(ResponseJsonFields.FIELD_ROWS);

                if(rows != null) {
                    JSONObject row = rows.getJSONObject(0);
                    JSONArray elements = row.getJSONArray(ResponseJsonFields.FIELD_ELEMENTS);

                    if(elements != null) {
                        JSONObject element = elements.getJSONObject(0);
                        String statusResult = element.getString(ResponseJsonFields.FIELD_STATUS);
                        if(statusResult.equalsIgnoreCase("ok")) {
                            JSONObject duration = element.getJSONObject(ResponseJsonFields.FIELD_DURATION);
                            int minutes = duration.getInt(ResponseJsonFields.FIELD_VALUE) / 60;
                            return minutes;
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }

        return 0;
    }


    private String getUrl(Context context, Alarm alarm) {

        String url = RequestsUrls.DISTANS_MATRIX_REQUEST;

        Pair<Double, Double> currentCoord = LocationsUtils.instance().getCurrentLocationCoordinates(context);

        url = url.replace("${originLong}", String.valueOf(currentCoord.first));
        url = url.replace("${originLat}", String.valueOf(currentCoord.second));

        url = url.replace("${destLong}", String.valueOf(alarm.getLongtitude()));
        url = url.replace("${destLat}", String.valueOf(alarm.getLatitude()));

        return url;
    }

}
