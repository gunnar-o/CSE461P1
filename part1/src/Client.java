import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.*;
import java.util.Arrays;

public class Client {
    private String hostName;

    public Client(String hostName) {
        this.hostName = hostName;
    }

    public ByteBuffer FormatHeader(int length, int pSecret, short step, short SSN) {
        ByteBuffer message = length % 4 == 0 ? ByteBuffer.allocate((12 + length)) :
                ByteBuffer.allocate(12 + length + 4 - length % 4);
        message.order( ByteOrder.BIG_ENDIAN);
        message.putInt(length);
        message.putInt(pSecret);
        message.putShort(step);
        message.putShort(SSN);
        return message;
    }

    public byte[] SendUDP(byte[] message, int portNumber, int count) {
        try {
            System.out.println("Sending UDP Packet to: " + hostName + " (Port: " + portNumber + ")");
            System.out.println("Packet: " + Arrays.toString(message));

            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(1000);
            InetAddress IPAddress = InetAddress.getByName(hostName);
            byte[] receiveData = new byte[1024];

            DatagramPacket sendPacket = new DatagramPacket(message, message.length, IPAddress, portNumber);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (true) {
                try {
                    for(int i = 0; i <= count; i++)
                        clientSocket.receive(receivePacket);
                    clientSocket.close();
                    return receiveData;
                } catch (SocketTimeoutException e) {
                    clientSocket.send(sendPacket);
                    continue;
                }
            }

        } catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}



