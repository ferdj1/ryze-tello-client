package my.project.fer.ryzetello.server.mock;

public interface RyzeTelloCommandService {

    RyzeTelloState takeoff();

    RyzeTelloState land();

    RyzeTelloState streamon();

    RyzeTelloState streamoff();

    RyzeTelloState emergency();

    RyzeTelloState up(int x);

    RyzeTelloState down(int x);

    RyzeTelloState left(int x);

    RyzeTelloState right(int x);

    RyzeTelloState forward(int x);

    RyzeTelloState back(int x);

    RyzeTelloState cw(int x);

    RyzeTelloState ccw(int x);

    RyzeTelloState flip(char x);

    RyzeTelloState go(int x, int y, int z, int speed);

    RyzeTelloState stop();

    RyzeTelloState curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed);

    RyzeTelloState go(int x, int y, int z, int speed, String mid);

    RyzeTelloState curve(int x1, int y1, int z1, int x2, int y2, int z2, int speed, String mid);
    
    RyzeTelloState jump(int x, int y, int z, int speed, int yaw, String mid1, String mid2);
    
    RyzeTelloState speed(int x);
    
    RyzeTelloState rc(int a, int b, int c, int d);
    
    RyzeTelloState wifi(String ssid, String pass);
    
    RyzeTelloState mon();
    
    RyzeTelloState moff();
    
    RyzeTelloState mdirection(int x);
    
    RyzeTelloState ap(String ssid, String pass);

}
