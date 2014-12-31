package com.belvain.soswe.cluster;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.belvain.soswe.core.Soswe;

/**
 * Thread to run on background and receive packets from new nodes.
 */
public class DetectionThread implements Runnable {
	
    DatagramSocket socket;
    
    @Override
    public void run() {
        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            socket = new DatagramSocket(8888, InetAddress.getByName("0.0.0.0")); //TODO make port configurable
            socket.setBroadcast(true);
            
            while (true) {
                System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");
                //Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                //Packet received
                System.out.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
                System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));

                //See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();
                if (message.startsWith("SOSWE_"+Soswe.soswecluster.getClusterName())) {
                    String remoteName = message.split("-")[1];
                    if(!remoteName.equals(Soswe.name)){ //Dont add yourself
                        if(!Soswe.soswecluster.containsNode(remoteName)){ //Dont add node which is already there.
                            Soswe.soswecluster.addNode(new ClusterNode(remoteName, packet.getAddress().toString(),packet.getPort()));
                            String response = "SOSWE_"+Soswe.soswecluster.getClusterName()+"-"+Soswe.name;  //respond to sender so this can be added
                            byte[] sendData = response.getBytes();
                            //Send a response
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), 8888);
                            socket.send(sendPacket);
    
                            System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
                        }
                    }
    	  	
                }
            }
        } catch (IOException ex) {
        }
		
    }

}
