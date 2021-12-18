import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) {
        try {
            new EchoClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
