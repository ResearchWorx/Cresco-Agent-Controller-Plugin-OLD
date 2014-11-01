package plugincore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ControllerDB {

	Map<String,AgentNode> agentMap;
	
	public ControllerDB()
	{
			agentMap = new ConcurrentHashMap<String,AgentNode>();		
	}
	
	public Boolean isNode(String region, String agent, String plugin)
	{
		try{
		if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			if(agentMap.containsKey(agent))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
			if(isNode(region, agent, null))
			{
				if(agentMap.get(agent).isPlugin(plugin))
				{
					return true;
				}
			}
			return false;	
		}
		return false;
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : isNode ERROR : " + ex.toString());
			return false;
		}
	}
	
	public void addNode(String region, String agent, String plugin)
	{
		try{
		if((region != null) && (agent != null) && (plugin == null)) //agent node			
		{
			AgentNode aNode = new AgentNode(agent);
			agentMap.put(agent, aNode);
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node			
		{
			if(!isNode(region, agent, null))
			{
				AgentNode aNode = new AgentNode(agent);
				agentMap.put(agent, aNode);
			}
			agentMap.get(agent).addPlugin(plugin);
		}
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : addNode ERROR : " + ex.toString());
		}
	}
	
	public void setNodeParams(String region, String agent, String plugin, Map<String,String> paramMap)
	{
		try{
		if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			agentMap.get(agent).setAgentParams(paramMap);
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
			agentMap.get(agent).setPluginParams(plugin, paramMap);
		}
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : setNodeParams ERROR : " + ex.toString());
		}
	}
	
	public Map<String,String> getNodeParams(String region, String agent, String plugin)
	{
		try{
		if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			return agentMap.get(agent).getAgentParams();
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
			return agentMap.get(agent).getPluginParams(plugin);
		}	
		return null;
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : getNodeParams ERROR : " + ex.toString());
			return null;
		}
	}
	
	
	public void setNodeParam(String region, String agent, String plugin, String key, String value)
	{
		try{
		if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			agentMap.get(agent).setAgentParam(key, value);
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
			agentMap.get(agent).setPluginParam(plugin, key, value);
		}
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : setNodeParam ERROR : " + ex.toString());
		}
	}
	
	public void removeNode(String region, String agent, String plugin)
	{
		try{
		if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			agentMap.remove(agent);
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
			agentMap.get(agent).removePlugin(plugin);
		}
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : removeNode ERROR : " + ex.toString());
		}
	}
}
