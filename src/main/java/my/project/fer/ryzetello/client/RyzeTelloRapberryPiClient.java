package my.project.fer.ryzetello.client;

//import my.project.fer.ryzetello.constants.MessageConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Client placed on RaspberryPi that registers drone on the coordinator server and sends commands from that server to
 * the drone
 */
public class RyzeTelloRapberryPiClient implements Runnable {

    private static final int DEFAULT_RASPBERRY_PI_CLIENT_STATE_PORT = 8890;
    private static final int DEFAULT_RASPBERRY_PI_CLIENT_VIDEO_PORT = 11111;
    private static final String DEFAULT_COORDINATOR_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_COORDINATOR_SERVER_PORT = 50000;
    private static final int DEFAULT_COORDINATOR_SERVER_VIDEO_PORT = 50010;
    private static final String DEFAULT_DRONE_HOST = "192.168.10.1";
    private static final int DEFAULT_DRONE_PORT = 8889;

    // TODO: Move to other class
    public static final String COMMAND = "command";
    public static final String STREAM = "stream";
    public static final String STREAM_ON = "streamon";
    public static final String STREAM_OFF = "streamoff";
    public static final String REGISTER = "REGISTER";
    public static final String HEALTH_CHECK = "HEALTH_CHECK";
    public static final String HEALTH_CHECK_ALL_OK = "HEALTH_CHECK_ALL_OK";
    public static final String HEALTH_CHECK_DRONE_OFFLINE = "HEALTH_CHECK_DRONE_OFFLINE";

    public static final long DRONE_HEALTH_CHECK_DELAY_SECONDS = 3;
    public static final long DRONE_HEALTH_CHECK_DELAY_MILLISECONDS = 3000;
    //

    private final String coordinatorServerHost;
    private final int coordinatorServerPort;
    private final int coordinatorServerVideoPort;

    private final String droneHost;
    private final int dronePort;

    private DatagramSocket clientSocket;

    private byte[] receiveBuffer = new byte[1024];
    private byte[] sendBuffer = new byte[1024];

    private byte[] receiveVideoBuffer = new byte[1024];
    private byte[] sendVideoBuffer = new byte[1024];

    private long lastReceivedDroneStateTimestamp = -1L;

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;

    public RyzeTelloRapberryPiClient() {
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

        this.droneHost = envDroneHost == null ? DEFAULT_DRONE_HOST : envDroneHost;
        this.dronePort = envDronePort == null ? DEFAULT_DRONE_PORT : Integer.parseInt(envDronePort);

        this.executorService = Executors.newFixedThreadPool(1);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        try {
            this.clientSocket = new DatagramSocket(DEFAULT_RASPBERRY_PI_CLIENT_STATE_PORT);
        } catch (SocketException e) {
            System.err.println("Socket error. Shutting down...");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            //System.out.printf("RaspPi Client started on %s:%d.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

            // Enable drone SDK mode
            sendBuffer = COMMAND.getBytes();
            DatagramPacket sendPacket =
                new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost), dronePort);
            clientSocket.send(sendPacket);
            sendBuffer = new byte[1024];

