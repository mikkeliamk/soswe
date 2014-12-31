
package com.belvain.soswe.util;

import com.belvain.soswe.core.Soswe;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * Quick implemantation of Quartz job listener
 * @author belvain
 * 
 */
public class SosweJobListener implements JobListener {

    public static final String LISTENER_NAME = "SosweJobListener";
    
    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jec) {
        String jobName = jec.getJobDetail().getKey().toString();
        // Get handled file
        JobDataMap dataMap = jec.getJobDetail().getJobDataMap();
        String file = dataMap.getString("filename");
        
        this.addToEventLog(file, "Job: "+jobName+" for "+file+" is about to start...");
        
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jec) {
        System.out.println("Vetoed");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jec, JobExecutionException jee) {
                String jobName = jec.getJobDetail().getKey().toString();
                
                // Get handled file
                JobDataMap dataMap = jec.getJobDetail().getJobDataMap();
                String file = dataMap.getString("filename");
                
                this.addToEventLog(file, "Job: "+jobName+" for "+file+" finished.");
                
                if(!jee.getMessage().equals("")){
                    this.addToEventLog(file, "Exception thrown by job "+jobName+" Exception: "+jee.getMessage()+"\n");
                    jee.printStackTrace();
                }
    }
    
    public void addToEventLog(String file, String msg){
        Soswe.eventLog.put(file, msg);
        if(Soswe.eventLog.size() > 20){
            String key = Soswe.eventLog.keySet().iterator().next();
            Soswe.eventLog.remove(key);
        }
    }
    
}
