
package com.belvain.soswe.rest;

import com.belvain.soswe.cluster.ClusterNode;
import com.belvain.soswe.core.Soswe;
import com.belvain.soswe.util.JobDescription;
import com.belvain.soswe.workflow.Workflow;
import com.belvain.soswe.workflow.WorkflowHeader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * REST Web Service
 *
 * @author belvain
 */
@Path("/")
public class SosweCom {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of SosweCom
     */
    public SosweCom() {
    }
    
    /**
     * List all nodes currently in cluster.
     * @return Nodes currently in cluster, if clustering not enabled return string clustering not enabled.
     */
    @GET
    @Path("/nodes")
    @Produces(MediaType.TEXT_PLAIN)
    public String getNodes(){
    	if(Soswe.clustering){
    		String resp = "";
    		for(ClusterNode node : Soswe.soswecluster.nodes){
    			resp += "["+node.getNodeName()+"] @ "+node.getNodeAddress()+":"+node.getNodePort()+"\n";
    		}
    		
    		return resp;
    	}else{
    		return "clustering is not enabled";
    	}
    }
    
    /**
     * @return Current status of workflow-engine
     */
    @GET
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    public String getEvents() {
        if(Soswe.eventLog.isEmpty()){
            return "No activity.";
        }else{
            return new flexjson.JSONSerializer().deepSerialize(Soswe.eventLog);
        }
    }

    /**
     * @return available workflows in json format
     */
    @GET
    @Path("/list/json")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<WorkflowHeader> getJsonWorkflows() {
        return Soswe.availableWorkflows;
    }
    
    /**
     * @return workflows currently scheduled
     */
    @GET
    @Path("/currentjobs")
    @Produces(MediaType.APPLICATION_XML)
    public ArrayList<JobDescription> getCurrentJobs() {
    	ArrayList<JobDescription> jobs = new ArrayList<JobDescription>();
    	try {
		   for (String groupName : Soswe.scheduler.getJobGroupNames()) {
			   
			     for (JobKey jobKey : Soswe.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
			 
					  String jobName = jobKey.getName();
					  String jobGroup = jobKey.getGroup();
				 
					  List<Trigger> triggers = (List<Trigger>) Soswe.scheduler.getTriggersOfJob(jobKey);
					  String nextFireTime = triggers.get(0).getNextFireTime().toString();
					  
					  jobs.add(new JobDescription(jobName,jobGroup,nextFireTime));
			 
				  }
			 
		   }
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
    	
    	return jobs;
    }
    
    /**
     * @return available workflows in xml format
     */
    @GET
    @Path("/list/xml")
    @Produces(MediaType.APPLICATION_XML)
    public ArrayList<WorkflowHeader> getXmlWorkflows() {
        return Soswe.availableWorkflows;
    }
    
    /**
     * @param workflowName Name of a workflow
     * @return Description of a workflow in json format
     */
    @GET
    @Path("/{workflowName}/desc")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkflowHeader getDescription(@PathParam("workflowName") String workflowName) {
        WorkflowHeader head = new WorkflowHeader();
        for(WorkflowHeader header : Soswe.availableWorkflows){
            if(header.getName().equals(workflowName)){
                head = header;
            }
        }
        return head;
    }
    
    /**
     * Start workflow
     * @param workflowName Name of a workflow
     */
    @GET
    @Path("/start/{workflowName}")
    @Produces(MediaType.TEXT_PLAIN)
    public String startWorkflow(@PathParam("workflowName") String workflowName){
        String answer = "";
        boolean workflowFound = false;
        for(WorkflowHeader header : Soswe.availableWorkflows){
            if(header.getName().equals(workflowName)){
                workflowFound = true;
            }
        }
        if(workflowFound){
            answer = "Starting workflow: "+ workflowName +"\n"
                    + "With parameters: \n";
            MultivaluedMap<String,String> params = this.context.getQueryParameters();
            HashMap<String, Object> options = new HashMap<String, Object>();
            String filename = "";
            String organization = "";
            String user = "";
            
            for(String param : params.keySet()){
            	if(!param.equals("schedule")){
	                answer += param+"="+params.getFirst(param);
	                options.put(param, params.getFirst(param));
	                
	                if (param.equals("filename")) {
	                    filename = params.getFirst(param);
	                } else if (param.equals("organization")) {
	                    organization = params.getFirst(param);
	                } else if (param.equals("username")) {
	                    user = params.getFirst(param);
	                }
            	}
            }

            String uniqueWorkflowName = workflowName+"-"+filename;
            
            JobDetail job = JobBuilder.newJob(Workflow.class)
                    .withIdentity(uniqueWorkflowName, "group1")
                    .usingJobData("configFilepath", Soswe.workflowConfig) //these values will automatically be mapped to variables with same name in workflow class
                    .usingJobData("name", workflowName)
                    .usingJobData("filename", filename)
                    .usingJobData("organization", organization)
                    .usingJobData("user", user)
                    .build();
            
            //TODO add register for most common triggers and get parameters for creating one.
            //TODO fix error because trigger exists
            
            Trigger trigger; 
            
            if(context.getQueryParameters().containsKey("schedule")){
                trigger = TriggerBuilder
                		.newTrigger()
                		.withIdentity("cronTrigger-"+uniqueWorkflowName, "group1")
                		.withSchedule(
                			CronScheduleBuilder.cronSchedule(context.getQueryParameters().get("schedule").get(0)))
                        .startNow()
                        .build();
            }else{
                trigger = TriggerBuilder
                		.newTrigger()
                		.withIdentity("InstantTrigger"+uniqueWorkflowName, "group1")
                        .startNow()
                        .build();
            }
            
            trigger.getJobDataMap().putAll(options);
		
			try {
				if(Soswe.scheduler.checkExists(trigger.getKey())){ //this business seems bit shady? WTF Quartz?
					JobKey oldKey = Soswe.scheduler.getTrigger(trigger.getKey()).getJobKey();
					Soswe.scheduler.deleteJob(oldKey);
				}
				if(Soswe.scheduler.checkExists(job.getKey())){
					Soswe.scheduler.deleteJob(job.getKey());
				}
				
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		
            Soswe.workflowmanager.addToQueue(job, trigger);
            
        }else{
            answer = "Workflow not found!";
        }
        return answer;
        
    }

}