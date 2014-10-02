package controllercore;


import java.util.Queue;
import shared.CmdEvent;
import shared.LogEvent;
import channels.AgentControlChannel;

public class AgentDiscovery implements Runnable {

	private static AgentControlChannel cs;
	private final Queue<LogEvent> logQueueIncoming;
	
	public AgentDiscovery(Queue<LogEvent> logQueueIncoming) throws Exception
	{
		this.logQueueIncoming = logQueueIncoming;
	    cs = new AgentControlChannel();  
	}
	
      public void run() 
      {
        
    	  try
    	  {
    		  ControllerEngine.AgentDiscoveryActive = true;
  			  ControllerEngine.AgentDiscoveryEnabled = true;
  			  
    		  while(true)
    			{
    			  
    			  synchronized(logQueueIncoming) 
    			  {
    		    		while ((!logQueueIncoming.isEmpty())) 
    		    		{
    		    			LogEvent le = logQueueIncoming.poll(); //get logevent
    		    			  
    		  			      if(le.getEventType().equals("WATCHDOG"))
    		    		      {
    		  			    	  String sourcekey; 
    		  			    	  if(le.getEventAgent().equals(le.getEventSource()))
    		  			    	  {
    		  			    		  sourcekey = le.getEventAgent();
    		  			    	  }
    		  			    	  else
    		  			    	  {
    		        				sourcekey = le.getEventAgent() + "_" + le.getEventSource();
    		  			    	  }
    		    		      	String[] s_str = le.getEventMsg().split(" ");
    		    		      	long watchTs = Long.parseLong(s_str[2].substring(0, s_str[2].length() - 2));
    		    		      	//System.out.println(watchTs);
    		    		      			if(!ControllerEngine.agentStatus.containsKey(sourcekey))
    		    		      			{
    		    		      				/*
    		    		      				ControllerEngine.agentStatus.put(sourcekey, watchTs);
    		    		      				System.out.println("New Agent/Plugin: " + sourcekey);
    		    		      				CmdEvent ce = refreshCmd(sourcekey);
    		    		      				//System.out.println(sourcekey + " COMMANDS " + ce.getCmdResult());
    		    		      				ControllerEngine.cmdMap.put(sourcekey, ce.getCmdResult()); 
    		    		      				*/		      					
    		    		      			}
    		    		      			else
    		    		      			{
    		    		      				/*
    		    		      				long lastCount = ControllerEngine.agentStatus.get(sourcekey);
    		    		      				if(lastCount > watchTs)
    		    		      				{
    		    		      					ControllerEngine.agentStatus.put(sourcekey, watchTs);
    		    		      					System.out.println("OLD:UPDATE");
    		    		      					CmdEvent ce = refreshCmd(sourcekey);
    		    		      					ControllerEngine.cmdMap.put(sourcekey, ce.getCmdResult());   		      		
    		    		      				}
    		    		      				*/
    		    		      			}
    		    		      }
    		    		      else if(le.getEventType().equals("CONFIG"))
    		    		      {
    		    		    	  String sourcekey = le.getEventSource();
    		    		    	  if(le.getEventMsg().equals("enabled"))
    		    		    	  {
    		    		    		  if(!ControllerEngine.agentStatus.containsKey(sourcekey))
    				      			  {
    				      					ControllerEngine.agentStatus.put(sourcekey, System.currentTimeMillis());
    				      					System.out.println("New Agent/Plugin: " + sourcekey);
    				      					CmdEvent ce = refreshCmd(sourcekey);
    				      					ControllerEngine.cmdMap.put(sourcekey, ce.getCmdResult()); 
    				      					
    				      					if(sourcekey.contains("_")) //refresh agent if plugin event
    				      					{
    				      						if(!ControllerEngine.agentStatus.containsKey(sourcekey))
    				  		      			    {
    				      							System.out.println("Plugin Registered Before Agent");
    				  		      			    }
    				      						String refresh_agent = sourcekey.substring(0,sourcekey.indexOf("_"));
    				    		  				ce = refreshCmd(refresh_agent);
    						      			    ControllerEngine.cmdMap.put(refresh_agent, ce.getCmdResult()); 			      						
    				      						
    				      					}
    				      					
    				      			  }
    		    		    		  else
    		    		    		  {
    		    		    			    System.out.println("ERROR : CONFIG sent for existing agent");
    		    		    		  }
    		    		    	  }
    		    		    	  else if(le.getEventMsg().equals("disabled"))
    		    		    	  {
    		    		    		  if(ControllerEngine.agentStatus.containsKey(sourcekey))
    				      			  {
    		    		    			  //remove all commands for agent0_plugin/1
    		    		    			  //remote agent status
    		    		    			  System.out.println("Removing Agent/Plugin: " + sourcekey);
    				      				  ControllerEngine.cmdMap.remove(sourcekey);
    		    		    			  ControllerEngine.agentStatus.remove(sourcekey);
    		    		    			  
    		    		    			  if(sourcekey.contains("_")) //refresh agent if plugin event
    				      					{
    				      						if(!ControllerEngine.agentStatus.containsKey(sourcekey))
    				  		      			    {
    				      							System.out.println("Plugin Registered Before Agent");
    				  		      			    }
    				      						String refresh_agent = sourcekey.substring(0,sourcekey.indexOf("_"));
    				    		  				CmdEvent ce = refreshCmd(refresh_agent);
    						      			    ControllerEngine.cmdMap.put(refresh_agent, ce.getCmdResult()); 			      							      						
    				      					}
    				      			  }
    		    		    		  else
    		    		    		  {
    		    		    			  System.out.println("ERROR : CONFIG sent for non-existant agent");
    		    		    		  }
    		    		    	  }
    		    		      }
    		    			}
    		    			
    		    			
    		    			
    		    		}
    		    	
  			    
    			}
    	  }
    	  catch(Exception ex)
    	  {
    		  System.out.println("Error : AgentDiscovery : " + ex.toString());
    	  }
      }
      public static CmdEvent refreshCmd(String sourcekey)
      {
    	  
   	   try
   	   {
   	    CmdEvent ce = cs.call(new CmdEvent("discover",sourcekey));
   		return ce;
   	   }
   	   catch(Exception ex)
   	   {
   		   System.out.println(ex.toString());
   		   return new CmdEvent("ERROR","ERROR");
   	   }
      }
}
