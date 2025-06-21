package views;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class provides a method to read an HTML file and return its content as a string.
 */
public class HtmlReader {

    /**
     * Reads an HTML file and returns its content as a string.
     *
     * @param filePath The path to the HTML file to be read.
     * @return The content of the HTML file as a string, or {@code null} if an error occurs during reading.
     */
    public static String readHtmlFile(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                contentBuilder.append(currentLine).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return contentBuilder.toString();
    }
}