            // Register phase
            sendBuffer = REGISTER.getBytes();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(coordinatorServerHost),
                coordinatorServerPort);
            clientSocket.send(sendPacket);
            sendBuffer = new byte[1024];
            //

            // Wait for commands and state
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(receivePacket);

                String receivedMessage = new String(receivePacket.getData()).replaceAll("\0", "");
                System.out.println("Client received message: " + receivedMessage);

                if (receivePacket.getAddress().getHostAddress().equals(coordinatorServerHost)) {

                    if (receivedMessage.startsWith(HEALTH_CHECK)) {
                        // TODO Check drone state, for now, send ALL_OK
                        // Send ALL_OK to coordinator
                        System.out.println("Sending message to coordinator: " + HEALTH_CHECK_ALL_OK);
                        sendBuffer = HEALTH_CHECK_ALL_OK.getBytes();
                        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                            InetAddress.getByName(coordinatorServerHost), coordinatorServerPort);
                        clientSocket.send(sendPacket);

                        sendBuffer = new byte[1024];
                    } else if (receivedMessage.startsWith(STREAM)) {
                        if (receivedMessage.startsWith(STREAM_ON)) {
                            // Turn on video stream
                            executorService.submit(new RyzeTelloRaspberryPiVideoClient());

                            // Send streamon command to drone
                            sendBuffer = receivedMessage.getBytes();
                            sendPacket =
                                new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost),
                                    dronePort);
                            clientSocket.send(sendPacket);

                            sendBuffer = new byte[1024];
                        } else if (receivedMessage.startsWith(STREAM_OFF)) {
                            // Send streamon command to drone
                            sendBuffer = receivedMessage.getBytes();
                            sendPacket =
                                new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost),
                                    dronePort);
                            clientSocket.send(sendPacket);

                            sendBuffer = new byte[1024];

                            // Turn off video stream
                            executorService.shutdown();
                        }
                    } else {
                        // Send command to drone
                        sendBuffer = receivedMessage.getBytes();
                        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost),
                            dronePort);
                        clientSocket.send(sendPacket);

                        sendBuffer = new byte[1024];
                    }
                } else if (receivePacket.getAddress().getHostAddress().equals(droneHost)) {
                    if (lastReceivedDroneStateTimestamp == -1) {
                        scheduledExecutorService
                            .scheduleAtFixedRate(new DroneHealthCheckRunnable(), DRONE_HEALTH_CHECK_DELAY_SECONDS,
                                DRONE_HEALTH_CHECK_DELAY_SECONDS, TimeUnit.SECONDS);
                    }

                    lastReceivedDroneStateTimestamp = System.currentTimeMillis();
                }

                receiveBuffer = new byte[1024];
            }
        } catch (IOException e) {
            System.err.println("Socket error. Shutting down...");

            // TODO??: Send HEALTH_CHECK_DRONE_OFFLINE to coordinator

            System.exit(1);
        }

    }

    public static void main(String[] args) {
        RyzeTelloRapberryPiClient client = new RyzeTelloRapberryPiClient();

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
                try {
                    System.out.println("Sending message to coordinator: " + HEALTH_CHECK_DRONE_OFFLINE);
                    sendBuffer = HEALTH_CHECK_DRONE_OFFLINE.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                        InetAddress.getByName(coordinatorServerHost), coordinatorServerPort);
                    clientSocket.send(sendPacket);

                    sendBuffer = new byte[1024];
                } catch (IOException e) {
                    System.out.println("Error while sending 'drone offline' message to coordinator.");
                }
            }
        }

    }

    private class RyzeTelloRaspberryPiVideoClient implements Runnable {

        @Override
        public void run() {
            try (DatagramSocket clientVideoSocket = new DatagramSocket(DEFAULT_RASPBERRY_PI_CLIENT_VIDEO_PORT)) {

                // Wait for stream data
                while (true) {
                    DatagramPacket receiveVideoPacket =
                        new DatagramPacket(receiveVideoBuffer, receiveVideoBuffer.length);
                    clientVideoSocket.receive(receiveVideoPacket);

                    byte[] receivedVideoData = receiveVideoPacket.getData();
                    System.out
                        .println(":::::::VIDEO::::::: Client received video data, size: " + receivedVideoData.length);

                    receiveVideoBuffer = new byte[1024];

                    // Send video to coordinator server
                    sendVideoBuffer = receivedVideoData;
                    DatagramPacket sendVideoPacket = new DatagramPacket(sendVideoBuffer, sendVideoBuffer.length,
                        InetAddress.getByName(coordinatorServerHost), coordinatorServerVideoPort);
                    clientVideoSocket.send(sendVideoPacket);

                    sendVideoBuffer = new byte[1024];
                }
            } catch (IOException e) {
                System.err.println("Socket error. Shutting down...");
                System.exit(1);
            }

        }

    }

}
