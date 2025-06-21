package graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to create a graph from topics with the correct connections between nodes.
 */
public class Graph extends ArrayList<Node> {

//------------------------------------------------------------------------------------------------------------------
// Private Methods:
//------------------------------------------------------------------------------------------------------------------
    /**
     * This method creates a new node for a given topic and adds it to the graph.
     *
     * @param topicData the topic data to create a node for
     * @param topicMap a map to store the existing topic nodes
     * @return Node newTopicNode - the node corresponding to the given topicData
     */
    private Node createTopicNode(Topic topicData, HashMap<Topic, Node> topicMap) {
        // Check if the topic node already exists in the graph
        if (topicMap.containsKey(topicData))
            return topicMap.get(topicData);

        //else, create a new node for the topic
        String newTopicName = "T" + topicData.m_topicName;
        Node newTopicNode = new Node(newTopicName);
        this.add(newTopicNode);
        topicMap.put(topicData, newTopicNode);
        return newTopicNode;
    }

    /**
     * This method creates a new node for a given agent and adds it to the graph.
     * 
     * @param agent  agent to create a node for
     * @param nodeMap   map to store the existing agent nodes 
     * @return Node newAgentNode - the node corresponding to the given agent
     */
    private Node createAgentNode(Agent agent, HashMap<Agent, Node> nodeMap) {
        // Check if the agent node already exists in the graph
        if (nodeMap.containsKey(agent))
            return nodeMap.get(agent);
        String newAgentName = "A" + agent.getName();
        Node newAgentNode = new Node(newAgentName);
        this.add(newAgentNode);
        nodeMap.put(agent, newAgentNode);
        return newAgentNode;
    }


//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This method checks if the graph contains any cycles.
     *
     * @return true for cycles, false otherwise
     */
    public boolean hasCycles() {
        // check each node in the graph for cycles
        for (Node node : this) {
            if (node.hasCycles()) return true;
        }
        return false;
    }

    /**
     * This method creates a new graph from topics with the correct connections between nodes.
     * 
     * @throws IllegalArgumentException if the graph has cycles
     */

    public void createFromTopics() {
        // reset graph
        this.removeRange(0, this.size());

        // maps to store the existing topic and agent nodes
        HashMap<Topic, Node> topicMap = new HashMap<>();
        HashMap<Agent, Node> agentMap = new HashMap<>();

        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();

        // create a node for each topic and add it to the graph
        for (Topic topic : topicManager .getTopics()) {
            Node topicNode = createTopicNode(topic, topicMap);
            topic.setId(topicNode.toString().split("@")[1]);

            for (Agent a : topic.m_listSubscribers) {
                Node agentNode = createAgentNode(a, agentMap);
                topicNode.addEdge(agentNode);
            }
            
            for (Agent a : topic.m_listPublishers) {
                Node agentNode = createAgentNode(a, agentMap);
                agentNode.addEdge(topicNode);
            }
        }
    }
}
