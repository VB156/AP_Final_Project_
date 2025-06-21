package graph;

/**
 * This class is used to increment the received message by 1 and publish the result to an output topic.
 */
public class IncAgent implements Agent {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_agentName;

    // input topic
    private String m_inputTopic;

    // output topic
    private String m_outputTopic;

    // last value
    private double m_lastValue;

    /**
     * This constructor is used to create a new IncAgent with the given name, input and output topics.
     *
     * @param incAgentName The name of the agent.
     * @param subscribedTopics An array containing the input topic(s) for the agent. The first element is used.
     * @param publishedTopics An array containing the output topic(s) for the agent. The first element is used.
     */
    public IncAgent(String incAgentName, String[] subscribedTopics, String[] publishedTopics) {
        if (subscribedTopics == null || subscribedTopics.length == 0 || publishedTopics == null || publishedTopics.length == 0) {
            throw new IllegalArgumentException("Both subscribedTopics and publishedTopics must contain at least one topic.");
        }
        if (subscribedTopics.length > 1 || publishedTopics.length > 1) {
            throw new IllegalArgumentException("subscribedTopics and publishedTopics can only contain one topic each.");
        }

        this.m_agentName = incAgentName;
        this.m_inputTopic = subscribedTopics[0];
        this.m_outputTopic = publishedTopics[0];

        // subscribe to the input topic and add publisher to the output topic
        TopicManagerSingleton.get().getTopic(m_inputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).addPublisher(this);
    }

    /**
     * This constructor is used to create a new IncAgent with a single topic for both input and output.
     * 
     * @param incAgentName The name of the agent.
     * @param subscribedTopic The input topic.
     * @param publishedTopic The output topic.
     */
    public IncAgent(String incAgentName, String subscribedTopic, String publishedTopic) {
        this(incAgentName, new String[]{subscribedTopic}, new String[]{publishedTopic});
    }


    /**
     * This method returns the name of the agent.
     * 
     * @return The name of the agent.
     */
    @Override
    public String getName() {
        return m_agentName;
    }

    /**
     * This method resets the last value to 0.
     */
    @Override
    public void reset() {
        m_lastValue = 0;
    }

    /**
     * This method is called when a new message is received on the subscribed topic.
     * It increments the received message by 1 and publishes the result to the output topic.
     *
     * @param topic The topic on which the message was received.
     * @param msg   The received message. It is expected to contain a valid double value.
     */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble)) {
            return;
        }
        m_lastValue = msg.asDouble;
        TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(m_lastValue + 1));
    }

    /**
     * This method unsubscribes from the input and output topics and removes the agent from the output topic
     */
    @Override
    public void close() {
        TopicManagerSingleton.get().getTopic(m_inputTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).removePublisher(this);
    }
}
