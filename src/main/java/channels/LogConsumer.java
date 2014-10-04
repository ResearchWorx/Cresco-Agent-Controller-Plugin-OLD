package channels;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Queue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import shared.CmdEvent;
import shared.LogEvent;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import controllercore.ControllerEngine;


public class LogConsumer implements Runnable {

	private final Queue<LogEvent> logQueueIncoming;
	private Channel channel;
    private Connection connection;
    private ConnectionFactory factory;    
 	public String EXCHANGE_NAME_INCOMINGLOG;
 	private QueueingConsumer consumer;
 	private Unmarshaller LogEventUnmarshaller;
    

 	public LogConsumer(Queue<LogEvent> logQueueIncoming) throws JAXBException
	{
		this.logQueueIncoming = logQueueIncoming;
		this.EXCHANGE_NAME_INCOMINGLOG = ControllerEngine.config.getRegion() + "_log";
		JAXBContext jaxbContext = JAXBContext.newInstance(LogEvent.class);
	    LogEventUnmarshaller = jaxbContext.createUnmarshaller();
	    
	}
	
	
	public void run() {
        
    	try{
    	
    		//Queue AMPQ Output
    		factory = new ConnectionFactory();
    		factory.setHost(ControllerEngine.config.getAMPQControlHost());
    		factory.setUsername(ControllerEngine.config.getAMPQControlUser());
    		factory.setPassword(ControllerEngine.config.getAMPQControlPassword());
    		factory.setConnectionTimeout(10000);
    	    
    		connection = factory.newConnection();
    		channel = connection.createChannel();
    		channel.exchangeDeclare(EXCHANGE_NAME_INCOMINGLOG, "fanout");
    		String queueName = channel.queueDeclare().getQueue();
    		channel.queueBind(queueName, EXCHANGE_NAME_INCOMINGLOG, "");

    		consumer = new QueueingConsumer(channel);
    		channel.basicConsume(queueName, true, consumer); 
    		
    	}
    	catch(Exception ex)
    	{
    		System.err.println("LogConsumer Initialization Failed:  Exiting");
    		System.err.println(ex);
    		System.exit(1);
    	}
    
    	ControllerEngine.logConsumerActive = true;
    	ControllerEngine.logConsumerEnabled = true;
    	
    	LogEvent le = new LogEvent("INFO","CORE","LogConsumer Started");
    	ControllerEngine.logQueueOutgoing.offer(le);
    	
    	while (ControllerEngine.logConsumerEnabled) 
    	{
        	try 
        	{
        		if(ControllerEngine.logConsumerActive)
        		{
        			
        			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        			String message = new String(delivery.getBody());
        			InputStream stream = new ByteArrayInputStream(message.getBytes());		        
    			    JAXBElement<LogEvent> rootUm = LogEventUnmarshaller.unmarshal(new StreamSource(stream), LogEvent.class);		        
    			    LogEvent ie  = rootUm.getValue();
    			    //generate logs
    			    ControllerEngine.logQueueIncoming.offer(ie);   			    
        		}
        		
				Thread.sleep(100);
        	}
        	catch (Exception ex)
        	{
        		System.out.println("ERROR : Log Consumer : " + ex.toString());
        	}
        	//don't close channel, let log out do this.
        	//perhaps remove code
        	/*
		    try
		    {
		    	if(channel.isOpen())
	 			{
	 				channel.close();
	 			}
	 			if(connection.isOpen())
	 			{
	        		connection.close();
	 			}
		    }
		    catch(Exception ex)
		    {
		    		System.out.println("LogConsumer Interupted" + ex.toString());	        	
		    		le = new LogEvent("ERROR","CORE","LogConsumer Interupted" + ex.toString());
		    		ControllerEngine.logQueueOutgoing.offer(le);
		    }
		    */	    	
			
        }
    	System.out.println("LogConsumer Disabled");   	
    	le = new LogEvent("INFO","CORE","LogConsumer Disabled");
    	ControllerEngine.logQueueOutgoing.offer(le);
    	return;
    }
	
}
