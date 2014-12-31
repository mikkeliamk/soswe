
package com.belvain.soswe.workflow;

import com.belvain.soswe.core.Soswe;
//import java.util.Vector;
import org.quartz.JobDetail;
//import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 *	???
 * @author belvain
 */
public class WorkflowManager { //TODO figure out if this class is needed
    
//    private boolean running = false;
//    private Vector<Workflow> queue = new Vector<Workflow>();
//    private Scheduler scheduler = null;
    
    public WorkflowManager(){
         
    }
    
    public void addToQueue(JobDetail newFlow, Trigger trig){
        try {
            Soswe.scheduler.scheduleJob(newFlow, trig);
        } catch (SchedulerException ex) {
            ex.printStackTrace();
            System.err.println("Error scheduling job..");
        }
    }
    
}
