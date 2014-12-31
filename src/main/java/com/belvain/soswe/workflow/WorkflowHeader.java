/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.belvain.soswe.workflow;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Lightweight pojo xml wrapper to hold workflows header data
 * @author belvain
 */

@XmlRootElement(name="workflows")
public class WorkflowHeader {
    
    String name;
    String description;

    public String getName() {
        return name;
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
    
}
