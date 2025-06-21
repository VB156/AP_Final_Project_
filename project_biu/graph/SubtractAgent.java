package graph;

/**
 * This class is used to subtract the second message from the first message and publish the result to an output topic.
 */ 
public class SubtractAgent implements Agent {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_agentName;

    // input topics
    private String m_firstInputTopic;
    private String m_secondInputTopic;
    private String m_outputTopic;

    // input messages values & flags
    private double m_firstMsgValue;
    private double m_secondMsgValue;
    private boolean m_firstMsgIsReceived;
    private boolean m_secondMsgIsReceived;


//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor is used to create a new SubtractAgent instance with the given name, subscriptions, and publications.
     * 
     * @param subAgentName The name of the SubtractAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to. Must have at least 2 values.
     * @param publishedTopics An array of strings representing the topics to publish to. Must have at least one value.
     * 
     * @throws IllegalArgumentException If the conditions for subs and pubs are not met.
     */
    public SubtractAgent(String subAgentName, String[] subscribedTopics, String[] publishedTopics) {
        if (subscribedTopics == null || publishedTopics == null || subscribedTopics.length < 2 || publishedTopics.length < 1)
            throw new IllegalArgumentException("SubtractAgent: subscribedTopics must have at least 2 values and publishedTopics at least one");
        if (subAgentName == null || subAgentName.isEmpty())
            throw new IllegalArgumentException("SubtractAgent: subAgentName cannot be null or empty");
        
        this.m_agentName = subAgentName;
        this.m_firstInputTopic = subscribedTopics[0];
        this.m_secondInputTopic = subscribedTopics[1];
        this.m_outputTopic = publishedTopics[0];

        TopicManagerSingleton.get().getTopic(m_firstInputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_secondInputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).addPublisher(this);
    }

    /**
     * This constructor is using the other constructor with the same parameters but with a single published topic.
     * 
     * @param subAgentName The name of the SubtractAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to. Must have at least 2 values.
     * @param publishedTopic An array of strings representing the topics to publish to. Must have at least one value.
     *      */
    public SubtractAgent(String subAgentName, String[] subscribedTopics, String publishedTopic) {
        this(subAgentName, subscribedTopics, new String[]{publishedTopic});
    }

    /**
     * This method returns the name of the SubtractAgent.
     * 
     * @return The name of the SubtractAgent.
     */
    @Override
    public String getName() {
        return m_agentName;
    }

    /**
     * Resets the firstMsgValue and secondMsgValue values to 0 and the flags to false.
     */
    @Override
    public void reset() {
        m_firstMsgIsReceived = false;
        m_secondMsgIsReceived = false;
        m_firstMsgValue = 0;
        m_secondMsgValue = 0;
    }

    /**
     * This method is called when a new message is received on a subscribed topic.
     * 
     * @param topic The topic on which the message was received.
     * @param msg The received message.
     */
    @Override
    public void callback(String topic, Message msg) {
        //Check if the message is a number, if not return
        if (Double.isNaN(msg.asDouble))
            return; 

        if (topic.equals(m_firstInputTopic)) {
            m_firstMsgIsReceived = true;
            m_firstMsgValue = msg.asDouble;
        }
        else if (topic.equals(m_secondInputTopic)) {
            m_secondMsgIsReceived = true;
            m_secondMsgValue = msg.asDouble;
        }
        else return;

        // Publish only if two messages received
        if (m_firstMsgIsReceived && m_secondMsgIsReceived){
            double subResult = m_firstMsgValue - m_secondMsgValue;
            TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(subResult));
        }
        else return;
    }

    /**
     * This method unsubscribes from the input and output topics and removes the agent from the output topic
     */
    @Override
    public void close() {
        TopicManagerSingleton.get().getTopic(m_firstInputTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_secondInputTopic).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(m_outputTopic).removePublisher(this);  
    }
}
