import server.HTTPServer;
import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    private static volatile boolean stop = false;
    private static final int PORT = 1234;
    private static final int N_THREADS = 5;

    public static void main(String[] args) {
        HTTPServer server = setupServer();
        server.start();

        printServerAddress();
        waitForShutdown();

        server.close();
        System.out.println("Server stopped successfully.");
    }

    /**
     * Creates and configures the HTTP server with all the required servlets.
     * @return A fully configured HTTPServer instance.
     */
    private static HTTPServer setupServer() {
        HTTPServer server = new MyHTTPServer(PORT, N_THREADS);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.addServlet("POST", "/upload", new ConfLoader());
        return server;
    }

    /**
     * Prints the server's listening address to the console.
     */
    private static void printServerAddress() {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server is listening on http://" + ipAddress + ":" + PORT + "/app/index.html");
        } catch (UnknownHostException e) {
            System.err.println("Error: Could not determine local IP address.");
            e.printStackTrace();
        }
    }

    /**
     * Blocks and waits for the user to type 'x' to shut down the server.
     */
    private static void waitForShutdown() {
        System.out.println("Enter 'x' and press Enter to stop the server.");
        while (!stop) {
            try {
                if (System.in.read() == 'x') {
                    stop = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                stop = true; // Stop on error
            }
        }
    }
}


