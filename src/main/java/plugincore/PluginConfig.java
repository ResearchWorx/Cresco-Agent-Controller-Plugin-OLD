package plugincore;

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
	public String getControllerIP()
	{
		try{
		return configObj.getString("controllerip");
		}
		catch(Exception ex)
		{
			System.out.println("Controller : PluginConfig : ERROR " + ex.toString());
			return null;
		}
	}
	public String getControllerPort()
	{
		try{
		return configObj.getString("controllerport");
		}
		catch(Exception ex)
		{
			System.out.println("Controller : PluginConfig : ERROR " + ex.toString());
			return null;
		}
	}
}