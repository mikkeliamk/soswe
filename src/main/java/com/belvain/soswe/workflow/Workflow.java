package com.belvain.soswe.workflow;

import com.belvain.soswe.core.Soswe;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Workflow implements Job{
    
    private String configFilepath;
    private String name;
    private String filename;
    private String organization;
    private String user;
    private String description;
    private Vector<Microservice> microservices;
    private Vector<String> flowCtrl;
    private int defaultAction; //1 = yes, 0 = ask, -1 = no
    private boolean running = false;
    private boolean started = false;
    private boolean waiting = false;
    private String finaloutput;
    private String answer;
    private HashMap<String, Object> options; 


    public Workflow(){
        this.name = null;
        this.filename = null;
        this.organization = null;
        this.user = null;
        this.description = null;
    }
    
    /**
     * Creates workflow steps from config file
     * @param dynOptions Options to be added to workflow context
     */
    public void buildWorkflow(HashMap<String,Object> dynOptions){
        File file = new File(this.configFilepath);
    	this.microservices = new Vector<Microservice>();
    	this.flowCtrl = new Vector<String>();
    	this.options = new HashMap<String,Object>();
    	
    	if(dynOptions != null){
    		this.options = dynOptions;
    	}
    	
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder dBuilder;
    	
		try {
			
			dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(file);
	    	doc.getDocumentElement().normalize();
	    	File microserviceFolder = new File(Soswe.msconfigfolder);
	    	
	    	NodeList nodes = doc.getElementsByTagName("workflow");
	    	boolean prompt = false;
	    	String question = "";
	    	
	    	for(int i = 0;i<nodes.getLength();i++){
	    		Node node = nodes.item(i);
	    		if(node.getNodeType() == Node.ELEMENT_NODE){
	    			if(this.name.equals(node.getAttributes().getNamedItem("name").getNodeValue())){		
	    				String action = node.getAttributes().getNamedItem("defaultAction").getNodeValue();
						if(action.equals("yes")){
							this.defaultAction = 1;
						}else if(action.equals("ask")){
							this.defaultAction = 0;
						}else if(action.equals("no")){
							this.defaultAction = -1;
						}
	    				NodeList subnodes = node.getChildNodes();
	    				for(int j = 0;j<subnodes.getLength();j++){
	    					Node subnode = subnodes.item(j);
	    					
	    	    			if("options".equalsIgnoreCase(subnode.getNodeName())){
	    	    				NodeList optionNodes = subnode.getChildNodes();
	    	    				for(int x = 0;x<optionNodes.getLength();x++){
	    	    					Node optionNode = optionNodes.item(x);
	    		    				if(optionNode.getNodeType() == Node.ELEMENT_NODE){
	    		    					this.options.put(optionNode.getNodeName(), optionNode.getAttributes().getNamedItem("option").getNodeValue());
	    		    				}
	    	    					
	    	    				}

	    	    			}else if(subnode.getNodeName().equals("prompt")){
	    						question = subnode.getAttributes().getNamedItem("question").getNodeValue();
	    						prompt = true;
	    					}else if(subnode.getNodeName().equals("task")){
	    						boolean important = Boolean.parseBoolean(subnode.getAttributes().getNamedItem("important").getNodeValue());
	    						if(prompt){
	    							this.flowCtrl.add(question);
	    							prompt = false;
	    						}else{
	    							this.flowCtrl.add("run");
	    						}
	    						String microserviceName = subnode.getAttributes().getNamedItem("name").getNodeValue();
	    						for(File mservice : microserviceFolder.listFiles()){
	    							Document msDoc = dBuilder.parse(mservice);
	    							if(msDoc.getElementsByTagName("service").item(0).getAttributes().getNamedItem("name").getNodeValue().equals(microserviceName)){
	    								Element element = (Element) msDoc.getElementsByTagName("service").item(0);
	    								
	    								String desc = getValue("description", element);
	    								String exec = getValue("exec", element);
	    								String log = getValue("log", element);
	    								
	    								MicroserviceInterface mserv = Soswe.pluginManager.getPlugin(MicroserviceInterface.class, new OptionCapabilities("name:"+microserviceName));
	    								mserv.buildService(microserviceName, desc, exec, log, important, this.name);
	    								
	    								this.microservices.add((Microservice) mserv);
	    								
	    							}
	    						}
	    					}
	    				}
	    			}	
	    		}
	    	}
	    	
		} catch (Exception e) {
			e.printStackTrace();
		} 	
    	
    }
    
    private String getValue(String tag, Element element) {
	    NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
	    Node node = (Node) nodes.item(0);
	    return node.getNodeValue();
	}

    /*
     * (non-Javadoc)
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        
    	this.configFilepath = jec.getJobDetail().getJobDataMap().getString("configFilepath");
    	this.name = jec.getJobDetail().getJobDataMap().getString("name");
    	this.filename = jec.getJobDetail().getJobDataMap().getString("filename");
    	this.organization = jec.getJobDetail().getJobDataMap().getString("organization");
    	this.user = jec.getJobDetail().getJobDataMap().getString("user");
        
    	HashMap<String, Object> paramOpts = new HashMap<String, Object>();
    	
    	for(String dynKey : jec.getMergedJobDataMap().keySet()){
    		paramOpts.put(dynKey, jec.getMergedJobDataMap().get(dynKey));
    	}
    	
        this.buildWorkflow(paramOpts);
        
    	this.running = true;
    	this.started = true;
    	String prevOutput = "";
    	
    	int microservicesSize = microservices.size();
    	
        try {
            for(int i=0; i < microservicesSize; i++){
            	if(flowCtrl.get(i).equals("run")){
            		 microservices.get(i).execute(prevOutput,this.options);
            	}else{
            		if(defaultAction > 0){
            			microservices.get(i).execute(prevOutput,this.options);
            		}else if(defaultAction == 0){
            			this.waiting = true;
            			microservices.get(i).setState("input");

            			synchronized (this) {
            			    this.wait();
            			}
            			
            			this.waiting = false;
            			if(answer.equals("yes")){
            				microservices.get(i).execute(prevOutput,this.options);
            			}else if(answer.equals("no")){
               			 	microservices.get(i).setState("canceled");
               			 	microservices.get(i).setOutput("");
               			 	microservices.get(i).setCompleted(true);
               			 	microservices.get(i).log();
            			}
            		}else if(defaultAction < 0){
            			 microservices.get(i).setState("canceled");
            			 microservices.get(i).setOutput("");
            			 microservices.get(i).setCompleted(true);
            			 microservices.get(i).log();
            		}
            	}
                prevOutput = microservices.get(i).getOutput();
                if(microservices.get(i).getState().equals("error")){
                	if(microservices.get(i).isImportant()){
                	    for(int j = (i+1);j<microservicesSize;j++){
	               			 microservices.get(j).setState("canceled");
	               			 microservices.get(j).setOutput("");
	               			 microservices.get(j).setCompleted(true);
	               			 microservices.get(j).log();
                		}
                		i = microservices.size();
                	}
                }
            }
        } catch (Exception e) {
            System.out.println("Error " + e.toString());
            this.running = false;
            
        }
        this.running = false;
        log();
        this.finaloutput = microservices.get((microservicesSize - 1)).getOutput();
    }
    
    /*
     * Writes log file from executin of a workflow
     */
	public void log() {
		
        try {

        	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        	File file = null;
        	
        	if (this.organization != null && this.user != null) {
        	    file = new File(Soswe.logfolder+"/"+this.organization+"/"+this.user+"/workflows/"+this.name+"/"+sdf.format(new Date())+".log"); 
        	} else {
        	    file = new File(Soswe.logfolder+"/workflows/"+this.name+"/"+sdf.format(new Date())+".log");
        	}
        	
        	file.getParentFile().mkdirs();
        	FileWriter writer = new FileWriter(file,true);
        	PrintWriter printer = new PrintWriter(writer);
        	
        	if(!file.exists() || file.length() == 0){
        		file.createNewFile();
        		printer.println("#### LOG FILE FOR WORKFLOW "+this.getName().toUpperCase()+" ####");
        	}else{
        		printer.println("\n****************************************************");
        	}

        	printer.println("Date/time: "+new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
            printer.println("File: "+this.getFilename());
        	printer.println("Worklow steps:");
        	printer.println("---------------------------------------");
        	for(int i = 0; i < microservices.size();i++){
        		printer.println("Step: "+i+" Name: "+microservices.get(i).getName()+" Success: "+microservices.get(i).getState());
        	}
        	printer.println("---------------------------------------");
        	printer.close();
        	writer.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getFilename() {
        return this.filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Vector<Microservice> getMicroservices() {
        return microservices;
    }

    public void setMicroservices(Vector<Microservice> microservices) {
        this.microservices = microservices;
    }
    
    public void addMicroservice(Microservice service){
    	this.microservices.add(service);
    }
    
    public ArrayList<HashMap<String,String>> getWorkflowstatus(){
    	ArrayList<HashMap<String,String>> stateMap = new ArrayList<HashMap<String,String>>();
    	int i = 0;
    	for(Microservice ms : this.microservices){
    		HashMap<String, String> currentService = new HashMap<String,String>();
    		currentService.put("name", ms.getName());
    		currentService.put("state", ms.getState());
    		if(ms.getState().equals("input")){
    			currentService.put("question", this.flowCtrl.get(i));
    		}  		
    		stateMap.add(currentService);
    		i++;
    	}
    	return stateMap;
    }

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public Vector<String> getFlowCtrl() {
		return flowCtrl;
	}

	public void setFlowCtrl(Vector<String> flowCtrl) {
		this.flowCtrl = flowCtrl;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public HashMap<String, Object> getOptions() {
		return options;
	}

	public void setOptions(HashMap<String, Object> options) {
		this.options = options;
	}

	public String getFinaloutput() {
		return finaloutput;
	}

	public void setFinaloutput(String finaloutput) {
		this.finaloutput = finaloutput;
	}

    public String getConfigFilepath() {
        return configFilepath;
    }
    
    public void setConfigFilepath(String configFilepath) {
        this.configFilepath = configFilepath;
    }

}
