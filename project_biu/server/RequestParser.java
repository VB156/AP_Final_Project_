package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to parse an HTTP request and extract relevant information.
 */
public class RequestParser {

    //------------------------------------------------------------------------------------------------------------------
    // Inner class:
    //------------------------------------------------------------------------------------------------------------------

    /**
     * A class representing parsed information from an HTTP request.
     */
    public static class RequestInfo {
        private final String m_httpCommand;
        private final String m_uri;
        private final String[] m_uriSegments;
        private final Map<String, String> m_parameters;
        private final byte[] m_content;

        /**
         * this constructor initializes the RequestInfo object.
         * 
         * @param httpCommand the HTTP command (e.g., GET, POST).
         * @param uri the full URI of the request.
         * @param uriSegments the segments of the URI path.
         * @param parameters the parameters extracted from the URI or request body.
         * @param content the content of the request, if any.
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.m_httpCommand = httpCommand;
            this.m_uri = uri;
            this.m_uriSegments = uriSegments;
            this.m_parameters = parameters;
            this.m_content = content;
        }

        public String getHttpCommand() {
            return m_httpCommand;
        }

        public String getUri() {
            return m_uri;
        }

        public String[] getUriSegments() {
            return m_uriSegments;
        }

        public Map<String, String> getParameters() {
            return m_parameters;
        }

        public byte[] getContent() {
            return m_content;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Class methods:
    //------------------------------------------------------------------------------------------------------------------

    /**
     * this method parses an HTTP request and extracts relevant information.
     * @param reader the BufferedReader containing the HTTP request.
     * @return a RequestInfo object containing the parsed request information, or null if the request is invalid.
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String lineFromReader = reader.readLine();
        String httpCommandParsed;
        String uriParsed;
        String boundary = "";
        String[] uriSegments;
        Map<String, String> parameters = new HashMap<>();
        byte[] content;

        if (lineFromReader == null || lineFromReader.isEmpty())
            return null;

        String[] requestLineParts = lineFromReader.split(" ");
        if (requestLineParts.length < 3)
            return null;

        httpCommandParsed = requestLineParts[0];
        uriParsed = requestLineParts[1];

        // Extract URI segments
        String path = uriParsed.split("\\?")[0];
        uriSegments = Arrays.stream(path.split("/")).filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);

        // Parse parameters from URI
        if (uriParsed.contains("?")) {
            String queryString = uriParsed.split("\\?")[1];
            String[] queryParams = queryString.split("&");
            for (String param : queryParams) {
                String[] paramParts = param.split("=");
                if (paramParts.length == 2) {
                    parameters.put(paramParts[0], paramParts[1]);
                }
            }
        }

        // read until empty line
        while (reader.ready()) {
            lineFromReader = reader.readLine();
            if (lineFromReader.isEmpty())
                break;
            if (lineFromReader.contains("boundary=")) {
                boundary = lineFromReader.split("boundary=")[1];
            }
        }

        // read until empty line
        while (reader.ready()) {
            lineFromReader = reader.readLine();
            if (lineFromReader.isEmpty())
                break;
            if (lineFromReader.contains("filename=")){
                parameters.put("filename", lineFromReader.split("filename=")[1].replace("\"",""));
                continue;
            }
            String[] paramParts = lineFromReader.split("=");
            if (paramParts.length == 2) {
                parameters.put(paramParts[0], paramParts[1]);
            }
        }

        // Read content after the second empty line
        StringBuilder contentBuilder = new StringBuilder();
        while (reader.ready()) {
            lineFromReader = reader.readLine();
            if (!boundary.isEmpty() && lineFromReader.contains(boundary))
                break;

            contentBuilder.append(lineFromReader).append("\n");
        }
        if(!contentBuilder.isEmpty())
            contentBuilder.deleteCharAt(contentBuilder.length() - 1);
        content = contentBuilder.toString().getBytes();

        return new RequestInfo(httpCommandParsed, uriParsed, uriSegments, parameters, content);
    }

    
}
