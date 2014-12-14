package plugincore;

import channels.ControllerChannel;
import shared.MsgEvent;
import shared.MsgEventType;



public class CommandExec {

	public CommandExec()
	{
		
	}
	
	
	public MsgEvent cmdExec(MsgEvent ce)
	{
		try
		{
			String callId = ce.getParam("callId-" + PluginEngine.region + "-" + PluginEngine.agent + "-" + PluginEngine.plugin); //unique callId
			if(callId != null) //this is a callback put in RPC hashmap
			{
				PluginEngine.rpcMap.put(callId, ce);
				return null;
			}
			else if((ce.getParam("dst_region") != null) && (ce.getParam("dst_agent") != null) && (ce.getParam("dst_plugin") != null))
			{
				if((ce.getParam("dst_region").equals(PluginEngine.region)) && (ce.getParam("dst_agent").equals(PluginEngine.agent)) && (ce.getParam("dst_plugin").equals(PluginEngine.plugin)))
				{	
					//message is for this plugin!
					if(ce.getMsgType() == MsgEventType.DISCOVER)
					{
						//ce.setParams(PluginEngine.config.getPluginConfig());
						/*
						StringBuilder sb = new StringBuilder();
						sb.append("help\n");
						sb.append("show\n");
						sb.append("show_name\n");
						sb.append("show_version\n");
						ce.setMsgBody(sb.toString());
						*/
						ce.setMsgBody(PluginEngine.config.getPluginConfigString());
						
					}
					else if(ce.getMsgType() == MsgEventType.EXEC) //Execute and respond to execute commands
					{
						if(ce.getParam("cmd").equals("show") || ce.getParam("cmd").equals("?") || ce.getParam("cmd").equals("help"))
						{
							StringBuilder sb = new StringBuilder();
							sb.append("\nPlugin " + PluginEngine.pluginName + " Help\n");
							sb.append("-\n");
							sb.append("show\t\t\t\t\t Shows Commands\n");
							sb.append("show name\t\t\t\t Shows Plugin Name\n");
							sb.append("show version\t\t\t\t Shows Plugin Version");
							ce.setMsgBody(sb.toString());
						}
						else if(ce.getParam("cmd").equals("show_version"))
						{
							ce.setMsgBody(PluginEngine.pluginName);
						}
						else if(ce.getParam("cmd").equals("show_name"))
						{
							ce.setMsgBody(PluginEngine.pluginVersion);
						}
					}
					else
					{
						ce.setMsgBody("Plugin Command [" + ce.getMsgType().toString() + "] unknown");
					}
					
					return ce;
				}
				else
				{
					System.out.println("Controller : MsgIn : Msg Routing Error : Msg Not for this plugin");
					System.out.println("MsgType=" + ce.getMsgType().toString());
					System.out.println("Region=" + ce.getMsgRegion() + " Agent=" + ce.getMsgAgent() + " plugin=" + ce.getMsgPlugin());
					System.out.println("params=" + ce.getParamsString());
					return null;
				}
			}
			//message for controller
			else if((ce.getParam("dst_region") != null) && (ce.getParam("dst_agent") == null) && (ce.getParam("dst_plugin") == null))
			{	
					if((ce.getParam("dst_region").equals(PluginEngine.region)) && (ce.getParam("dst_agent") == null) && (ce.getParam("dst_plugin") == null))
					{
						final MsgEvent me = ce;
						if((ce.getMsgType() == MsgEventType.CONFIG) || (ce.getMsgType() == MsgEventType.WATCHDOG)) //Check for discover messages		
						{
							
								Thread thread = new Thread(){
									public void run(){
										//System.out.println("Discovery Thread Started Region=" + me.getParam("src_region") + " agent=" + me.getParam("src_agent") + " plugin=" + me.getParam("src_plugin"));
										PluginEngine.agentDiscover.discover(me); //start discovery process
									}
								};
								thread.start();
							
						}
						
						//log to master controller
						//remove dest info
						ce.removeParam("dst_region");
						ce.removeParam("dst_agent");
						ce.removeParam("dst_plugin");
						PluginEngine.commandExec.cmdExec(ce);
						
						return null;
					}
					else
					{
						System.out.println("**THIS SHOULD NEVER HAPPEN! CONTROLLER WITH ANOTHER REGIONS'S LOGS");
						return null;
					}
			}
			else if((ce.getParam("dst_region") == null) && (ce.getParam("dst_agent") == null) && (ce.getParam("dst_plugin") == null))
			{
				if(PluginEngine.hasController)
				{
					//ce.setParam("controllerlog", "log");
					if(ce.getParam("controllercmd") != null)
					{
						final MsgEvent me = ce;
						
						Thread thread = new Thread(){
							public void run(){
								/*
								System.out.println("Routing to Master Controller");
								System.out.println("MsgType=" + me.getMsgType().toString());
								System.out.println("Region=" + me.getMsgRegion() + " Agent=" + me.getMsgAgent() + " plugin=" + me.getMsgPlugin());
								System.out.println("params=" + me.getParamsString());
								*/
								PluginEngine.controllerChannel.sendController(me);
							}
						};
						thread.start();
					}
					return null;
				}
				return null;
			}
			else
			{
				System.out.println("Controller : CommandExec : Unknown Message");
				System.out.println("MsgType=" + ce.getMsgType().toString());
				System.out.println("Region=" + ce.getMsgRegion() + " Agent=" + ce.getMsgAgent() + " plugin=" + ce.getMsgPlugin());
				System.out.println("params=" + ce.getParamsString());
				return null;
			}
			
			
	
		}
		catch(Exception ex)
		{
			MsgEvent ee = new MsgEvent(MsgEventType.ERROR,PluginEngine.region,PluginEngine.agent,null,"Controller : MsgIn : Error :" + ex.toString());
			return ee;
		}
	}
	
	
	
}
