package plugincore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import shared.MsgEvent;
import shared.MsgEventType;


public class ControllerDB {

	Map<String,AgentNode> agentMap;
	private Cache<String, Long> blackListCache;
	//
	
	public ControllerDB()
	{
			agentMap = new ConcurrentHashMap<String,AgentNode>();		
			blackListCache = CacheBuilder.newBuilder()
				    .concurrencyLevel(4)
				    .softValues()
				    .maximumSize(10000)
				    .expireAfterWrite(15, TimeUnit.MINUTES)
				    .build();
	}
	
	public Boolean isNode(String region, String agent, String plugin)
	{
		try{
			if(isBlacklisted(agent))
			{
				return false;
			}
			
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
	
	private MsgEvent controllerMsgEvent(String region, String agent, String plugin, String controllercmd)
	{
		MsgEvent ce = new MsgEvent(MsgEventType.CONFIG,null,null,null,"Generated by ControllerDB");
		ce.setParam("controllercmd", controllercmd);
		
		
		if((region != null) && (agent != null) && (plugin != null))
		{
			ce.setParam("src_region", region);
			ce.setParam("src_agent", agent);
			ce.setParam("src_plugin", plugin);
			
		}
		else if((region != null) && (agent != null) && (plugin == null))
		{
			ce.setParam("src_region", region);
			ce.setParam("src_agent", agent);
		}
		else if((region != null) && (agent == null) && (plugin == null))
		{
			ce.setParam("src_region", region);
		}
		return ce;
	}
	
	public void addNode(String region, String agent, String plugin)
	{
		if(isBlacklisted(agent))
		{
			return;
		}
		
		System.out.println("Adding Node: " + region + " " + agent + " " + plugin);
		//CODY
		try{
		if((region != null) && (agent != null) && (plugin == null)) //agent node			
		{
			AgentNode aNode = new AgentNode(agent);
			agentMap.put(agent, aNode);
			//add to controller
			if(PluginEngine.hasController)
			{
				if(!PluginEngine.controllerChannel.addNode(controllerMsgEvent(region,agent,plugin,"addnode")))
				{
					System.out.println("Controller : ControllerDB : Failed to addNode to Controller");
				}
			}
			
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node			
		{
			if(!isNode(region, agent, null))
			{
				//AgentNode aNode = new AgentNode(agent);
				//agentMap.put(agent, aNode);
				//CODY
				addNode(region, agent, null);
			}
			agentMap.get(agent).addPlugin(plugin);
			//add to controller
			if(PluginEngine.hasController)
			{
				if(!PluginEngine.controllerChannel.addNode(controllerMsgEvent(region,agent,plugin,"addnode")))
				{
					System.out.println("Controller : ControllerDB : Failed to addNode to Controller");
				}
			}
		}
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : addNode ERROR : " + ex.toString());
		}
	}
	
	public void setNodeParams(String region, String agent, String plugin, Map<String,String> paramMap)
	{
		//extract config from param Map
		Map<String,String> configMap = PluginEngine.config.buildPluginMap(paramMap.get("msg"));

		try
		{
			if(isBlacklisted(agent))
			{
				return;
			}
			if((region != null) && (agent != null) && (plugin == null)) //agent node
			{
				agentMap.get(agent).setAgentParams(configMap);
				if(PluginEngine.hasController)
				{
					MsgEvent ce = controllerMsgEvent(region,agent,plugin,"setparams");
					ce.setParam("configparams", paramMap.get("msg"));
					if(!PluginEngine.controllerChannel.setNodeParams(ce))
					{
						System.out.println("Controller : ControllerDB : Failed to setParams for Node on Controller");
					}
				}
			}
			else if((region != null) && (agent != null) && (plugin != null)) //plugin node
			{
				agentMap.get(agent).setPluginParams(plugin, configMap);
				if(PluginEngine.hasController)
				{
					MsgEvent ce = controllerMsgEvent(region,agent,plugin,"setparams");
					ce.setParam("configparams", paramMap.get("msg"));
					if(!PluginEngine.controllerChannel.setNodeParams(ce))
					{
						System.out.println("Controller : ControllerDB : Failed to setParams for Node on Controller");
					}
				}
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
			if(isBlacklisted(agent))
			{
				return null;
			}
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
			if(isBlacklisted(agent))
			{
				return;
			}
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
			
		if((region != null) && (agent == null) && (plugin == null)) //agent node
		{
			//controller
			if(PluginEngine.hasController)
			{
				if(!PluginEngine.controllerChannel.removeNode(controllerMsgEvent(region,null,null,"removenode")))
				{
					System.out.println("Controller : ControllerDB : Failed to addNode to Controller");
				}
			}
			return;
		}
		if(isBlacklisted(agent))
		{
			return;
		}
		if((region != null) && (agent != null) && (plugin == null)) //agent node
		{
			agentMap.remove(agent);
			//controller
			if(PluginEngine.hasController)
			{
				if(!PluginEngine.controllerChannel.removeNode(controllerMsgEvent(region,agent,plugin,"removenode")))
				{
					System.out.println("Controller : ControllerDB : Failed to addNode to Controller");
				}
			}
		}
		else if((region != null) && (agent != null) && (plugin != null)) //plugin node
		{
			agentMap.get(agent).removePlugin(plugin);
			//controller
			if(PluginEngine.hasController)
			{
				if(!PluginEngine.controllerChannel.removeNode(controllerMsgEvent(region,agent,plugin,"removenode")))
				{
					System.out.println("Controller : ControllerDB : Failed to addNode to Controller");
				}
			}
		}
		blackListCache.put(agent, System.currentTimeMillis());
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerDB : removeNode ERROR : " + ex.toString());
		}
	}
	private boolean isBlacklisted(String agent)
	{
		Long blackListTime = blackListCache.getIfPresent(agent);
		if(blackListTime != null)
		{
			System.out.println("Controller : ControllerDB : Agent: " + agent + " blacklisted for " + blackListTime + "ms.");
			return true;
		}
		return false;
	}
}
