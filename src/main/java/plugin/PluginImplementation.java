package plugin;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.configuration.SubnodeConfiguration;

import controllercore.ControllerEngine;
import plugins.PluginInterface;
import shared.CmdEvent;
import shared.LogEvent;


public class PluginImplementation implements PluginInterface {

	public ControllerEngine pe;
	
	public PluginImplementation()
	{
		pe = new ControllerEngine(); //actual plugin code
		
	}
	public String getName()
	{
		   return ((ControllerEngine) pe).getName(); 
	}
    public String getVersion()
    {
    	return ((ControllerEngine) pe).getVersion();
	}	
	public CmdEvent incomingCommand(CmdEvent command)
    {
		return ((ControllerEngine) pe).incomingCommand(command);
	}
	public LogEvent incomingLog(LogEvent log)
    {
		return ((ControllerEngine) pe).incomingLog(log);
	}
	
	   /*
	public CmdEvent incomingCommand(CmdEvent command);
	   public CmdEvent outgoingCommand(CmdEvent command);
	   public LogEvent incomingCommand(LogEvent log);
	   public LogEvent outgoingCommand(LogEvent log);
	   */
	
	public boolean initialize(ConcurrentLinkedQueue<LogEvent> logQueue,SubnodeConfiguration configObj,String pluginSlot) 
	{
	   return ((ControllerEngine) pe).initialize(logQueue, configObj,pluginSlot);
    }
	public void shutdown()
	{
		   ((ControllerEngine) pe).shutdown(); 
	}
	
}

