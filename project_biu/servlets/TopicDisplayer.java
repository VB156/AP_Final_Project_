package servlets;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;
import views.HtmlReader;

import java.io.IOException;
import java.io.OutputStream;


/**
 * This class is used to handle incoming requests related to topic display.
 */
public class TopicDisplayer implements Servlet {

    //------------------------------------------------------------------------------------------------------------------
    // Class methods:
    //------------------------------------------------------------------------------------------------------------------

    /**
     * this method handles the incoming request related to topic display.
     * @param requestInfo the RequestInfo object containing information about the incoming request.
     * @param toClient the OutputStream to which the response will be written.
     * @throws IOException if an I/O error occurs while writing to the OutputStream.
     */
    @Override
    public void handle(RequestInfo requestInfo, OutputStream toClient) throws IOException {
        sendMessage(requestInfo.getParameters().get("topic"), requestInfo.getParameters().get("message"));

        String content = HtmlReader.readHtmlFile("html_files/values.html");

        String tableContent = createTable();
        String valuesContent = createValuesMap();
        content = content != null ? content.replace("<!--PLACE_TABLE-->", tableContent) : null;
        content = content != null ? content.replace("\"PLACE_VALUES\":\"VALUES\"", valuesContent) : null;
        sendContent(content.split("\n"), toClient);
    }

    /**
     * this method sends a message to a specific topic in the graph.
     * @param topic the name of the topic to which the message will be sent.
     * @param msg the content of the message to be sent.
     */
    private void sendMessage(String topic, String msg) {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        for (Topic t : tm.getTopics()) {
            if (t.m_topicName.equals(topic)) {
                t.publish(new Message(msg));
                break;
            }
        }
    }

    /**
     * this function generates a string representation of the topics and their latest messages,
     * formatted as an HTML table.
     * 
     * @return a string containing the HTML representation of the topics and their latest messages.
     * each row in the table represents a topic, with the topic name and the latest message displayed.
     */
    private String createTable() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        StringBuilder tableContent = new StringBuilder();
        for (Topic topic : tm.getTopics()) {
            tableContent.append("<tr><td>");
            tableContent.append(topic.m_topicName);
            tableContent.append("</td><td>");
            Message msg;
            if ((msg = topic.getLastMsg()) != null) {
                if (Double.isNaN(msg.asDouble))
                    tableContent.append(msg.asText);
                else
                    tableContent.append(String.format("%.02f", msg.asDouble));
            }
            tableContent.append("</td></tr>");
        }
        return tableContent.toString();
    }


    /**
     * this method generates a string representation of the latest messages for each topic,
     * formatted as a JSON object.
     * 
     * @return a string containing the latest messages for each topic, formatted as a JSON object.
     * the keys are the topic IDs, and the values are the corresponding latest messages.
     */
    private String createValuesMap() {
        TopicManagerSingleton.TopicManager tm = TopicManagerSingleton.get();
        StringBuilder valuesContent = new StringBuilder();
        for (Topic topic : tm.getTopics()) {
            if (topic.getLastMsg() == null)
                continue;

            if (Double.isNaN(topic.getLastMsg().asDouble))
                valuesContent.append(String.format("\"%s\": %s,\n", topic.getId(), topic.getLastMsg().asText));
            else {
                valuesContent.append(String.format("\"%s\": %.02f,\n", topic.getId(), topic.getLastMsg().asDouble));
            }
        }
        return valuesContent.toString();
    }


    /**
     * this method sends the content to the client using the provided output stream.
     * @param content the content to be sent to the client.
     * @param toClient the output stream to which the content will be written.
     * @throws IOException if an I/O error occurs while writing to the output stream.
     */
    private void sendContent(String[] content, OutputStream toClient) throws IOException {
        toClient.write(("HTTP/1.1 200 OK\r\n").getBytes());
        toClient.write(("Content-Type: text/html\r\n").getBytes());
        toClient.write(("\r\n").getBytes());
        for (String line : content)
            toClient.write(line.getBytes());
    }

    @Override   
    public void close() throws IOException {
        // do nothing
    }

}
