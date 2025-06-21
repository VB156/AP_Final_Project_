package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to represent a node in the graph.
 */
public class Node {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    private String m_NodeName;

    // List of edges of the node
    private List<Node> m_NodeEdges;

    private Message m_NodeMsg;

//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor is used to create a new Node instance with the given name.
     * 
     * @param name The name of the node.
     */
    public Node(String name) {
        this.m_NodeName = name;
        this.m_NodeEdges = new ArrayList<>();
    }

    /**
     * This method returns the name of the node.
     * 
     * @return The name of the node.
     */
    public String getName() {
        return m_NodeName;  
    }

    /**
     * This method sets the name of the node.
     * 
     * @param name The name of the node.
     */
    public void setName(String name) {
        this.m_NodeName = name;
    }

    /**
     * This method returns the message of the node.
     * 
     * @return The message of the node.
     */
    public Message getMsg() {
        return m_NodeMsg;
    }

    /**
     * This method sets the message of the node.
     * 
     * @param msg The message of the node.
     */
    public void setMsg(Message msg) {
        this.m_NodeMsg = msg;
    }

    /**
     * This method returns the edges of the node.
     * 
     * @return The edges of the node.
     */
    public List<Node> getEdges() {
        return m_NodeEdges;
    }

    /**
     * This method sets the edges of the node.
     * 
     * @param edges The edges of the node.
     */
    public void setEdges(List<Node> edges) {
        this.m_NodeEdges = edges;
    }

    /**
     * This method adds an edge to the node.
     * 
     * @param node The node to add to the edges.
     */
    public void addEdge(Node node) {
        m_NodeEdges.add(node);
    }


    /**
     * This method checks if the current node and its descendants form a cycle - using a DFS algorithm.
     * 
     * @return {@code true} if a cycle is detected, {@code false} otherwise.
     */
    public boolean hasCycles() {
        Set<Node> visitedNodes = new HashSet<>();
        Set<Node> stackNodes = new HashSet<>();
        return dfs(this, visitedNodes, stackNodes);
    }


    /**plusIn2
     * This method implements the DFS algorithm for detecting cycles in the graph.
     * 
     * @param currentNode The node to start the DFS from.
     * @param visitedNodes A set to keep track of visited nodes.
     * @param stackNodes A set to keep track of nodes currently in the DFS stack.
     * @return {@code true} if a cycle is detected, {@code false} otherwise.
     */
    private static boolean dfs(Node currentNode, Set<Node> visitedNodes, Set<Node> stackNodes) {
        // If the node is in the stack, there is a cycle
        if (stackNodes.contains(currentNode)) {
            return true;
        }
        // If the node is already visited, there is no cycle
        if (visitedNodes.contains(currentNode)) {
            return false;
        }

        // Add the node to the visited and stack sets
        visitedNodes.add(currentNode);
        stackNodes.add(currentNode);

        // Check if the neighbors have cycles
        for (Node neighbor : currentNode.getEdges()) {
            if (dfs(neighbor, visitedNodes, stackNodes)) {
                return true;
            }
        }

        // Remove the node from the stack
        stackNodes.remove(currentNode);
        return false;
    }
}