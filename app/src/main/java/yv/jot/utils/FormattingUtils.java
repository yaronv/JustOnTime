package yv.jot.utils;


public class FormattingUtils {

    private static FormattingUtils instance = null;

    public static FormattingUtils instance() {
        if(instance == null) {
            instance = new FormattingUtils();
        }
        return instance;
    }

    private FormattingUtils() {

    }

    public String formatHour(int hour) {
        if(hour < 10) {
            return "0" + String.valueOf(hour);
        }
        return String.valueOf(hour);
    }

    public String formatMinutes(int minutes) {
        if(minutes < 10) {
            return "0" + String.valueOf(minutes);
        }
        return String.valueOf(minutes);
    }
}
