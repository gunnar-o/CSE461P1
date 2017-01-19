import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.*;

public class Client {
    private String hostName;
    private int portNumber;

    public Client(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public byte[] FormatMessage(String data, int pSecret, short step, short SSN) {
        int length = data.length();
        ByteBuffer message = ByteBuffer.allocate(12 + data.length() + 4 - data.length() % 4);
        message.order( ByteOrder.BIG_ENDIAN);
        message.putInt(data.length() + 1);
        message.putInt(pSecret);
        message.putShort(step);
        message.putShort(SSN);
        for(int i = 0; i < data.length(); i++) {
            message.put((byte) (data.charAt(i) & 0xFF));
        }
        message.put((byte) ('\0' & 0xFF));
        return message.array();
    }

    public byte[] SendUDP(byte[] message) {

        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(hostName);
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, IPAddress, portNumber);
            clientSocket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            clientSocket.close();
            return receiveData;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }
}


