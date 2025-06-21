package graph;

import java.util.function.BinaryOperator;

/**
 * This class is used to perform a binary operation on the received messages and publish the result to an output topic.
 */
public class BinOpAgent implements Agent {
    
//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_agentName;

    // input topics
    private String m_firstInputTopic;
    private String m_secondInputTopic;

    // output topic
    private String m_outputTopic;

    // input messages values & flags
    private double m_firstMsgValue;
    private double m_secondMsgValue;
    private boolean m_firstMsgIsReceived;
    private boolean m_secondMsgIsReceived;

    // binary operation
    private BinaryOperator<Double> m_binOp;


//------------------------------------------------------------------------------------------------------------------
//Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * BinOpAgent Constructor
     * 
     * @param agentName   The name of the agent.
     * @param firstInputTopic  The name of the first input topic.
     * @param secondInputTopic The name of the second input topic.
     * @param outputTopic The name of the output topic.
     * @param binOperator   The binary operation to be performed on the received messages.
     */
    public BinOpAgent(String agentName, String firstInputTopic, String secondInputTopic, String outputTopic, BinaryOperator<Double> binOperator) {
        this.m_agentName = agentName;
        this.m_firstInputTopic = firstInputTopic;
        this.m_secondInputTopic = secondInputTopic;
        this.m_outputTopic = outputTopic;
        this.m_binOp = binOperator;

        TopicManagerSingleton.get().getTopic(firstInputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(secondInputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(outputTopic).addPublisher(this);
    }   

    public BinOpAgent(String agentName, String firstInputTopic, String outputTopic, BinaryOperator<Double> binOperator) {
        this.m_agentName = agentName;
        this.m_firstInputTopic = firstInputTopic;
        this.m_secondInputTopic = null;
        m_secondMsgIsReceived = true;
        this.m_outputTopic = outputTopic;
        this.m_binOp = binOperator;

        TopicManagerSingleton.get().getTopic(firstInputTopic).subscribe(this);
        TopicManagerSingleton.get().getTopic(outputTopic).addPublisher(this);
    }   


    /**
     * This method returns the name of the agent
     * 
     * @return m_agentName The name of the agent.
     */
    @Override
    public String getName() {
        return m_agentName;
    }


    /**
     * This method resets the firstMessage and secondMessage values to 0 and their flags
     */
    @Override
    public void reset() {
        m_firstMsgIsReceived = false;
        m_secondMsgIsReceived = false;
        m_firstMsgValue = 0;
        m_secondMsgValue = 0;
    }

    /**
     * This method sends the message to the output topic if both messages are received
     * 
     * @param topic The topic of the message.
     * @param msg The message.
     */
    @Override
    public void callback(String topic, Message msg) {
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
        
        // if the message is not from the first or second input topic, return
        else return;

        if (m_firstMsgIsReceived && m_secondMsgIsReceived){
            // for binary operations, publish only if two messages received
            if (m_secondInputTopic != null) {
                double out = m_binOp.apply(m_firstMsgValue, m_secondMsgValue);
                TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(out));
            }
            // for unary operations, publish the result even if the second message is not received
            else {
                double out = m_binOp.apply(m_firstMsgValue, m_firstMsgValue);
                TopicManagerSingleton.get().getTopic(m_outputTopic).publish(new Message(out));
            }
        }
        else return;
    }

    /**
     * This method unsubscribes from the input and output topics and removes the agent from the output topic
     */
    @Override
    public void close() {
        TopicManagerSingleton.get().getTopic(m_firstInputTopic).unsubscribe(this);
        if (m_secondInputTopic != null) {
            TopicManagerSingleton.get().getTopic(m_secondInputTopic).unsubscribe(this);
        }
        TopicManagerSingleton.get().getTopic(m_outputTopic).removePublisher(this);
    }
}
