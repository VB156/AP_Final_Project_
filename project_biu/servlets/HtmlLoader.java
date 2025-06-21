package servlets;

import server.RequestParser.RequestInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * This class is used to handle incoming requests related to HTML file loading.
 */
public class HtmlLoader implements Servlet {

    //------------------------------------------------------------------------------------------------------------------
    // Class variables:
    //------------------------------------------------------------------------------------------------------------------

    private final String m_htmlFolder;

    //------------------------------------------------------------------------------------------------------------------
    // Class methods:
    //------------------------------------------------------------------------------------------------------------------

    /**
     * this constructor initializes the HtmlLoader object.
     * @param htmlFolder the folder containing the HTML files.
     */
    public HtmlLoader(String htmlFolder) {
        this.m_htmlFolder = htmlFolder;
    }

    /**
     * this method handles the incoming request related to HTML file loading.
     * @param requestInfo the RequestInfo object containing information about the incoming request.
     * @param toClient the OutputStream to which the response will be written.
     * @throws IOException if an I/O error occurs while writing to the OutputStream.
     */
    @Override   
    public void handle(RequestInfo requestInfo, OutputStream toClient) throws IOException {
        String[] uriSegments = requestInfo.getUriSegments();
        if (uriSegments.length > 1) {
            uriSegments = Arrays.copyOfRange(uriSegments, 1, uriSegments.length);
        } else {
            uriSegments = new String[]{"index.html"};
        }

        Path requestedPath = Paths.get(m_htmlFolder, uriSegments);

        if (Files.exists(requestedPath)) {
            sendFile(requestedPath, toClient);
        } else if (Files.isRegularFile(requestedPath)) {
            sendFile(requestedPath, toClient);
        } else {
            sendNotFound(toClient);
        }
    }

    /**
     * this method sends a file to the client using HTTP 200 OK response.
     * @param filePath the path to the file to be sent.
     * @param toClient the OutputStream to which the response will be written.
     * @throws IOException if an I/O error occurs while reading the file or writing to the OutputStream.
     */
    private void sendFile(Path filePath, OutputStream toClient) throws IOException {
        byte[] content = Files.readAllBytes(filePath);
        toClient.write(("HTTP/1.1 200 OK\r\n").getBytes());
        toClient.write(("Content-Type: text/").getBytes());
        toClient.write(filePath.getFileName().toString().split("\\.")[1].toLowerCase().getBytes()); // hml, css, ...
        toClient.write(("\r\n").getBytes());
        toClient.write(("Content-Length: " + content.length + "\r\n").getBytes());
        toClient.write(("\r\n").getBytes());
        toClient.write(content);
    }


    /**
     * this method sends a 404 Not Found HTTP response to the client.
     * @param toClient the OutputStream to which the response will be written.
     * @throws IOException if an I/O error occurs while writing to the OutputStream.
     */
    private void sendNotFound(OutputStream toClient) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n";
        toClient.write(response.getBytes());
        toClient.flush();
    }

    @Override   
    public void close() throws IOException {
        // do nothing
    }
}
