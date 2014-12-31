package com.belvain.soswe.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
 
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
 
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 
import com.belvain.soswe.cluster.Cluster;
import com.belvain.soswe.cluster.ClusterUtils;
import com.belvain.soswe.cluster.DetectionThread;
import com.belvain.soswe.util.NameGenerator;
import com.belvain.soswe.util.SosweJobListener;
import com.belvain.soswe.workflow.WorkflowHeader;
import com.belvain.soswe.workflow.WorkflowManager;
 
public class Soswe {
	
    public static final String VERSION = "1.0";
    public static String host;
    public static int port;
    public static String baseUrl;
    public static boolean clustering = false;
    public static String msconfigfolder;
    public static String msexecutables;
    public static String operatingmode;
    public static String clusterconfig;
    public static String workflowConfig;
    public static String logfolder;
    public static String name;
    public static ArrayList<WorkflowHeader> availableWorkflows = new ArrayList<WorkflowHeader>();
    public static WorkflowManager workflowmanager = new WorkflowManager();
    public static LinkedHashMap<String, String> eventLog = new LinkedHashMap<String, String>();
    public static Scheduler scheduler;
    public static Cluster soswecluster;
    public static PluginManager pluginManager;
 
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://"+host+"/"+baseUrl).port(port).build();
    } 
 
    protected static HttpServer startServer() throws IOException {
        System.out.println("Starting grizzly...");
        ResourceConfig rc = new ResourceConfig();
        rc.register(com.belvain.soswe.rest.SosweCom.class);
        rc.register(JacksonFeature.class);
        return GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), rc);
    }
 
    public static void main(String[] args) throws IOException {
	
        name = NameGenerator.Generate();
	    
        pluginManager = PluginManagerFactory.createPluginManager();
        pluginManager.addPluginsFrom(new File("microservice/classes/").toURI()); 
        
        Long starttime = System.currentTimeMillis();
        Properties properties = new Properties();
        try{
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.getListenerManager().addJobListener(new SosweJobListener(), GroupMatcher.jobGroupEquals("group1"));
            scheduler.start();
            
            properties.load(new FileInputStream("config/soswe.conf"));
            
            if(properties.getProperty("clustering").equals("TRUE")){
                clustering = true;
            }
            if(clustering){
                String clustername = properties.getProperty("clustername");
                soswecluster = new Cluster(clustername);
                ClusterUtils.QueryNodes(Soswe.name, Soswe.soswecluster.getClusterName());
                Thread detectionThread = new Thread(new DetectionThread());
                detectionThread.start(); //start listening UDP packets so other nodes can be detected.
            }
	        
	        if(properties.getProperty("msconfigfolder").equals("default")){
	            msconfigfolder = "microservice/microservice_config";
	        }else{
	            msconfigfolder = properties.getProperty("msconfigfolder");
	        }
	        
	        if(properties.getProperty("logfolder").equals("default")){
	            logfolder = "/var/log/soswe/workflow/";
            }else{
                logfolder = properties.getProperty("logfolder");
            }
	        
	        if(properties.getProperty("msexecutables").equals("default")){
	            msexecutables = "microservice/msexecutables";
	        }else{
	            msexecutables = properties.getProperty("msexecutables");
	        }
	        host = properties.getProperty("host");
	        port = Integer.parseInt(properties.getProperty("port"));
	        baseUrl = properties.getProperty("urlroot");
	        
	        workflowConfig = "config/workflows.xml"; //TODO add possibility to configure
	        
	    } catch (Exception ex) {
	        System.err.println("Could not start SOSWE, exiting...");
	        System.exit(0);
	    }
	    
        File workflowconf = new File(workflowConfig);
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(workflowconf);
            doc.getDocumentElement().normalize();
	
	        NodeList nodes = doc.getElementsByTagName("workflow");
	        for(int i = 0;i < nodes.getLength();i++){
	            Node node = nodes.item(i);
	            if(node.getNodeType() == Node.ELEMENT_NODE){
	                WorkflowHeader header = new WorkflowHeader();
	                header.setName(node.getAttributes().getNamedItem("name").getNodeValue());
	                header.setDescription(node.getAttributes().getNamedItem("description").getNodeValue());
	                availableWorkflows.add(header);
	            }
	        }
	        
	    }catch(Exception e){
	        System.err.println("Error reading workflows");
	    }
	    
	    System.out.println("Soswe node["+name+"] started in "+(System.currentTimeMillis() - starttime)+" ms\n"
	            + "*******************************************\n"
	            + "Clustering: "+clustering+"\n"
	            + "Microservice configuration folder: "+msconfigfolder+"\n"
	            + "Microservice executable folder "+msexecutables+"\n"
	            + "Log folder "+logfolder+"\n"
	            + "*******************************************"
	            );
	    	
	    /*Starting grizzly http server*/
	    HttpServer httpServer = startServer();
	    httpServer.start();
    }
}