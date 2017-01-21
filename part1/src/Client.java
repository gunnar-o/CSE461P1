import org.omg.CORBA.*;

import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.nio.*;
import java.util.Arrays;

public class Client {
    private String hostName;
    private Socket tcpSocket;

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

    public byte[] SendUDP(byte[] message, int portNumber, int receiveCount) {
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
                    for(int i = 1; i <= receiveCount; i++)
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

    public void OpenTCP(int portNumber) {
        try{
            tcpSocket = new Socket(hostName, portNumber);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void CloseTCP() {
        try {
            tcpSocket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public byte[] ReadTCP(int length) {
        try {
            byte[] response = new byte[length];
            DataInputStream clientRead = new DataInputStream(tcpSocket.getInputStream());
            clientRead.readFully(response);
            return response;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void SendTCP(byte[] message, int count) {
        try {
            for(int i = 0; i < count; i++) {
                System.out.println("Sending TCP Packet (" + i + ") to: " + hostName + " (Port: " + tcpSocket.getPort() + ")");
                System.out.println(message.length);
                System.out.println("Packet: " + Arrays.toString(message));
                DataOutputStream clientWrite = new DataOutputStream(tcpSocket.getOutputStream());
                clientWrite.write(message);
                clientWrite.flush();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
