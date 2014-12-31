package com.belvain.soswe.workflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Helper class to simplify creating new microservices
 */
public abstract class Microservice implements MicroserviceInterface  {
    
	private String name;
    private String description;
    private String exec;
    private String log;
    private String state; // "waiting", "running", "error", "completed"
    private String output;
    private String workflow;
    
    private boolean status; // true = success, false = failure
    private boolean completed;
    private boolean important;
    
    @Override
	public abstract String[] caps();
    
	@Override
	public void buildService(String name, String Desc, String exec, String log, boolean important, String workflow){
		this.setName(name);
		this.setDescription(Desc);
		this.setExec(exec);
		this.setLog(log);
		this.setState("waiting");
		this.setCompleted(false);
		this.setImportant(important);
		this.setWorkflow(workflow);
	}
	
    @Override
    public abstract boolean execute(String input,HashMap<String, Object> options) throws Exception;
    
    @Override
    public void log(){
        try {
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        	File file = new File(this.getLog()+sdf.format(new Date())+".log");
        	file.getParentFile().mkdirs();
        	FileWriter writer = new FileWriter(file,true);
        	PrintWriter printer = new PrintWriter(writer);
        	
        	if(!file.exists() || file.length() == 0){
        		file.createNewFile();
        		printer.println("#### LOG FILE FOR OSA-MICROSERVICE "+this.getName().toUpperCase()+" ####");
        	}else{
        		printer.println("\n****************************************************");
        	}
        	
        	printer.println("Date/time: "+new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
        	printer.println("Workflow:"+this.getWorkflow());
        	printer.println("Success:"+this.getState());
        	printer.println("Microservice output:");
        	printer.println("---------------------------------------");
        	printer.print(this.getOutput());
        	printer.println("---------------------------------------");
        	printer.close();
            writer.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    };
    
    public String getName() {
    	return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getExec() {
        return exec;
    }
    
    public void setExec(String exec) {
        this.exec = exec;
    }
    
    public String getLog() {
        return log;
    }
    
    public void setLog(String log) {
        this.log = log;
    }

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}
	
	public String getWorkflow() {
        return this.workflow;
    }
	
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }
	
}
