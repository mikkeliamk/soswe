package com.belvain.soswe.workflow;

import java.util.HashMap;

import net.xeoh.plugins.base.Plugin;

public interface MicroserviceInterface extends Plugin{
	public abstract boolean execute(String input,HashMap<String, Object> options) throws Exception;
	public void log();
	public String[] caps();
	public void buildService(String name, String Desc, String exec, String log, boolean important, String workflow);
}
