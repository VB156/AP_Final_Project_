package views;

import graph.Graph;
import graph.Node;

/**
 * This class is used to generate an HTML representation of a given graph.
 */
public class HtmlGraphWriter {

    //------------------------------------------------------------------------------------------------------------------
    // Class methods:
    //------------------------------------------------------------------------------------------------------------------

    /**
     * this method generates an HTML representation of a given graph.
     * @param graph the graph to be represented.
     * @param filePath the path to the HTML file to be used as a template.
     * @return an array of strings representing the HTML representation of the graph.
     */
    public static String[] getGraphHTML(Graph graph, String filePath) {
        String htmlTemplate = HtmlReader.readHtmlFile(filePath);
        if (htmlTemplate == null) {
            return new String[]{""};
        }

        String graphJson = GraphToJson(graph);
        String modifiedHtml = htmlTemplate.replace("\"GRAPH_DATA\"", graphJson);
        return modifiedHtml.split("\n");
    }


    /**
     * this method converts a given graph into a JSON string representation for the use of the graph.js file.
     * @param graph the graph to be converted.
     * @return a JSON string representing the graph.
     */
    static String GraphToJson(Graph graph) {
        StringBuilder json = new StringBuilder("[");
        for (Node node : graph) {
            json.append("{");
            json.append("\"id\":\"").append(node.toString().split("@")[1]).append("\",\n");
            String type = "";
            String name = node.getName();
            if (name.charAt(0) == 'T') {
                type = "Topic";
                name = name.substring(1);
            } else if (name.charAt(0) == 'A') {
                type = "Agent";
                name = name.split("\\.")[1];
            }
            json.append("\"type\":\"").append(type).append("\",\n");
            json.append("\"name\":\"").append(name).append("\",\n");
            json.append("\"edges\":[");
            boolean first = true;
            for (Node edge : node.getEdges()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(edge.toString().split("@")[1]).append("\"");
                first = false;
            }

            json.append("]");
            json.append("},\n");
        }
        json.append("]");
        return json.toString();
    }
}
