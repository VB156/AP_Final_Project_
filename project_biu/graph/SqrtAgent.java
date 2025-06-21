package graph;

/**
 * This class is used to implement the SqrtAgent.
 */
public class SqrtAgent implements Agent {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_sqrtAgentName;
    private String m_inputTopic;
    private String m_outputTopic;
    private double m_message;

//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor is used to create a new SqrtAgent instance.
     * 
     * @param sqrtAgentName The name of the SqrtAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to.
     * @param publishedTopics An array of strings representing the topics to publish to.
     */
    public SqrtAgent(String sqrtAgentName, String[] subscribedTopics, String[] publishedTopics) {
        if (subscribedTopics == null || publishedTopics == null || subscribedTopics.length < 1 || publishedTopics.length < 1)
            throw new IllegalArgumentException("SqrtAgent: subscribedTopics must have at least one topic and publishedTopics at least one");
        if (subscribedTopics.length > 1 || publishedTopics.length > 1) {
            throw new IllegalArgumentException("SqrtAgent: subscribedTopics and publishedTopics can only contain one topic each.");
        }

        this.m_sqrtAgentName = sqrtAgentName;
        this.m_inputTopic = subscribedTopics[0];
        this.m_outputTopic = publishedTopics[0];

        TopicManagerSingleton.get().getTopic(m_inputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).addPublisher(this);
    }

    /**
     * This constructor is used to create a new SqrtAgent instance with one published topic.
     * 
     * @param sqrtAgentName The name of the SqrtAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to.
     * @param publishedTopic A string representing the topic to publish to.
     */
    public SqrtAgent(String sqrtAgentName, String[] subscribedTopics, String publishedTopic) {
        this(sqrtAgentName, subscribedTopics, new String[]{publishedTopic});
    }

    /**
     * This method is used to get the name of the SqrtAgent.
     * 
     * @return The name of the SqrtAgent.
     */
    @Override
    public String getName() {
        return m_sqrtAgentName;
    }

    /**
     * This method is used to reset the SqrtAgent.
     */
    @Override
    public void reset() {
        m_message = 0;
    }


    /**
     * This method is called when a new message is received on the subscribed topic.
     * It calculates the square root of the received message and publishes the result to the output topic.
     *
     * @param topic The topic on which the message was received.
     * @param msg   The received message. It must be a non-negative double value.
     */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble) || msg.asDouble < 0) {
            return;
        }
        m_message = msg.asDouble;
        TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(Math.sqrt(m_message)));
    }

    /**
     * This method is used to close the SqrtAgent.
     */
    @Override
    public void close() {
        TopicManagerSingleton.get().getTopic(m_inputTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).removePublisher(this);
    }
}
