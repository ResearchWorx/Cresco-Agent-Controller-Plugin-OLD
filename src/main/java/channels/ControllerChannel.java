package channels;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import plugincore.PluginEngine;
import shared.MsgEvent;

public class ControllerChannel {

	private final String USER_AGENT = "Cresco-Agent-Controller-Plugin/0.5.0";
	
	private String controllerUrl;
	
	public ControllerChannel()
	{
		if(PluginEngine.config.getControllerIP() != null)
		{
			if(PluginEngine.config.getControllerPort() != null)
			{
				controllerUrl = "http://" + PluginEngine.config.getControllerIP() + ":" + PluginEngine.config.getControllerPort() + "/API";
			}
			else
			{
				controllerUrl = "http://" + PluginEngine.config.getControllerIP() + ":32000/API";
			}
		}
	}
	
	public boolean getController() 
	{
		try
		{
			String url = controllerUrl + "?type=config&paramkey=controllercmd&paramvalue=registercontroller&paramkey=src_region&paramvalue=" + PluginEngine.region + "&paramkey=src_agent&paramvalue=" + PluginEngine.agent + "&paramkey=src_plugin&paramvalue=" + PluginEngine.plugin;
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
			con.setConnectTimeout(5000);
			
			// optional default is GET
			con.setRequestMethod("GET");
 
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
 
			int responseCode = con.getResponseCode();
			
			if(responseCode == 200)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));				        
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) 
				{
						response.append(inputLine);
				}
				in.close();
			
				MsgEvent ce = null;
				try
				{
					ce = meFromJson(response.toString());
				}
				catch(Exception ex)
				{
					System.out.println("Controller : ControllerChannel : Error meFromJson");
				}					
				if(ce != null)
				{
					if(ce.getMsgBody() != null)
					{
						if(ce.getMsgBody().equals("controllerregistered"))
						{
							return true;
						}
					}
				}
			}
			return false;
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerChannel : getController : " + ex.toString());
			return false;
		}
	}
 
    public String urlFromMsg(String type, Map<String,String> leMap)
    {
    	
    	try
    	{
    		StringBuilder sb = new StringBuilder();
    		
    		sb.append("?type=" + type);
    		
    		Iterator it = leMap.entrySet().iterator();
    		while (it.hasNext()) 
    		{
    			Map.Entry pairs = (Map.Entry)it.next();
    			sb.append("&paramkey=" + URLEncoder.encode(pairs.getKey().toString(), "UTF-8") + "&paramvalue=" + URLEncoder.encode(pairs.getValue().toString(), "UTF-8"));
    			it.remove(); // avoids a ConcurrentModificationException
    		}
        return sb.toString();
    	}
    	catch(Exception ex)
    	{
    		System.out.println("Controller : ControllerChannel : urlFromMsg :" + ex.toString());
    		return null;
    	}
    }
    
    public boolean removeNode(MsgEvent le)
    {
		try
		{
			Map<String,String> tmpMap = le.getParams();
			Map<String,String> leMap = null;
			String type = null;
			synchronized (tmpMap)
			{
				leMap = new ConcurrentHashMap<String,String>(tmpMap);
				type = le.getMsgType().toString();
			}
			String url = controllerUrl + urlFromMsg(type,leMap);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
			con.setConnectTimeout(5000);
			
			// optional default is GET
			con.setRequestMethod("GET");
 
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
 
			int responseCode = con.getResponseCode();
			
			if(responseCode == 200)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));				        
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) 
				{
						response.append(inputLine);
				}
				in.close();
			
				MsgEvent ce = null;
				try
				{
					ce = meFromJson(response.toString());
				}
				catch(Exception ex)
				{
					System.out.println("Controller : ControllerChannel : Error meFromJson");
				}					
				if(ce != null)
				{
					if(ce.getMsgBody() != null)
					{
						if(ce.getMsgBody().equals("noderemoved"))
						{
							return true;
						}
					}
				}
			}
			return false;
			
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerChannel : sendControllerLog : " + ex.toString());
			return false;
		}
	}
 
    public boolean addNode(MsgEvent le)
    {
		try
		{
			Map<String,String> tmpMap = le.getParams();
			Map<String,String> leMap = null;
			String type = null;
			synchronized (tmpMap)
			{
				leMap = new ConcurrentHashMap<String,String>(tmpMap);
				type = le.getMsgType().toString();
			}
			String url = controllerUrl + urlFromMsg(type,leMap);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
			con.setConnectTimeout(5000);
			
			// optional default is GET
			con.setRequestMethod("GET");
 
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
 
			int responseCode = con.getResponseCode();
			
			if(responseCode == 200)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));				        
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) 
				{
						response.append(inputLine);
				}
				in.close();
			
				MsgEvent ce = null;
				try
				{
					ce = meFromJson(response.toString());
				}
				catch(Exception ex)
				{
					System.out.println("Controller : ControllerChannel : Error meFromJson");
				}					
				if(ce != null)
				{
					if(ce.getMsgBody() != null)
					{
						if(ce.getMsgBody().equals("nodeadded"))
						{
							return true;
						}
					}
				}
			}
			return false;
			
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerChannel : sendControllerLog : " + ex.toString());
			return false;
		}
	}
 
    
    public void sendController(MsgEvent le)
    {
		try
		{
			Map<String,String> tmpMap = le.getParams();
			Map<String,String> leMap = null;
			String type = null;
			synchronized (tmpMap)
			{
				leMap = new ConcurrentHashMap<String,String>(tmpMap);
				type = le.getMsgType().toString();
			}
			String url = controllerUrl + urlFromMsg(type,leMap);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
			con.setConnectTimeout(5000);
			
			// optional default is GET
			con.setRequestMethod("GET");
 
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
 
			int responseCode = con.getResponseCode();
			
			if(responseCode != 200)
			{
				System.out.println("Controller : ControllerChannel : sendControllerLog Error RepsonceCode: " + responseCode);
				System.out.println(url);
			}
			
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerChannel : sendControllerLog : " + ex.toString());
		}
	}
 
    
	// HTTP GET request
	private void sendGet() throws Exception {
 
		String url = "http://www.google.com/search?q=mkyong";
 
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		
		//print result
		System.out.println(response.toString());
 
	}
 
	private MsgEvent meFromJson(String jsonMe)
	{
		Gson gson = new GsonBuilder().create();
        MsgEvent me = gson.fromJson(jsonMe, MsgEvent.class);
        //System.out.println(p);
        return me;
	}
}
