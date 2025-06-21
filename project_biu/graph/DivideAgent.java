package graph;

/**
 * This class is used to divide the received messages and publish the result to an output topic.
 */
public class DivideAgent implements Agent {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_name;

    // input topics
    private String m_firstTopic;
    private String m_secondTopic;
    private String m_outputTopic;

    // input messages values & flags
    private double m_firstMessage;
    private double m_secondMessage;
    private boolean m_firstReceived;
    private boolean m_secondReceived;

//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor is used to create a new DivideAgent.
     *
     * @param divAgentName The name of the DivideAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to. Must have at least 2 values.
     * @param publishedTopics  An array of strings representing the topics to publish to. Must have at least one value.
     * @throws IllegalArgumentException If the conditions for subscribedTopics and publishedTopics are not met.
     */
    public DivideAgent(String divAgentName, String[] subscribedTopics, String[] publishedTopics) {
        if (subscribedTopics == null || publishedTopics == null || subscribedTopics.length < 1 || publishedTopics.length < 1)
            throw new IllegalArgumentException("DivideAgent: subscribedTopics and publishedTopics must have at least 2 and 1 values respectively");
        m_name = divAgentName;
        m_firstTopic = subscribedTopics[0];
        m_secondTopic = subscribedTopics[1];
        m_outputTopic = publishedTopics[0];

        TopicManagerSingleton.get().getTopic(m_firstTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_secondTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).addPublisher(this);
    }

    /**
     * This constructor is used to create a new DivideAgent with a single topic for both input and output.
     *
     * @param divAgentName The name of the DivideAgent.
     * @param subscribedTopic The input topic.
     * @param publishedTopic The output topic.  
     */
    public DivideAgent(String divAgentName, String subscribedTopic, String publishedTopic) {
        this(divAgentName, new String[]{subscribedTopic}, new String[]{publishedTopic});
    }

    /**
     * This method returns the name of the DivideAgent.
     *
     * @return The name of the DivideAgent.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * This method resets the firstMessage and secondMessage values to 0 and the flags to false.
     */
    @Override
    public void reset() {
        m_firstReceived = false;
        m_secondReceived = false;
        m_firstMessage = 0;
        m_secondMessage = 0;
    }

    /**
     * This method divides the received messages and publishes the result to the output topic
     * 
     * @param topic The topic on which the message was received.
     * @param msg   The received message.
     */
    @Override
    public void callback(String topic, Message msg) {
        if (Double.isNaN(msg.asDouble))
            return;
        if (topic.equals(m_firstTopic)) {
            m_firstReceived = true;
            m_firstMessage = msg.asDouble;
        } 
        else if (topic.equals(m_secondTopic)) {
            m_secondReceived = true;
            m_secondMessage = msg.asDouble;
        } 
        else return;

        if (m_firstReceived && m_secondReceived || m_secondMessage == 0){
            double out = m_firstMessage / m_secondMessage;
            TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(out));
        }
        else return;
    }

    /**
     * This method unsubscribes from the input and output topics and removes the agent from the output topic
     */
    @Override
    public void close() {
        TopicManagerSingleton.get().getTopic(m_firstTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_secondTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).removePublisher(this);
    }
}
