package graph;

/**
 * This interface is used to define the agent's name, the agent's state, and the agent's callback.
 */
public interface Agent {
    String getName();
    void reset();
    void callback(String topic, Message msg);
    void close();
}
