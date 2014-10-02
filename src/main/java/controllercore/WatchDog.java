package controllercore;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import shared.LogEvent;


public class WatchDog {
	private PluginConfig config;
	private Timer timer;
	public Queue<LogEvent> log;
	private long startTS;
	public String pluginSlot;	
	  
	  public WatchDog(Queue<LogEvent> log, PluginConfig config, String pluginSlot) {
		  this.config = config;
		  this.pluginSlot = pluginSlot;
		  startTS = System.currentTimeMillis();
		  this.log = log;
		  timer = new Timer();
	      timer.scheduleAtFixedRate(new WatchDogTask(), 500, config.getWatchDogTimer());
	      
	      LogEvent le = new LogEvent("INFO",pluginSlot,"WatchDog timer set to " + config.getWatchDogTimer() + " milliseconds");
		  log.offer(le);
	  }


	class WatchDogTask extends TimerTask {
	    public void run() {
	    	
	        //PluginEngine.this.getVersion(); 
	    	long runTime = System.currentTimeMillis() - startTS;
			 LogEvent le = new LogEvent("WATCHDOG",pluginSlot,"Plugin Uptime " + String.valueOf(runTime) + "ms");
			 log.offer(le);
	      //timer.cancel(); //Not necessary because we call System.exit
	      //System.exit(0); //Stops the AWT thread (and everything else)
	    
	    }
	  }

}
