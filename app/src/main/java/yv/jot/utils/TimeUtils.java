package yv.jot.utils;


import java.util.HashMap;
import java.util.Map;

public class TimeUtils {

    private static TimeUtils instance = null;

    public static TimeUtils instance() {
        if(instance == null) {
            instance = new TimeUtils();
        }
        return instance;
    }

    private Map<String, Integer> timeToIndex = new HashMap<String, Integer>();

    private Map<String, Integer> snoozeToIndex = new HashMap<String, Integer>();

    private TimeUtils() {
        initTimeToIndex();
        initSnoozeToIndex();
    }

    private void initTimeToIndex() {
        timeToIndex.put("0", 0);
        timeToIndex.put("15", 1);
        timeToIndex.put("30", 2);
        timeToIndex.put("45", 3);
        timeToIndex.put("60", 4);
        timeToIndex.put("75", 5);
        timeToIndex.put("90", 6);
        timeToIndex.put("120", 7);
    }

    private void initSnoozeToIndex() {
        snoozeToIndex.put("5", 0);
        snoozeToIndex.put("10", 1);
        snoozeToIndex.put("15", 2);
        snoozeToIndex.put("20", 3);
        snoozeToIndex.put("25", 4);
        snoozeToIndex.put("30", 5);
    }

    public Map<String, Integer> getSnoozeToIndex() {
        return snoozeToIndex;
    }

    public Map<String, Integer> getTimeToIndex() {
        return this.timeToIndex;
    }
}
