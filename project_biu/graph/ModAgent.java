package graph;

/**
 * This class is used to calculate the modulo of two messages and publish the result to an output topic.
 */ 
public class ModAgent implements Agent {

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
     * This constructor is used to create a new ModAgent instance with the given name, subscriptions, and publications.
     * 
     * @param modAgentName The name of the ModAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to. Must have at least 2 values.
     * @param publishedTopics An array of strings representing the topics to publish to. Must have at least one value.
     * 
     * @throws IllegalArgumentException If the conditions for subs and pubs are not met.
     */ 
    public ModAgent(String modAgentName, String[] subscribedTopics, String[] publishedTopics) {
        if (subscribedTopics == null || publishedTopics == null || subscribedTopics.length < 2 || publishedTopics.length < 1)
            throw new IllegalArgumentException("ModAgent: subscribedTopics must have at least 2 values and publishedTopics at least one");
        if (modAgentName == null || modAgentName.isEmpty())
            throw new IllegalArgumentException("ModAgent: modAgentName cannot be null or empty");
        
        this.m_agentName = modAgentName;
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
     * @param modAgentName The name of the ModAgent.
     * @param subscribedTopics An array of strings representing the topics to subscribe to. Must have at least 2 values.
     * @param publishedTopic An array of strings representing the topics to publish to. Must have at least one value.
     */
    public ModAgent(String modAgentName, String[] subscribedTopics, String publishedTopic) {
        this(modAgentName, subscribedTopics, new String[]{publishedTopic});
    }

    /**
     * This method returns the name of the ModAgent.
     * 
     * @return The name of the ModAgent.
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
        if (m_firstMsgIsReceived && m_secondMsgIsReceived || m_secondMsgValue==0){
            double out = m_secondMsgValue % m_firstMsgValue;
            TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(out));
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
