package server;

import servlets.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

/**
 * This class represents a simple HTTP server that can handle multiple client connections concurrently.
 */
public class MyHTTPServer extends Thread implements HTTPServer {

    //------------------------------------------------------------------------------------------------------------------
    // Class variables:
    //------------------------------------------------------------------------------------------------------------------
    private final int m_portNumber;
    private final int m_numberOfThreads;
    private ServerSocket m_serverSocket;
    private boolean m_running;
    private final ExecutorService m_executor;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Servlet>> m_servletsMap;

    //------------------------------------------------------------------------------------------------------------------
    // Class methods:
    //------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor initializes the server with the given port number and number of threads.
     *
     * @param portNumber     the port number on which the server will listen for incoming connections.
     * @param numberOfThreads the number of threads in the thread pool.
     */
    public MyHTTPServer(int portNumber, int numberOfThreads) {
        this.m_portNumber = portNumber;
        this.m_numberOfThreads = numberOfThreads;
        this.m_executor = Executors.newFixedThreadPool(numberOfThreads);
        
        this.m_servletsMap = new ConcurrentHashMap<>();
        this.m_servletsMap.put("GET", new ConcurrentHashMap<>());
        this.m_servletsMap.put("POST", new ConcurrentHashMap<>());
        this.m_servletsMap.put("DELETE", new ConcurrentHashMap<>());
    }

    /**
     * this method adds a servlet to the server.
     *
     * @param httpCommand the HTTP command (GET, POST, DELETE) that the servlet will handle.
     * @param uri         the URI pattern that the servlet will match.
     * @param servlet     the servlet instance to be added.
     */
    public void addServlet(String httpCommand, String uri, Servlet servlet) {
        this.m_servletsMap.get(httpCommand.toUpperCase()).put(uri, servlet);
    }

    /**
     * Removes a servlet from the server.
     *
     * @param httpCommand the HTTP command (GET, POST, DELETE) that the servlet was handling.
     * @param uri         the URI pattern that the servlet was matching.
     */
    public void removeServlet(String httpCommand, String uri) {
        this.m_servletsMap.get(httpCommand.toUpperCase()).remove(uri);
    }

    /**
     *this method starting the server and begins listening for incoming connections.
     */
    public void run() {
        try {
            m_serverSocket = new ServerSocket(m_portNumber);
            // set the timeout for the server socket
            m_serverSocket.setSoTimeout(1000);

            while (m_running) {
                try {
                    Socket clientSocket = m_serverSocket.accept();
                    m_executor.submit(() -> handleClient(clientSocket));
                    
                } catch (SocketTimeoutException | SocketException ignored) {
                } catch (IOException e) {
                    // if an error occurs, close the server socket
                    e.printStackTrace();
                    if (m_serverSocket != null && !m_serverSocket.isClosed())
                        m_serverSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method starts the server.
     */
    @Override
    public void start() {
        this.m_running = true;
        new Thread(this).start();
    }

    /**
     *this method stops the server.
     */
    public void close() {
        this.m_running = false;
        // shutdown the executor
        m_executor.shutdown();
        try {
            if (!m_executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                m_executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            m_executor.shutdownNow();
        }
        // close the server socket
        try {
            if (m_serverSocket != null) {
                m_serverSocket.close();
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * this method handles an incoming client connection by parsing the request, finding the appropriate servlet,
     * and invoking the servlet's handle method.
     *
     * @param clientSocket the socket connected to the client.
     */
    private void handleClient(Socket clientSocket) {
        try {
            // parse the request
            RequestInfo requestInfo = RequestParser.parseRequest(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
            if (requestInfo == null) {
                sendNotFound(clientSocket.getOutputStream());
                return;
            }
            // find the appropriate servlet
            Servlet servlet = findServlet(requestInfo);
            if (servlet != null) {
                servlet.handle(requestInfo, clientSocket.getOutputStream());
            } else {
                sendNotFound(clientSocket.getOutputStream());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close the client socket
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * this method finds the right servlet for a given request by matching the request's HTTP command and URI.
     * @param requestInfo the parsed request information.
     * @return the servlet that matches the request, or null if no matching servlet is found.
     */
    private Servlet findServlet(RequestInfo requestInfo) {
        // get the map of servlets for the given HTTP command
        ConcurrentHashMap<String, Servlet> map = m_servletsMap.get(requestInfo.getHttpCommand().toUpperCase());
        // get the URI from the request
        String uri = requestInfo.getUri().split("\\?")[0];

        // loop through the URI to find the right servlet
        while (!uri.isEmpty()) {
            Servlet servlet = map.get(uri);
            if (servlet != null) {
                return servlet;
            }
            int lastSlash = uri.lastIndexOf('/');
            if (lastSlash == -1) {
                break;
            } else if (lastSlash == uri.length() - 1) {
                uri = uri.substring(0, lastSlash);
            } else
                uri = uri.substring(0, lastSlash + 1);
        }
        return null;
    }

    /**
     * this method sends a 404 Not Found response to the client.
     * @param out the output stream to send the response.
     * @throws IOException if an error occurs while writing to the output stream.
     */
    private void sendNotFound(OutputStream out) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n";
        out.write(response.getBytes());
        out.flush();
    }
}
