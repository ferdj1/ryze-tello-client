package my.project.fer.ryzetello.server.mock;

public class RyzeTelloCommandServiceImpl implements RyzeTelloCommandService {

    private RyzeTello tello;

    public RyzeTelloCommandServiceImpl() {
        this.tello = RyzeTello.getInstance();
    }

    @Override
    public RyzeTelloState takeoff() {
        tello.setFyling(true);
        tello.setMotorsRunning(true);

        System.out.println("Tello: takeoff");

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState land() {
        tello.setFyling(false);
        tello.setMotorsRunning(false);

        System.out.println("Tello: landing");

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState streamon() {
        tello.setVideoStreamOn(true);

        System.out.println("Tello: video stream on");

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState streamoff() {
        tello.setVideoStreamOn(false);

        System.out.println("Tello: video stream off");

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState emergency() {
        tello.setMotorsRunning(false);
        tello.setFyling(false);

        System.out.println("Tello: emergency landing");

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState up(int x) {
        tello.setZ(tello.getZ() + x);

        System.out.println("Tello: going up by " + x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState down(int x) {
        tello.setZ(tello.getZ() - x);

        System.out.println("Tello: going down by " + x);

        return RyzeTelloState.OK;

    }

    @Override
    public RyzeTelloState left(int x) {
        tello.setX(tello.getX() - x);

        System.out.println("Tello: going left by " + x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState right(int x) {
        tello.setX(tello.getX() + x);

        System.out.println("Tello: going right by " + x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState forward(int x) {
        tello.setY(tello.getY() + x);

        System.out.println("Tello: going forward by " + x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState back(int x) {
        tello.setY(tello.getY() - x);

        System.out.println("Tello: going back by " + x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState cw(int x) {
        System.out.println("Tello: turning clockwise by " + x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState ccw(int x) {
        System.out.println("Tello: turning counter-clockwise by " + x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState flip(char x) {
        switch (x) {
            case 'l':
                System.out.println("Tello: flipping left");
                return RyzeTelloState.OK;
            case 'r':
                System.out.println("Tello: flipping right");
                return RyzeTelloState.OK;
            case 'b':
                System.out.println("Tello: flipping back");
                return RyzeTelloState.OK;
            case 'f':
                System.out.println("Tello: flipping forward");
                return RyzeTelloState.OK;
            default:
                System.out.println("Tello: can't flip");
                return RyzeTelloState.ERROR;
        }
    }

    @Override
    public RyzeTelloState go(int x, int y, int z, int speed) {
        tello.setSpeed(speed);
        tello.setX(x);
        tello.setY(y);
        tello.setZ(z);

        System.out.printf("Tello: going to (%d, %d, %d) at speed %d%n", x, y, z, speed);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState stop() {
        System.out.println("Tello: hover");

        return RyzeTelloState.OK;

    }

    @Override
    public RyzeTelloState curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed) {
        tello.setSpeed(speed);

        System.out.printf("Tello: flying at a curve (%d, %d, %d) to (%d, %d, %d) at speed %d%n", x1, y1, z1, x2, y2, z2, speed);

        return RyzeTelloState.OK;

    }

    @Override
    public RyzeTelloState go(int x, int y, int z, int speed, String mid) {
        tello.setSpeed(speed);
        tello.setX(x);
        tello.setY(y);
        tello.setZ(z);
        tello.setMid(mid);

        System.out.printf("Tello: going to (%d, %d, %d) at speed %d, mid: %s%n", x, y, z, speed, mid);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed, String mid) {
        tello.setSpeed(speed);

        System.out.printf("Tello: flying at a curve (%d, %d, %d) to (%d, %d, %d) at speed %d, mid: %d%n", x1, y1, z1, x2, y2, z2, speed, mid);

        return RyzeTelloState.OK;

    }

    @Override
    public RyzeTelloState jump(int x, int y, int z, int speed, int yaw, String mid1, String mid2) {
        tello.setX(x);
        tello.setY(y);
        tello.setZ(z);

        System.out.printf("Tello: jump to(%d, %d, %d) on %s. Recognized (0, 0, %d) at  %s. Speed %d, yaw: %d%n", x, y, z, mid1, z, mid2, speed, yaw);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState speed(int x) {
        tello.setSpeed(x);

        System.out.printf("Tello: set speed to %d", x);

        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState rc(int a, int b, int c, int d) {
        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState wifi(String ssid, String pass) {
        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState mon() {
        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState moff() {
        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState mdirection(int x) {
        return RyzeTelloState.OK;
    }

    @Override
    public RyzeTelloState ap(String ssid, String pass) {
        return RyzeTelloState.OK;
    }

}
