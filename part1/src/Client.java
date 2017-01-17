import java.io.*;
import java.net.Socket;

/**
 * Created by Damir Zhaksilikov on 1/16/2017.
 */
public class Client {
    private String hostName;
    private int portNumber;

    public Client(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public String FormatData(String data, int pSecret, int step, int SSN) throws UnsupportedEncodingException {
        byte[] bytes = data.getBytes("UTF-8");
        int length = bytes.length;
        return null;
    }

    public void Write(String data) {
        try {
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            String userInput;
            out.println(data);
            System.out.println(in.readLine());

        } catch (Exception e) {
        }

    }
}
