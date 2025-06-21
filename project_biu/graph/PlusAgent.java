package graph;

/**
 * This class is used to implement the PlusAgent.
 */
public class PlusAgent implements Agent {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_plusAgentName;
    private String m_firstInputTopic;
    private String m_secondInputTopic;
    private String m_outputTopic;

    // Messages:
    private double m_firstMsgValue;
    private double m_secondMsgValue;
    private boolean m_firstMsgIsReceived;
    private boolean m_secondMsgIsReceived;

//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor is used to create a new PlusAgent instance.
     * 
     * @param plusAgentName The name of the PlusAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to.
     * @param publishedTopics An array of strings representing the topics to publish to.
     */

    public PlusAgent(String plusAgentName, String[] subscribedTopics, String[] publishedTopics) {
        if (subscribedTopics == null || publishedTopics == null || subscribedTopics.length < 1 || publishedTopics.length < 1)
            throw new IllegalArgumentException("PlusAgent: subscribedTopics must have at least 2 elements and publishedTopics at least one");
        this.m_plusAgentName = plusAgentName;
        this.m_firstInputTopic = subscribedTopics[0];
        this.m_secondInputTopic = subscribedTopics[1];
        this.m_outputTopic = publishedTopics[0];

        TopicManagerSingleton.get().getTopic(m_firstInputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_secondInputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).addPublisher(this);
    }

    /**
     * This constructor is used to create a new PlusAgent instance with one published topic.
     * 
     * @param plusAgentName The name of the PlusAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to.
     * @param publishedTopic A string representing the topic to publish to.
     */

    public PlusAgent(String plusAgentName, String[] subscribedTopics, String publishedTopic) {
        this(plusAgentName, subscribedTopics, new String[]{publishedTopic});
    }

    /**
     * This method is used to get the name of the PlusAgent.
     * 
     * @return The name of the PlusAgent.
     */
    @Override
    public String getName() {
        return m_plusAgentName;
    }

    /**
     * This method is used to reset the PlusAgent.
     */
    @Override
    public void reset() {
        m_firstMsgIsReceived = false;
        m_secondMsgIsReceived = false;
        m_firstMsgValue = 0;
        m_secondMsgValue = 0;
    }

    /**
     * This method is used to handle the callback from the subscribed topics.
     *
     * @param topic The topic on which the message was received.
     * @param msg   The received message.
     */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble))
            return;
        if (topic.equals(m_firstInputTopic)) {
            m_firstMsgIsReceived = true;
            m_firstMsgValue = msg.asDouble;
        } else if (topic.equals(m_secondInputTopic)) {
            m_secondMsgIsReceived = true;
            m_secondMsgValue = msg.asDouble;
        } else return;
        // Publish only if two messages received
        if (m_firstMsgIsReceived && m_secondMsgIsReceived)
        {
            double out = m_firstMsgValue + m_secondMsgValue;
            TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(out));
        }
        else return;
    }

    /**
     * This method is used to close the PlusAgent.
     */
    @Override
    public void close() {
        TopicManagerSingleton.get().getTopic(m_firstInputTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_secondInputTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).removePublisher(this);
    }
}
