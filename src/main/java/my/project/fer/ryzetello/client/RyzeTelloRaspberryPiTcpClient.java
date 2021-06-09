package my.project.fer.ryzetello.client;

//import my.project.fer.ryzetello.constants.MessageConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Client placed on RaspberryPi that registers drone on the coordinator server and sends commands from that server to
 * the drone
 */
public class RyzeTelloRaspberryPiTcpClient implements Runnable {

    private static final int DEFAULT_RASPBERRY_PI_CLIENT_STATE_PORT = 8890;
    //private static final int DEFAULT_RASPBERRY_PI_CLIENT_VIDEO_PORT = 11111;
    private static final String DEFAULT_COORDINATOR_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_COORDINATOR_SERVER_PORT = 50000;
    private static final int DEFAULT_COORDINATOR_SERVER_VIDEO_PORT = 50010;
    private static final String DEFAULT_DRONE_HOST = "192.168.10.1";
    private static final int DEFAULT_DRONE_PORT = 8889;
    private static final int DEFAULT_DRONE_VIDEO_PORT = 11111;

    // TODO: Move to other class
    public static final String COMMAND = "command";
    public static final String STREAM = "stream";
    public static final String STREAM_ON = "streamon";
    public static final String STREAM_OFF = "streamoff";
    public static final String TIME_READ = "time?";
    public static final String REGISTER = "REGISTER";
    public static final String ASSIGNED_VIDEO_PORT = "ASSIGNED_VIDEO_PORT";
    public static final String HEALTH_CHECK = "HEALTH_CHECK";
    public static final String HEALTH_CHECK_ALL_OK = "HEALTH_CHECK_ALL_OK";
    public static final String HEALTH_CHECK_DRONE_OFFLINE = "HEALTH_CHECK_DRONE_OFFLINE";

    public static final long DRONE_HEALTH_CHECK_DELAY_SECONDS = 3;
    public static final long DRONE_HEALTH_CHECK_DELAY_MILLISECONDS = 3000;

    public static final long DRONE_KEEP_ALIVE_DELAY_SECONDS = 10;
    //

    private final String coordinatorServerHost;
    private final int coordinatorServerPort;
    private final int coordinatorServerVideoPort;

    private final String droneHost;
    private final int dronePort;
    private final int droneVideoPort;

    private DatagramSocket clientSocket;
    private Socket clientTcpSocket;
    private Socket videoClientTcpSocket;

    private DatagramSocket clientVideoSocket;

    // UDP Video port
    private int videoPort = -1;

    // TCP in/out streams
    private PrintWriter out;
    private BufferedReader in;

    // TCP video in/out streams
    private BufferedOutputStream videoOut;
    private BufferedInputStream videoIn;

    // UDP buffers
    private byte[] receiveBuffer = new byte[1024];
    private byte[] sendBuffer = new byte[1024];

    // UDP video buffers
    private byte[] receiveVideoBuffer = new byte[1460];
    private byte[] sendVideoBuffer = new byte[1460];

    private long lastReceivedDroneStateTimestamp = -1L;

    private ExecutorService executorService;
    private ScheduledExecutorService healthCheckScheduledExecutorService;
    private ScheduledExecutorService keepAliveScheduledExecutorService;

    private ExecutorService clientsExecutorService;

