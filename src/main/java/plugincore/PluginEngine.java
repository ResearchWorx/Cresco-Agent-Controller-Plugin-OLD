package plugincore;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import channels.ControllerChannel;
import shared.Clogger;
import shared.MsgEvent;
import shared.MsgEventType;
import shared.PluginImplementation;
import shell.AppShellFactory;
import shell.AppShellFactory;
import shell.InAppPasswordAuthenticator;



public class PluginEngine {

	public static boolean hasController = false;
	public static ControllerChannel controllerChannel;
	
	public static PluginConfig config;
	public static ConcurrentLinkedQueue<MsgEvent> msgInQueue;
	
	public static ConcurrentMap<String,MsgEvent> rpcMap;
	
	public static ControllerDB gdb;
	public static Clogger clog;
	
	public static String pluginName;
	public static String pluginVersion;
	public static String plugin;
	public static String agent;
	public static String region;
	
	public static WatchDog wd;
	
	public static CommandExec commandExec;
	
	public static AgentDiscovery agentDiscover;
	public static ConcurrentMap<String,Long> discoveryMap;
	
	public static boolean ControlChannelEnabled = false; //control service on/off
	public static ConcurrentHashMap<String,String> cmdMap;
	public static AppShellFactory appShell;
	
	
	public PluginEngine()
	{
		pluginName="ControllerPlugin";	
	}
	public void shutdown()
	{
		System.out.println("Plugin Shutdown : Agent=" + agent + "pluginname=" + plugin);
		wd.timer.cancel(); //prevent rediscovery
		try
		{
			//try and remove entire region
			PluginEngine.gdb.removeNode(region, null, null);
		}
		catch(Exception ex)
		{
			String msg2 = "Plugin Shutdown Failed: Agent=" + agent + "pluginname=" + plugin;
			clog.error(msg2);
			
		}
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
			   System.out.println(msg);
			   clog.error(msg);
			   
			   //MsgEvent ee = new MsgEvent(MsgEventType.ERROR,PluginEngine.region,PluginEngine.agent,PluginEngine.plugin,msg);
			   //ee.setSrc(PluginEngine.region, PluginEngine.agent, PluginEngine.plugin);
			   //ee.setParam("dst_region", PluginEngine.region);
			   //PluginEngine.commandExec.cmdExec(ee);
				
			   version = "Unable to determine Version";
		   }
		   
		   return pluginName + "." + version;
	   }
	//steps to init the plugin
	public boolean initialize(ConcurrentLinkedQueue<MsgEvent> msgOutQueue,ConcurrentLinkedQueue<MsgEvent> msgInQueue, SubnodeConfiguration configObj, String region,String agent, String plugin)  
	{
		commandExec = new CommandExec(); //execute commands
		this.msgInQueue = msgInQueue;
		this.agent = agent;
		this.plugin = plugin;
		this.region = region;
		try{
			/*
			if(msgOutQueue == null) //logs are commands on controller so no queue is needed
			{
				System.out.println("MsgOutQueue==null");
				return false;
			}
			*/
			if(msgInQueue == null)
			{
				System.out.println("MsgInQueue==null");
				return false;
			}
			
			clog = new Clogger(msgInQueue,PluginEngine.region,PluginEngine.agent,PluginEngine.plugin);
			
			rpcMap = new ConcurrentHashMap<String,MsgEvent>();
			discoveryMap = new ConcurrentHashMap<String,Long>();
			
			
			this.config = new PluginConfig(configObj);
			
			//if controller exist load
			if(PluginEngine.config.getControllerIP() != null)
			{
				System.out.println("Controller : Master Controller Config Found");
				controllerChannel = new ControllerChannel();
				hasController = controllerChannel.getController();
				if(hasController)
				{
					System.out.println("Controller : Master Controller Found");
				}
				else
				{
					System.out.println("Controller : Unable to Contact Master Controller");
				}
			}		
			
			String startmsg = "Controller : Initializing Plugin: Region=" + region + " Agent=" + agent + " plugin=" + plugin + " version" + getVersion();
			clog.log(startmsg);
			
			System.out.println("Controller : ControllerDB Service");
			gdb = new ControllerDB(); //start graphdb service
			
			
	    	//Create Discovery Service wait to start
	    	cmdMap = new ConcurrentHashMap<String,String>();
	    	
	    	agentDiscover = new AgentDiscovery();
    		
	    	//Start UDP PeerDiscovery Listner
	    	/*
	    	int group = 6969; //set listener group
		    PeerDiscovery mp = new PeerDiscovery( group, 38000 );
			*/
	    	
        	//Starting SSH Server
        	
	    	/*
	    	SshServer sshd = SshServer.setUpDefaultServer();
    		sshd.setPort(5222);
    		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
    		sshd.setPasswordAuthenticator(new InAppPasswordAuthenticator());
    		appShell = new AppShellFactory();
    		sshd.setShellFactory(appShell);
    		sshd.start();
			*/
	    	
	    	wd = new WatchDog();
    		
    		return true;
    		
		
		}
		catch(Exception ex)
		{
			String msg = "ERROR IN PLUGIN: : Region=" + region + " Agent=" + agent + " plugin=" + plugin + " " + ex.toString();
			clog.error(msg);
			System.out.println(msg);
			
			
			return false;
		}
		
	}
	public void msgIn(MsgEvent me)
	{
		
		final MsgEvent ce = me;
		try
		{
		Thread thread = new Thread(){
		    public void run(){
		    	
		        try 
		        {
					MsgEvent re = commandExec.cmdExec(ce);
					if(re != null)
					{
						re.setReturn(); //reverse to-from for return
						msgInQueue.offer(re);
					}
					
					
				} 
		        catch(Exception ex)
		        {
		        	System.out.println("Controller : PluginEngine : msgIn Thread: " + ex.toString());
		        }
		    }
		  };
		  thread.start();
		}
		catch(Exception ex)
		{
			
			System.out.println("Controller : PluginEngine : msgIn Thread: " + ex.toString());
        	
		}
		
	}
	
	
		
}
