package graph;

import java.util.Set;
import java.util.HashSet;

/**
 * This class is used to represent a topic in the system.
*/ 
public class Topic {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    public final String m_topicName;
    public Set<Agent> m_listSubscribers;
    public Set<Agent> m_listPublishers;

    private Message m_lastMessage;
    private String m_topicId;

    /**
     * This constructor is used to create a new Topic instance with the given name.
     * 
     * @param topicName The name of the Topic. It should be unique within the system.
     * 
     * @throws IllegalArgumentException If the topicName is null or empty.
     */
    Topic(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            throw new IllegalArgumentException("Invalid topic name: " + topicName);
        }

        this.m_topicName = topicName;
        m_listSubscribers = new HashSet<Agent>();
        m_listPublishers = new HashSet<Agent>();
    }

//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This method is used to subscribe an agent to the topic.
     * 
     * @param agent The agent to subscribe to the topic.
     */
    public void subscribe(Agent agent) {
        m_listSubscribers.add(agent);
    }

    /**
     * This method is used to unsubscribe an agent from the topic.
     * 
     * @param agent The agent to unsubscribe from the topic.
     */
    public void unsubscribe(Agent agent) {
        m_listSubscribers.remove(agent);
    }

    /**
     * This method is used to publish a message to the topic.
     * 
     * @param message The message to publish to the topic.
     */
    public void publish(Message message) {
        m_lastMessage = message; // Update last message received by the topic
        for (Agent sub : m_listSubscribers) {
            sub.callback(this.m_topicName, message); // Send message
        }
    }

    /**
     * This method is used to add a publisher to the topic.
     * 
     * @param agent The agent to add as a publisher to the topic.
     */
    public void addPublisher(Agent agent) {
        m_listPublishers.add(agent);
    }

    /**
     * This method is used to remove a publisher from the topic.
     * 
     * @param agent The agent to remove as a publisher from the topic.
     */
    public void removePublisher(Agent agent) {
        m_listPublishers.remove(agent);
    }

    /**
     * This method is used to get the last message published to the topic for showing in html.
     * 
     * @return The last message published to the topic.
     */
    public Message getLastMsg() {
        // return null if no message was published to the topic
        if (m_lastMessage == null) {
            return null;
        }
        return m_lastMessage;
    }

    /**
     * This method is used to get the id of the topic.
     * 
     * @return The id of the topic.
     */
    public String getId() {
        return m_topicId;
    }

    /**
     * This method is used to set the id of the topic.
     * 
     * @param id The id of the topic.
     */
    public void setId(String id) {
        this.m_topicId = id;
    }
}
