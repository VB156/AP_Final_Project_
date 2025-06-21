package servlets;

import configs.GenericConfig;
import graph.Graph;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;

import java.io.*;


/**
 * This class is used to handle incoming requests related to configuration loading.
 */
public class ConfLoader implements Servlet {

    //------------------------------------------------------------------------------------------------------------------
    // Class methods:
    //------------------------------------------------------------------------------------------------------------------

    /**
     * this method sends the content to the client using the provided OutputStream.
     * 
     * @param content the array of strings representing the content to be sent to the client.
     * @param toClient the OutputStream to which the content will be written.
     * @throws IOException if an I/O error occurs while writing to the OutputStream.
     */
    private void sendContent(String[] content, OutputStream toClient) throws IOException {
        toClient.write(("HTTP/1.1 200 OK\r\n").getBytes());
        toClient.write(("Content-Type: text/html\r\n").getBytes());
        toClient.write(("\r\n").getBytes());
        for (String line : content)
            toClient.write(line.getBytes());
    }


    /** 
     * this method handles the incoming request related to configuration loading.
     * 
     * @param requestInfo the RequestInfo object containing information about the incoming request.
     * @param toClient the OutputStream to which the response will be written.
     * @throws IOException if an I/O error occurs while writing to the OutputStream.
     */
    @Override
    public void handle(RequestInfo requestInfo, OutputStream toClient) throws IOException {
        // Check if the request contains a filename parameter
        if (!requestInfo.getParameters().containsKey("filename"))
            return;
        String filename = requestInfo.getParameters().get("filename");

        // Write the request content to the specified configuration file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("config_files/" + filename))) {
            writer.write(new String(requestInfo.getContent(), 0));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Get the topic manager and clear it
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        topicManager.clear();

        // Create a new GenericConfig object and set its configuration file
        GenericConfig config = new GenericConfig();
        config.setConfFile("config_files/" + filename);
        config.create();

        // Create a new graph based on the topics in the configuration
        Graph topicsGraph = new Graph();
        topicsGraph.createFromTopics();

        // Generate HTML content for the graph and send
        String[] content = HtmlGraphWriter.getGraphHTML(topicsGraph, "html_files/graph.html");
        sendContent(content, toClient);
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
