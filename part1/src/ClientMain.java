import java.nio.ByteBuffer;
import java.util.Arrays;

public class ClientMain {
    static final private String HOST_NAME = "attu2.cs.washington.edu";
    static final private short step = 1;
    static final private short SSN = 997;

    public static void main(String[] args) {
        Client client = new Client(HOST_NAME);
        int[] responseA = PartA(client);
        int[] responseB = PartB(client, responseA);
    }

    private static int[] PartA(Client client) {
        System.out.println("-- START: 1A --");
        String data = "hello world";
        ByteBuffer message = client.FormatHeader(data.length() + 1, 0, step, SSN);

        for(int i = 0; i < data.length(); i++) {
            message.put((byte) (data.charAt(i) & 0xFF));
        }
        message.put((byte) ('\0' & 0xFF));

        byte[] response = client.SendUDP(message.array(), 12235, 0);
        ByteBuffer b = ByteBuffer.wrap(response, 12, 16);
        int[] responseA = new int[4];
        for(int i = 0; i < 4; i++) {
            responseA[i] = b.getInt();
        }
        System.out.println("-- RESPONSE 1A --");
        System.out.println("num: " + responseA[0]);
        System.out.println("len: " + responseA[1]);
        System.out.println("udp_port: " + responseA[2]);
        System.out.println("secretA: " + responseA[3]);
        System.out.println();
        return responseA;
    }


    private static int[] PartB(Client client, int[] responseA) {
        System.out.println("-- START: 1B --");
        byte[] response =  null;
        for(int i = 0; i < responseA[0]; i++) {
            ByteBuffer message = client.FormatHeader(responseA[1] + 4, responseA[3], step, SSN);
            message.putInt(i);
            int count = i == responseA[0] - 1 ? 1 : 0;
            response = client.SendUDP(message.array(), responseA[2], count);
            System.out.println("Acknowledge: Success");
        }

        ByteBuffer b = ByteBuffer.wrap(response, 12, 16);
        int[] responseB = new int[2];
        for(int i = 0; i < 4; i++) {
            responseA[i] = b.getInt();
        }

        System.out.println("-- RESPONSE 1B --");
        System.out.println("tcp_port: " + responseA[0]);
        System.out.println("secretB: " + responseA[1]);
        System.out.println();
        return responseB;
    }
}
