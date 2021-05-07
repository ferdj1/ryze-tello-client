package my.project.fer.ryzetello.client;

import my.project.fer.ryzetello.constants.MessageConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client placed on RaspberryPi that registers drone on the coordinator server and sends commands from that server to the drone
 */
public class RyzeTelloRapberryPiClient implements Runnable {

    private static final String DEFAULT_COORDINATOR_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_COORDINATOR_SERVER_PORT = 50000;
    private static final String DEFAULT_DRONE_HOST = "127.0.0.1";
    private static final int DEFAULT_DRONE_PORT = 50001;


    private final String coordinatorServerHost;
    private final int coordinatorServerPort;

    private final String droneHost;
    private final int dronePort;

    private byte[] receiveBuffer = new byte[1024];
    private byte[] sendBuffer = new byte[1024];

    public RyzeTelloRapberryPiClient() {
        final String envCoordinatorHost = System.getenv("RYZE_TELLO_COORDINATOR_SERVER_HOST");
        final String envCoordinatorPort = System.getenv("RYZE_TELLO_COORDINATOR_SERVER_PORT");

        this.coordinatorServerHost = envCoordinatorHost == null ? DEFAULT_COORDINATOR_SERVER_HOST : envCoordinatorHost;
        this.coordinatorServerPort = envCoordinatorPort == null ? DEFAULT_COORDINATOR_SERVER_PORT : Integer.parseInt(envCoordinatorPort);


        final String envDroneHost = System.getenv("RYZE_TELLO_DRONE_HOST");
        final String envDronePort = System.getenv("RYZE_TELLO_DRONE_PORT");

        this.droneHost = envDroneHost == null ? DEFAULT_DRONE_HOST : envDroneHost;
        this.dronePort = envDronePort == null ? DEFAULT_DRONE_PORT : Integer.parseInt(envDronePort);
    }

    @Override
    public void run() {
        try (DatagramSocket clientSocket = new DatagramSocket(12345)) {
            //System.out.printf("RaspPi Client started on %s:%d.\n", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());

            // Register phase
            final String registerMessage = MessageConstants.REGISTER;

            sendBuffer = registerMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(coordinatorServerHost), coordinatorServerPort);
            clientSocket.send(sendPacket);
            sendBuffer = new byte[1024];
            //

            // Wait for commands
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(receivePacket);

                String receivedMessage = new String(receivePacket.getData()).replaceAll("\0", "");
                System.out.println("Client received message: " + receivedMessage);

                if (receivePacket.getAddress().getHostAddress().equals(coordinatorServerHost)) {
                    // Send command to drone
                    sendBuffer = receivedMessage.getBytes();
                    sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost), dronePort);
                    clientSocket.send(sendPacket);

                    sendBuffer = new byte[1024];
                }

                receiveBuffer = new byte[1024];
            }
        } catch (IOException e) {
            System.err.println("Socket error. Shutting down...");
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        RyzeTelloRapberryPiClient client = new RyzeTelloRapberryPiClient();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(client);
    }

}