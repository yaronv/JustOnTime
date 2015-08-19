package yv.jot.Models;

public class Alarm {

    private long ID;
    private String time;
    private String destination;
    private int extra;
    private boolean active;
    private double longtitude;
    private double latitude;

    public Alarm() {

    }

    public Alarm(String time, String destination, int extra, boolean active) {
        this.setTime(time);
        this.setDestination(destination);
        this.setExtra(extra);
        this.setActive(active);
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }
}
