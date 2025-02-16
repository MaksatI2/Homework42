import server.EchoServer;

public class Main {
    public static void main(String[] args) {
        EchoServer.bindToPort(4040).run();
    }
}