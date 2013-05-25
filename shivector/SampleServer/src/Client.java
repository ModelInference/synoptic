import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }

    public void run() throws UnknownHostException, IOException,
            ClassNotFoundException {
        Socket requestSocket = new Socket("localhost", Util.SERVER_PORT);
        System.out.println("Connected to server at port " + Util.SERVER_PORT);

        System.out.println("getting outputstream");
        ObjectOutputStream out = new ObjectOutputStream(
                requestSocket.getOutputStream());
        out.flush();

        ObjectInputStream in = new ObjectInputStream(
                requestSocket.getInputStream());

        String message = (String) in.readObject();
        System.out.println("Message received:" + message);

        sendMessage(out, "Hi server");
        message = (String) in.readObject();
        System.out.println("Message received:" + message);

        sendMessage(out, "bye");
        message = (String) in.readObject();
        System.out.println("Message received:" + message);

        System.out.println("Closing connection");
        in.close();
        out.close();
        requestSocket.close();
    }

    /** Convert a string to a byte[] */
    public static byte[] stringToByteArray(String msg) {
        return msg.getBytes();
    }

    /** Convert a byte[] to a string */
    public static String byteArrayToString(byte[] msg, int length) {
        if (msg == null)
            return null;
        return new String(msg, 0, length);
    }

    public static void sendMessage(ObjectOutputStream out, String msg) {
        try {
            System.out.println("sending>" + msg);
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
