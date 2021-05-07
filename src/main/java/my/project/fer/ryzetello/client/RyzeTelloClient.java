package my.project.fer.ryzetello.client;

import my.project.fer.ryzetello.constants.MessageConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client used to test if UDP Server on drone receives commands
 */
public class RyzeTelloClient implements Runnable {

    private static final String DEFAULT_DRONE_HOST = "127.0.0.1";
    private static final int DEFAULT_DRONE_PORT = 50000;

    private final String droneHost;
    private final int dronePort;

    private final int port;
    private byte[] receiveBuffer = new byte[1024];
    private byte[] sendBuffer = new byte[1024];

    public RyzeTelloClient(int port) {

        final String envHost = System.getenv("RYZE_TELLO_DRONE_HOST");
        final String envPort = System.getenv("RYZE_TELLO_DRONE_PORT");

        this.droneHost = envHost == null ? DEFAULT_DRONE_HOST : envHost;
        this.dronePort = envPort == null ? DEFAULT_DRONE_PORT : Integer.parseInt(envPort);
        this.port = port;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String userInput;

        try (DatagramSocket clientSocket = new DatagramSocket(port)) {
            clientSocket.setSoTimeout(3000);

            // REGISTER
            sendBuffer = MessageConstants.REGISTER.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost), dronePort);
            clientSocket.send(sendPacket);

            do {
                System.out.println("Enter command:");
                userInput = scanner.nextLine();

                sendBuffer = userInput.getBytes();

                sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(droneHost), dronePort);
                clientSocket.send(sendPacket);

                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(receivePacket);

                String receivedMessage = new String(receivePacket.getData());
                System.out.println("Client received response: " + receivedMessage);
            }
            while (!userInput.equalsIgnoreCase("exit"));

        } catch (IOException e) {
            System.err.println("Socket error. Shutting down...");
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        int clientPort = 8890;
        RyzeTelloClient client = new RyzeTelloClient(clientPort);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(client);
    }

}
