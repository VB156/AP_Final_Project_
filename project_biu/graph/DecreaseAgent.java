package graph;

/**
 * This class is used to decrement the received message by 1 and publish the result to an output topic.
 */
public class DecreaseAgent implements Agent {
    
//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_decAgentName;

    // input topic
    private String m_inputTopic;

    // output topic
    private String m_outputTopic;

    // input message value
    private double m_decMessageValue;


//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * DecreaseAgent Constructor 
     * 
     * @param agentName The name of the agent.
     * @param subscribedTopics Array of subscribed topics, the first element is used as the input topic
     * @param publishedTopics Array of published topics, the first element is used as the output topic
     * 
     * @throws IllegalArgumentException if subscribed topics or published topics are null or empty, or if they have more than one topic
     */
    public DecreaseAgent(String agentName, String[] subscribedTopics, String[] publishedTopics) {
        if (subscribedTopics == null || subscribedTopics.length == 0 || publishedTopics == null || publishedTopics.length == 0) {
            throw new IllegalArgumentException("Subscribed topics and published topics must have at least one topic");
        }
        if (subscribedTopics.length > 1 || publishedTopics.length > 1) {
            throw new IllegalArgumentException("Subscribed topics and published topics can only have one topic each");
        }

        this.m_decAgentName = agentName;
        this.m_inputTopic = subscribedTopics[0];
        this.m_outputTopic = publishedTopics[0];

        // Subscribe to the input topic and add the agent a s a publisher to the output topic
        TopicManagerSingleton.get().getTopic(m_inputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).addPublisher(this);
    }


    /**
     * This constructor is used to create a new DecreaseAgent with a single topic for both input and output.
     * 
     * @param agentName The name of the agent.
     * @param subscribedTopic The input topic.
     * @param publishedTopic The output topic.
     */
    public DecreaseAgent(String agentName, String subscribedTopic, String publishedTopic) {
        this(agentName, new String[]{subscribedTopic}, new String[]{publishedTopic});   
    }

    /**
     * This method returns the name of the agent
     * 
     * @return m_decAgentName The name of the agent
     */
    @Override
    public String getName() {
        return m_decAgentName;
    }

    /**
     * This method resets the message value to 0
     */
    @Override
    public void reset() {
        m_decMessageValue = 0;
    }

    /**
     * This method decrements the received message by 1 and publishes the result to the output topic
     * 
     * @param topic The topic on which the message was received.
     * @param msg   The received message. It is expected to contain a valid double value.
     */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble))
            return;

        m_decMessageValue = msg.asDouble;
        TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(m_decMessageValue - 1));
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