    public RyzeTelloRaspberryPiTcpClient() {
        final String envCoordinatorHost = System.getenv("RYZE_TELLO_COORDINATOR_SERVER_HOST");
        final String envCoordinatorPort = System.getenv("RYZE_TELLO_COORDINATOR_SERVER_PORT");
        final String envCoordinatorVideoPort = System.getenv("RYZE_TELLO_COORDINATOR_SERVER_VIDEO_PORT");

        this.coordinatorServerHost = envCoordinatorHost == null ? DEFAULT_COORDINATOR_SERVER_HOST : envCoordinatorHost;
        this.coordinatorServerPort =
            envCoordinatorPort == null ? DEFAULT_COORDINATOR_SERVER_PORT : Integer.parseInt(envCoordinatorPort);
        this.coordinatorServerVideoPort = envCoordinatorVideoPort == null ?
            DEFAULT_COORDINATOR_SERVER_VIDEO_PORT :
            Integer.parseInt(envCoordinatorVideoPort);

        final String envDroneHost = System.getenv("RYZE_TELLO_DRONE_HOST");
        final String envDronePort = System.getenv("RYZE_TELLO_DRONE_PORT");
        final String envDroneVideoPort = System.getenv("RYZE_TELLO_DRONE_VIDEO_PORT");

        this.droneHost = envDroneHost == null ? DEFAULT_DRONE_HOST : envDroneHost;
        this.dronePort = envDronePort == null ? DEFAULT_DRONE_PORT : Integer.parseInt(envDronePort);
        this.droneVideoPort =
            envDroneVideoPort == null ? DEFAULT_DRONE_VIDEO_PORT : Integer.parseInt(envDroneVideoPort);

        this.healthCheckScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.keepAliveScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.clientsExecutorService = Executors.newFixedThreadPool(2);

        try {
            this.clientSocket = new DatagramSocket(DEFAULT_RASPBERRY_PI_CLIENT_STATE_PORT);
            this.clientTcpSocket = new Socket(coordinatorServerHost, coordinatorServerPort);
            this.videoClientTcpSocket = new Socket(coordinatorServerHost, coordinatorServerVideoPort);

            this.out = new PrintWriter(clientTcpSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientTcpSocket.getInputStream()));

            this.videoOut = new BufferedOutputStream(videoClientTcpSocket.getOutputStream());
            this.videoIn = new BufferedInputStream(videoClientTcpSocket.getInputStream());
        } catch (Exception e) {
            System.err.println("Socket error. Shutting down..." + e.getMessage());
            System.exit(1);
        }
    }

    private class TcpClientRunnable implements Runnable {

        @Override
        public void run() {
            try {
                String receivedMessage;
                while ((receivedMessage = in.readLine()) != null) {
                    System.out.println("Client received message: " + receivedMessage);

                    if (receivedMessage.startsWith(ASSIGNED_VIDEO_PORT)) {
                        String[] split = receivedMessage.split(":");
                        int port = Integer.parseInt(split[1]);

                        RyzeTelloRaspberryPiTcpClient.this.videoPort = port;

                        clientVideoSocket = new DatagramSocket(droneVideoPort);
                    } else if (receivedMessage.startsWith(HEALTH_CHECK)) {
                        // TODO Check drone state, for now, send ALL_OK
                        // Send ALL_OK to coordinator
                        System.out.println("Sending message to coordinator: " + HEALTH_CHECK_ALL_OK);
                        out.print(HEALTH_CHECK_ALL_OK);
                    } else if (receivedMessage.startsWith(STREAM)) {
                        if (receivedMessage.startsWith(STREAM_ON)) {
                            // Turn on video stream
                            executorService = Executors.newFixedThreadPool(1);
                            executorService.submit(new RyzeTelloRaspberryPiVideoClient());

                            // Send streamon command to drone
                            sendBuffer = receivedMessage.getBytes();
                            DatagramPacket sendPacket =
                                new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost),
                                    dronePort);
                            clientSocket.send(sendPacket);

                            sendBuffer = new byte[1024];
                        } else if (receivedMessage.startsWith(STREAM_OFF)) {
                            // Send streamoff command to drone
                            sendBuffer = receivedMessage.getBytes();
                            DatagramPacket sendPacket =
                                new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost),
                                    dronePort);
                            clientSocket.send(sendPacket);

                            sendBuffer = new byte[1024];

                            // Turn off video stream
                            //clientVideoSocket.disconnect();
                            //clientVideoSocket.close();
                            //executorService.shutdown();
                        }
                    } else {
                        // Send command to drone
                        sendBuffer = receivedMessage.getBytes();
                        DatagramPacket sendPacket =
                            new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost),
                                dronePort);
                        clientSocket.send(sendPacket);

                        sendBuffer = new byte[1024];
                    }

                }

            } catch (Exception e) {
                System.err.println("Socket error. Shutting down..." + e.getMessage());
                System.exit(1);
            }

        }

    }

    private class UdpClientRunnable implements Runnable {

        @Override
        public void run() {
            try {
                // Enable drone SDK mode
                sendBuffer = COMMAND.getBytes();
                DatagramPacket sendPacket =
                    new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost), dronePort);
                clientSocket.send(sendPacket);
                sendBuffer = new byte[1024];

                keepAliveScheduledExecutorService.scheduleAtFixedRate(new DroneKeepAliveRunnable(),
                    DRONE_KEEP_ALIVE_DELAY_SECONDS, DRONE_KEEP_ALIVE_DELAY_SECONDS, TimeUnit.SECONDS);

                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    clientSocket.receive(receivePacket);

                    String receivedMessage = new String(receivePacket.getData()).replaceAll("\0", "");
                    System.out.println("Client received message: " + receivedMessage);

                    if (lastReceivedDroneStateTimestamp == -1) {
                        healthCheckScheduledExecutorService
                            .scheduleAtFixedRate(new DroneHealthCheckRunnable(), DRONE_HEALTH_CHECK_DELAY_SECONDS,
                                DRONE_HEALTH_CHECK_DELAY_SECONDS, TimeUnit.SECONDS);
                    }

                    lastReceivedDroneStateTimestamp = System.currentTimeMillis();

                    receiveBuffer = new byte[1024];
                }
            } catch (Exception e) {
                System.err.println("Socket error. Shutting down..." + e.getMessage());
                System.exit(1);
            }

        }

    }

    @Override
    public void run() {
        //System.out.printf("RaspPi Client started on %s:%d.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        clientsExecutorService.submit(new TcpClientRunnable());
        clientsExecutorService.submit(new UdpClientRunnable());
    }

    public static void main(String[] args) {
        RyzeTelloRaspberryPiTcpClient client = new RyzeTelloRaspberryPiTcpClient();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(client);
    }

    private class DroneHealthCheckRunnable implements Runnable {

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();

            long difference = currentTime - lastReceivedDroneStateTimestamp;

            if (difference > DRONE_HEALTH_CHECK_DELAY_MILLISECONDS) {
                // Send HEALTH_CHECK_DRONE_OFFLINE to coordinator
                System.out.println("Sending message to coordinator: " + HEALTH_CHECK_DRONE_OFFLINE);
                out.println(HEALTH_CHECK_DRONE_OFFLINE);

                healthCheckScheduledExecutorService.shutdownNow();

                //
                System.exit(1);
            }
        }

    }

    private class DroneKeepAliveRunnable implements Runnable {

        @Override
        public void run() {
            try {
                System.out.println("Sending keep-alive to drone.");
                sendBuffer = TIME_READ.getBytes();
                DatagramPacket sendPacket =
                    new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost), dronePort);
                clientSocket.send(sendPacket);
                sendBuffer = new byte[1024];
            } catch (IOException e) {
                // TODO?
            }
        }

    }

    private class RyzeTelloRaspberryPiVideoClient implements Runnable {

        @Override
        public void run() {
            try {
                // Wait for stream data
                while (true) {
                    DatagramPacket receiveVideoPacket =
                        new DatagramPacket(receiveVideoBuffer, receiveVideoBuffer.length);
                    clientVideoSocket.receive(receiveVideoPacket);

                    byte[] receivedVideoData = receiveVideoPacket.getData();
                    System.out
                        .println(":::::::VIDEO::::::: Client received video data, size: " + receivedVideoData.length);

                    receiveVideoBuffer = new byte[1460];

                    // Send video to coordinator server
                    sendVideoBuffer = receivedVideoData;

                    //videoOut.write(sendVideoBuffer);
                    DatagramPacket sendVideoPacket = new DatagramPacket(sendVideoBuffer, sendVideoBuffer.length,
                        InetAddress.getByName(coordinatorServerHost), videoPort);
                    clientVideoSocket.send(sendVideoPacket);

                    sendVideoBuffer = new byte[1460];
                }
            } catch (IOException e) {
                System.err.println("Socket error. Shutting down..." + e.getMessage());
                System.exit(1);
            }

        }

    }

}
