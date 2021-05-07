package my.project.fer.ryzetello.server.mock;

public class RyzeTello {

    private static RyzeTello INSTANCE;

    private boolean isFyling;
    private boolean isVideoStreamOn;
    private boolean motorsRunning;

    // Location
    private int x;
    private int y;
    private int z;

    private String mid = null;

    private int flyTimeStart;
    private int speed = 20;
    private int battery = 100;
    private String wifi = "TEST-WIFI";
    private String sdk = "TEST-SDK";
    private String sn = "TEST-SERIAL-NUMBER";

    private RyzeTello() {
    }

    public static RyzeTello getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RyzeTello();
        }

        return INSTANCE;
    }

    public boolean isFyling() {
        return isFyling;
    }

    public void setFyling(boolean fyling) {
        isFyling = fyling;
    }

    public boolean isVideoStreamOn() {
        return isVideoStreamOn;
    }

    public void setVideoStreamOn(boolean videoStreamOn) {
        isVideoStreamOn = videoStreamOn;
    }

    public boolean isMotorsRunning() {
        return motorsRunning;
    }

    public void setMotorsRunning(boolean motorsRunning) {
        this.motorsRunning = motorsRunning;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public int getFlyTimeStart() {
        return flyTimeStart;
    }

    public void setFlyTimeStart(int flyTimeStart) {
        this.flyTimeStart = flyTimeStart;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getWifi() {
        return wifi;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

}
