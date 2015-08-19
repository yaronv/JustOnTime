package yv.jot.location;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import yv.jot.Models.Alarm;
import yv.jot.utils.TrafficUtils;

public class TrafficTimeCalculatorTask extends AsyncTask<Alarm, Void, Integer> {

    private final Context context;
    private final Alarm alarm;
    private final TextView holder;

    public TrafficTimeCalculatorTask(Context context, Alarm alarm, TextView holder) {
        this.context = context;
        this.alarm = alarm;
        this.holder = holder;
    }

    @Override
    protected Integer doInBackground(Alarm... params) {

        Alarm alarm = params[0];

        int drivingTime = TrafficUtils.instance().getDrivingTime(context, alarm);

        return drivingTime;
    }

    @Override
    protected void onPreExecute() {}


    @Override
    protected void onPostExecute(Integer result) {
        this.holder.setText(result + " minutes");
    }

    @Override
    protected void onProgressUpdate(Void... values) {}

}