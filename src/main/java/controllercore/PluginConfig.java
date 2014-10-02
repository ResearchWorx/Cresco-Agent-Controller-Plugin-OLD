package controllercore;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;

public class PluginConfig {

	private SubnodeConfiguration configObj; 
	  
	
	public PluginConfig(SubnodeConfiguration configObj) throws ConfigurationException
	{
	    this.configObj = configObj;
	}
	public String getPluginName()
	{
		return configObj.getString("pluginname");
	}
	public String getRegion()
	{
		return configObj.getString("region");
	}
	public int getWatchDogTimer()
	{
		return configObj.getInt("watchdogtimer");
	}
	
	public String getAMPQControlHost()
	{
		return configObj.getString("ampq_control_host");
	}
	
	public String getAMPQControlUser()
	{
		return configObj.getString("ampq_control_username");	    
	}
	
	public String getAMPQControlPassword()
	{
		return configObj.getString("ampq_control_password");	    
	}
	
	
}