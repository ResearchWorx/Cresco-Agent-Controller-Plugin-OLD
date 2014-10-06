package channels;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import shared.CmdEvent;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import controllercore.ControllerEngine;


public class AgentControlChannel  {

	private Connection connection;
	private QueueingConsumer consumer;
	private Marshaller CmdEventMarshaller;
	private Unmarshaller CmdEventUnmarshaller;
	private Channel channel;
	private ConcurrentHashMap<String,Channel> agentChannelMap;
	
	
	public AgentControlChannel() throws Exception {
		
		//Create the AMPQ connection for each channel to share
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(ControllerEngine.config.getAMPQControlHost());
		factory.setUsername(ControllerEngine.config.getAMPQControlUser());
		factory.setPassword(ControllerEngine.config.getAMPQControlPassword());
		factory.setConnectionTimeout(10000);
	    connection = factory.newConnection();
	    channel = connection.createChannel();
	    
	    //Create XML marshal objects
	    JAXBContext jaxbContext = JAXBContext.newInstance(CmdEvent.class);
	    CmdEventUnmarshaller = jaxbContext.createUnmarshaller();
	    CmdEventMarshaller = jaxbContext.createMarshaller();
	    
	    //Create hash for all agent channels
	    agentChannelMap = new ConcurrentHashMap<String,Channel>();    
	}
	
	public CmdEvent call(CmdEvent ce) throws Exception {  
		
		CmdEvent CmdResponse = null; //set response to null
	    
		String arg = ce.getCmdArg().substring(ce.getCmdArg().indexOf("_") + 1);
		String agent = null;
		
		if(ce.getCmdArg().contains("_")) //for agent vs plugin commands
        {
        	agent = ce.getCmdArg().substring(0,ce.getCmdArg().indexOf("_"));
        }
        else
        {
        	agent = ce.getCmdArg();
        }
        
        //strip agent from cmdArg
        ce.setCmdArg(arg);
		
        String requestQueueName = ControllerEngine.config.getRegion() + "_control_" + agent; 
        String replyQueueName = channel.queueDeclare().getQueue();
        
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
        
	    String response = null;
		String corrId = java.util.UUID.randomUUID().toString();

	    BasicProperties props = new BasicProperties
	                                .Builder()
	                                .correlationId(corrId)
	                                .replyTo(replyQueueName)
	                                .build();

	    //unmarshal message
	    StringWriter CmdEventXMLString = new StringWriter();
        QName qName = new QName("com.researchworx.cresco.shared", "CmdEvent");
        JAXBElement<CmdEvent> rootM = new JAXBElement<CmdEvent>(qName, CmdEvent.class, ce);
        
        CmdEventMarshaller.marshal(rootM, CmdEventXMLString);
        
        channel.basicPublish("", requestQueueName, props, CmdEventXMLString.toString().getBytes());
        
	    while (true) {
	        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	        
	    	
	        if (delivery.getProperties().getCorrelationId().equals(corrId)) {
	            response = new String(delivery.getBody());
	            
	            InputStream stream = new ByteArrayInputStream(response.getBytes());		        
			    JAXBElement<CmdEvent> rootUm = CmdEventUnmarshaller.unmarshal(new StreamSource(stream), CmdEvent.class);		        
			    CmdResponse = rootUm.getValue();
			    //response = rce.getCmdType() + "," + rce.getCmdArg() + "," +  rce.getCmdResult();
			    break;
	        }
	    }
        
	    return CmdResponse; 
	}
	public void close() throws Exception {
	    connection.close();
	}
	
}
