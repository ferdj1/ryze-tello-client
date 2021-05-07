package my.project.fer.ryzetello.server.mock;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UDP Server mock, used as temporary replacement for Ryze Tello drone
 */
public class RyzeTelloServerMock implements Runnable {

    private static final int SERVER_PORT = 50001;
    private byte[] receiveBuffer = new byte[1024];
    private byte[] sendBuffer = new byte[1024];

    private RyzeTelloCommunicatorService ryzeTelloCommunicatorService;

    public RyzeTelloServerMock() {
        this.ryzeTelloCommunicatorService = new RyzeTelloCommunicatorServiceImpl();
    }

    @Override
    public void run() {
        System.out.println("Started server on port " + SERVER_PORT);
        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                String receivedCommand = new String(receivePacket.getData()).replaceAll("\0", "");
                System.out.println("Drone server received command: " + receivedCommand);

                String response = ryzeTelloCommunicatorService.execute(receivedCommand);

                // Buffer cleanup
                receiveBuffer = new byte[1024];

                InetAddress ipAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                DatagramPacket sendPacket = new DatagramPacket(response.getBytes(),
                    receivedCommand.getBytes().length, ipAddress, port);

                //serverSocket.send(sendPacket);
            }
        } catch (IOException e) {
            System.err.println("Socket error. Shutting down server...");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        RyzeTelloServerMock server = new RyzeTelloServerMock();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(server);
    }

}
