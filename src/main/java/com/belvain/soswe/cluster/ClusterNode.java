package com.belvain.soswe.cluster;

/**
 * Mock-up class to be implemented in the future, data of a single node is saved here.
 * @author belvain
 * 
 */
public class ClusterNode {

    private String nodeName;
    private String nodeAddress;
    private int nodePort;
    private boolean Alive;
    
    public ClusterNode(String nodeName, String nodeAddress, int nodePort){
        this.nodeName = nodeName;
        this.nodeAddress = nodeAddress;
        this.nodePort = nodePort;
    }
    
    public boolean Connect(){
        //TODO connect to node
        return false;
    }
    
    public boolean healthCheck(){
        this.Alive = false; //TODO implement health check
        return Alive;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public boolean isAlive() {
        return Alive;
    }

    public void setAlive(boolean Alive) {
        this.Alive = Alive;
    }
    
}
