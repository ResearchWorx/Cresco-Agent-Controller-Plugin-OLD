package controllercore;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import channels.LogConsumer;
import plugin.PluginImplementation;
import shared.CmdEvent;
import shared.LogEvent;
import shell.AppShellFactory;
import shell.InAppPasswordAuthenticator;



public class ControllerEngine {

	public static PluginConfig config;
	public static ConcurrentLinkedQueue<LogEvent> logQueueOutgoing;
	public static ConcurrentLinkedQueue<LogEvent> logQueueIncoming;
	
	private String pluginName;
	private String pluginSlot;
	
	public static boolean logConsumerActive = false;
	public static boolean logConsumerEnabled = false;
	
	public static boolean AgentDiscoveryActive = false;
	public static boolean AgentDiscoveryEnabled = false;
	
	public static boolean ControlChannelEnabled = false; //control service on/off
	//public static boolean watchDogActive = false; //agent watchdog on/off
	
	
	public static ConcurrentHashMap<String,String> cmdMap;
	
	public static Map<String,Long> agentStatus;
	
	public static AppShellFactory appShell;
	
	
	public ControllerEngine()
	{
		//change this to the name of your plugin
		pluginName="ControllerPlugin";	
	}
	public void shutdown()
	{
		
	}
	public String getName()
	{
		   return pluginName; 
	}
	public String getVersion() //This should pull the version information from jar Meta data
    {
		   String version;
		   try{
		   String jarFile = PluginImplementation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		   File file = new File(jarFile.substring(5, (jarFile.length() -2)));
           FileInputStream fis = new FileInputStream(file);
           @SuppressWarnings("resource")
		   JarInputStream jarStream = new JarInputStream(fis);
		   Manifest mf = jarStream.getManifest();
		   
		   Attributes mainAttribs = mf.getMainAttributes();
           version = mainAttribs.getValue("Implementation-Version");
		   }
		   catch(Exception ex)
		   {
			   String msg = "Unable to determine Plugin Version " + ex.toString();
			   System.err.println(msg);
			   logQueueOutgoing.offer(new LogEvent("ERROR",pluginSlot,msg));
			   
			   
			   version = "Unable to determine Version";
		   }
		   
		   return pluginName + "." + version;
	   }
	//steps to init the plugin
	public boolean initialize(ConcurrentLinkedQueue<LogEvent> logQueue, SubnodeConfiguration configObj, String pluginSlot)  
	{
		this.logQueueOutgoing = logQueue;
		this.pluginSlot = pluginSlot;
		
		try{
			this.config = new PluginConfig(configObj);
			
			String startmsg = "Initializing Plugin: " + getVersion();
			System.err.println(startmsg);
			logQueue.offer(new LogEvent("INFO",pluginSlot,startmsg));
			
			WatchDog wd = new WatchDog(logQueue,config,pluginSlot);
			
			//Create Incoming log Queue wait to start
	    	logQueueIncoming = new ConcurrentLinkedQueue<LogEvent>();
    		LogConsumer lc = new LogConsumer(logQueueIncoming);
    		Thread logConsumerThread = new Thread(lc);
	    	logConsumerThread.start();
	    	while(!logConsumerEnabled)
	    	{
	    		Thread.sleep(1000);
	    		String msg = "Waiting for logConsumer Initialization...";
	    		logQueue.offer(new LogEvent("INFO","CORE",msg));
		    	System.out.println(msg);
	    	}
	    	
	    	//Create Discovery Service wait to start
	    	cmdMap = new ConcurrentHashMap<String,String>();
	    	agentStatus = new ConcurrentHashMap<String,Long>();
	    	AgentDiscovery ad = new AgentDiscovery(logQueueIncoming);
    		Thread AgentDiscoveryThread = new Thread(ad);
	    	AgentDiscoveryThread.start();
	    	while(!AgentDiscoveryEnabled)
	    	{
	    		Thread.sleep(1000);
	    		String msg = "Waiting for Agent Discovery Initialization...";
	    		logQueue.offer(new LogEvent("INFO","CORE",msg));
		    	System.out.println(msg);
	    	}
            
        	//Starting SSH Server
        	SshServer sshd = SshServer.setUpDefaultServer();
    		sshd.setPort(5222);
    		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
    		sshd.setPasswordAuthenticator(new InAppPasswordAuthenticator());
    		appShell = new AppShellFactory();
    		sshd.setShellFactory(appShell);
    		sshd.start();
			
    		return true;
    		
		
		}
		catch(Exception ex)
		{
			String msg = "ERROR IN PLUGIN: " + ex.toString();
			System.err.println(msg);
			logQueue.offer(new LogEvent("ERROR",pluginSlot,msg));
			return false;
		}
		
	}
	public LogEvent incomingLog(LogEvent le)
	{
		return le;
	}
	public CmdEvent incomingCommand(CmdEvent ce)
	{
		if(ce.getCmdType().equals("discover"))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("help\n");
			sb.append("show\n");
			sb.append("show_name\n");
			sb.append("show_version\n");
			ce.setCmdResult(sb.toString());
		}
		else if(ce.getCmdArg().equals("show") || ce.getCmdArg().equals("help"))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("\nPlugin " + getName() + " Help\n");
			sb.append("-\n");
			sb.append("show\t\t\t\t\t Shows Commands\n");
			sb.append("show name\t\t\t\t Shows Plugin Name\n");
			sb.append("show version\t\t\t\t Shows Plugin Version");
			ce.setCmdResult(sb.toString());
		}
		else if(ce.getCmdArg().equals("show_version"))
		{
			ce.setCmdResult(getVersion());
		}
		else if(ce.getCmdArg().equals("show_name"))
		{
			ce.setCmdResult(getName());
		}
		else
		{
			ce.setCmdResult("Plugin Command [" + ce.getCmdType() + "] unknown");
		}
		return ce;
	}
		
}
