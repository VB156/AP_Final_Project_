package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class is used to wrap an existing agent and provide parallelism by sending messages to the agent from a separate thread.
 */
public class ParallelAgent implements Agent {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private Agent m_wrappedAgent;

    private BlockingQueue<MessageQueued> m_messageQueue;

    private boolean m_running;

    private Thread m_senderThread;

//------------------------------------------------------------------------------------------------------------------
// Inner Class:
//------------------------------------------------------------------------------------------------------------------
    /**
     * This class is used to hold a topic and a message together in queue.
     */
    public static class MessageQueued {
        private String m_topic;
        private Message m_msg;

        /**
         * Constructor for MessageQueued.
         *
         * @param topic The topic of the message.
         * @param msg   The message.
         */
        MessageQueued(String topic, Message msg) {
            this.m_msg = msg;
            this.m_topic = topic;
        }
    }

//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor is used to create a new ParallelAgent instance.
     * 
     * @param agent The agent to be wrapped.
     * @param queueSize size of the queue
     */
    public ParallelAgent(Agent agent, int queueSize) {
        this.m_wrappedAgent = agent;
        m_messageQueue = new ArrayBlockingQueue<>(queueSize);
        m_running = true;
        
        m_senderThread = new Thread(() -> {
            while (m_running) {
                //Waits for a message when queue is empty  
                MessageQueued msg = takeFromQueue(); 
                if (msg != null) {
                    m_wrappedAgent.callback(msg.m_topic, msg.m_msg);
                }
            }
        });
        m_senderThread.start();
    }

    /**
     * This method is used to take a message from the queue with error handling.
     * 
     * @return The message from the queue
     */
    public MessageQueued takeFromQueue() {
        try {
            return m_messageQueue.take(); 
        } catch (InterruptedException e) {
            //return null if thread is interrupted
            return null;
        }
    }

    /**
     * This method is used to put a message into the queue with error handling.
     * 
     * @param msg The message to be put into the queue.
     */
    public void put(MessageQueued msg) {
        try {
            m_messageQueue.put(msg);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * This method is used to get the name of the agent.
     * 
     * @return The name of the agent.
     */
    @Override
    public String getName() {
        return m_wrappedAgent.getName();
    }

    /**
     * This method is used to reset the agent.
     */
    @Override
    public void reset() { 
        m_wrappedAgent.reset();
    }

    /**
     * This method is used to send a message to the agent.
     * 
     * @param topic The topic of the message.
     * @param msg   The message.
     */
    @Override
    public void callback(String topic, Message msg) {
        put(new MessageQueued(topic, msg));
    }

    /**
     * This method is used to stop the agent and the sender thread.
     */
    @Override
    public void close() {
        m_running = false;
        m_senderThread.interrupt(); // if take() waits to new message
    }
}