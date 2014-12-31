package com.belvain.soswe.cluster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import com.belvain.soswe.core.Soswe;

public class ClusterUtils {
	
	/**
	 * Broadcast nodename and clustername in order to detect other nodes<br/>
	 * belonging to the same cluster
	 * 
	 * @param nodename Name of node
	 * @param clustername Name of a cluster to find
	 */
    public static void QueryNodes(String nodename, String clustername){
        try {
            DatagramSocket  c = new DatagramSocket();
            c.setBroadcast(true);
            String msg = "SOSWE_"+clustername+"-"+nodename;
            byte[] sendData = msg.getBytes();

            //Try the 255.255.255.255 first
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
                c.send(sendPacket);
            } catch (Exception e) {
            }

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
			    NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

			    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
			      continue; // Don't want to broadcast to the loopback interface
			    }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    // Send the broadcast package!
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                        c.send(sendPacket);
                    } catch (Exception e) {
                    }

                }
            }
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            boolean timedout = false;
            c.setSoTimeout(3000);
            while(!timedout){
                try {
                    c.receive(receivePacket);
		
                    //We have a response
                    System.out.println(">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
		
                    //Check if the message is correct
                    String message = new String(receivePacket.getData()).trim();
                    if (message.startsWith("SOSWE_"+Soswe.soswecluster.getClusterName())) {
                        String remoteName = message.split("-")[1];
                        if(!remoteName.equals(Soswe.name)){ //Dont add yourself
                            if(!Soswe.soswecluster.containsNode(remoteName)){ //Dont add node which is already there.
                                Soswe.soswecluster.addNode(new ClusterNode(remoteName, receivePacket.getAddress().toString(),receivePacket.getPort()));
                            }
                        }
                    }
                    
                }catch (SocketTimeoutException e){
                    timedout = true;  
                }
            }
            //Close the port!
            c.close();
        } catch (IOException ex) {
        }
    }
    
}
