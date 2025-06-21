package configs;

import graph.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

/**
 * This class implements the Config interface and is used to create and manage agents based on a configuration file.
 */
public class GenericConfig implements Config {
//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------

    private String m_filename;
    private Set<Agent> m_listAgents;

//------------------------------------------------------------------------------------------------------------------
// Class methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * this method dynamically creates an instance of a specified agent class using reflection.
     * @param className The fully qualified name of the agent class to be instantiated.
     * @param subcribedTopics An array of subscription topic names.
     * @param publishedTopics An array of publication topic names.
     * @return An instance of the specified agent class
     */
    private static Object dynamicallyCreateAgentClass(String className, String[] subcribedTopics, String[] publishedTopics) {
        try {
            // Load the class dynamically

            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor(new Class[]{String.class, String[].class, String[].class});
            Object o = constructor.newInstance(className, subcribedTopics, publishedTopics);
            return o;
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            System.out.println("Class not created: " + className);
        }
        return null;
    }

    /**
     * This method creates and initializes the agents based on the configuration file.
     */
    @Override
    public void create() {

        List<String> linesFromFile = new ArrayList<>();
        try {
            // Read all lines from the file
            linesFromFile = Files.readAllLines(Paths.get(m_filename));
            // Check if the file has the correct format
            if (linesFromFile.size() % 3 != 0) {
                linesFromFile = null;
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create a set of agents from the file
        m_listAgents = new HashSet<>();
        for (int i = 0; i < linesFromFile.size() / 3; i++) {
            // Get the agent class name by the format: graph.AgentName
            String agentClass = "graph."+linesFromFile.get(i * 3).split("\\.")[1];
            String[] subs = linesFromFile.get(i * 3 + 1).split(",");
            String[] pubs = linesFromFile.get(i * 3 + 2).split(",");

            // Create the agent and add it to the set
            Agent agent = (Agent) dynamicallyCreateAgentClass(agentClass, subs, pubs);
            m_listAgents.add(agent);
        }
    }

    /**
     * Returns the name of the configuration.
     * @return The name of the configuration.
     */
    @Override
    public String getName() {
        return "Generic Config";
    }

    /**
     * Returns the version of the configuration.
     * @return The version of the configuration.
     */
    @Override
    public int getVersion() {
        return 1;
    }

    /**
     * Closes all agents associated with this configuration.
     */
    @Override
    public void close() {
        for (Agent agent : m_listAgents) {
            agent.close();
        }
        m_listAgents.clear();
    }

    /**
     * this method sets the configuration file path for the {@link GenericConfig} instance.
     * @param filename The path of the configuration file.
     */
    public void setConfFile(String filename) {
        if (Files.exists(Paths.get(filename)))
            m_filename = filename;
    }
}
