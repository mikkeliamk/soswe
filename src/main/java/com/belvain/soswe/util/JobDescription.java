package com.belvain.soswe.util;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Pojo xml wrapper for scheduled workflow 
 */
@XmlRootElement(name="jobs")
public class JobDescription {
	
	private String name;
	private String group;
	private String nextFireTime;
	
	public JobDescription(){
		   
	}
	
	public JobDescription(String name, String group, String nextFireTime){
		this.name = name;
		this.group = group;
		this.nextFireTime = nextFireTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(String nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	
}
