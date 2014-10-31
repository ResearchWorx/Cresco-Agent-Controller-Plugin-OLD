package plugincore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import shared.MsgEvent;

public class AgentNode {

	//private MsgEvent de = null;
	private Map<String,MsgEvent> deMap = null;
	private String agentName;
	
	public AgentNode(String agentName, MsgEvent de)
	{
		this.agentName = agentName;
		deMap = new ConcurrentHashMap<String,MsgEvent>();
		deMap.put("this", de);
		
	}
	public MsgEvent getAgentDe()
	{
		return deMap.get("this");
	}
	public void setAgentDe(MsgEvent de)
	{
		deMap.put("this", de);
	}
	public MsgEvent getPluginDe(String pluginSlot)
	{
		if(deMap.containsKey(pluginSlot))
		{
		return deMap.get(pluginSlot);
		}
		else
		{
		return null;
		}
	}
	public void setPluginDe(String pluginSlot, MsgEvent de)
	{
		deMap.put(pluginSlot, de);
	}
	public void removePlugin(String pluginSlot)
	{
		deMap.remove(pluginSlot);
	}
	public ArrayList<String> getPlugins() 
	{
		ArrayList<String> ar = new ArrayList<String>();
		Iterator it = deMap.entrySet().iterator();
  	    while (it.hasNext()) 
  	    {
  	        Map.Entry pairs = (Map.Entry)it.next();
  	        if(!pairs.getKey().toString().equals("this"))//don't add self
  	        {
  	    	ar.add(pairs.getKey().toString());
  	        }
  	    }
  	    return ar;
	}
	
}
