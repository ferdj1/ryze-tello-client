package my.project.fer.ryzetello.server.mock;

public class RyzeTelloReadServiceImpl implements RyzeTelloReadService {

    private RyzeTello tello;

    public RyzeTelloReadServiceImpl() {
        this.tello = RyzeTello.getInstance();
    }

    @Override
    public String speed() {
        return String.valueOf(tello.getSpeed());
    }

    @Override
    public String battery() {
        return String.valueOf(tello.getBattery());
    }

    @Override
    public String time() {
        return String.valueOf(System.currentTimeMillis() - tello.getFlyTimeStart());
    }

    @Override
    public String wifi() {
        return tello.getWifi();
    }

    @Override
    public String sdk() {
        return tello.getSdk();
    }

    @Override
    public String sn() {
        return tello.getSn();
    }

}
