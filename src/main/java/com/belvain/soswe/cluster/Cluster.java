/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.belvain.soswe.cluster;

import java.util.ArrayList;

/**
 *
 * @author belvain
 */
public class Cluster {
    
    public ArrayList<ClusterNode> nodes;

    private String clusterName;
    
    public Cluster(String name){
    	this .nodes = new ArrayList<ClusterNode>();
        this.clusterName = name;
    }
    
    /**
     * Perform healthcheck to see if cluster is healty
     */
    public void performHealthCheck(){
        for(ClusterNode node : nodes){
            if(!node.healthCheck()){
                System.err.println("Connection to node "+node.getNodeName()+" has been lost!");
            }
        }
    }
    
    /**
     * @return Name of a cluster
     */
	public String getClusterName() {
		return clusterName;
	}
	
	/**
	 *@param clusterName New name for a cluster
	 *Sets name for the cluster
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	
	/**
	 *Adds new node to cluster
	 *@param node Node to be added to cluster
	 */
	public void addNode(ClusterNode node){
		System.out.println("Node: "+node.getNodeName()+" added to cluster");
		this.nodes.add(node);
	}
	
	/**
	 * @param nodename
	 * Checks if cluster contains node
	 */
	public boolean containsNode(String nodename){
		boolean contains = false;
		for(ClusterNode node : this.nodes){
			if(node.getNodeName().equals(nodename)){
				contains = true;
			}
		}
		return contains;
	}
    
}
