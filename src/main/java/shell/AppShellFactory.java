package shell;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shared.CmdEvent;
import channels.AgentControlChannel;
import controllercore.ControllerEngine;

public class AppShellFactory implements Factory {

	public static AgentControlChannel cs;
	public static String word;
	public static List<Completer> completors;
	
    public Command create() 
    {
    	//Get command channel
    	try {
			cs = new AgentControlChannel();
			completors = new LinkedList<Completer>();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return new InAppShell();
    }
    
    public static void genCompletors()
    {
    	completors.clear();
        
    	/*
    	String[] StringArray0 = new String[]{"agent","plugins","version"};
    	String[] myStringArray1 = new String[]{"o1","o2","o3"};

    	StringsCompleter[] sc = new StringsCompleter[]{new StringsCompleter("show"),new StringsCompleter(StringArray0)};

		completors.add(
    			 new AggregateCompleter(
    					  new ArgumentCompleter(
    				//			new StringsCompleter("show"), new StringsCompleter(StringArray0), new NullCompleter())
    					  //new StringsCompleter("show"), new StringsCompleter(StringArray0))
    							sc)
    				                 
    							  //new StringsCompleter("show"), new StringsCompleter("agent", "plugins"), new NullCompleter())
    			                       )
    		                          );
		*/
			completors.add(
       			 new AggregateCompleter(
       					  new ArgumentCompleter(new StringsCompleter(InAppShell.SHELL_CMD_QUIT,InAppShell.SHELL_CMD_EXIT, InAppShell.SHELL_CMD_VERSION, InAppShell.SHELL_CMD_HELP))));
    	

    }
    

    private static class InAppShell implements Command, Runnable {

        private static final Logger log = LoggerFactory.getLogger(InAppShell.class);

        public static final boolean IS_MAC_OSX = System.getProperty("os.name").startsWith("Mac OS X");

        private static final String SHELL_THREAD_NAME = "InAppShell";
        private static final String SHELL_PROMPT = "cresco> ";
        private static final String SHELL_CMD_QUIT = "quit";
        private static final String SHELL_CMD_EXIT = "exit";
        private static final String SHELL_CMD_VERSION = "version";
        private static final String SHELL_CMD_HELP = "help";

        private InputStream in;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback callback;
        private Environment environment;
        private Thread thread;
        private ConsoleReader reader;
        private PrintWriter writer;
        
        
        public InputStream getIn() {
            return in;
        }

        public OutputStream getOut() {
            return out;
        }

        public OutputStream getErr() {
            return err;
        }

        public Environment getEnvironment() {
            return environment;
        }

        public void setInputStream(InputStream in) {
            this.in = in;
        }

        public void setOutputStream(OutputStream out) {
            this.out = out;
        }

        public void setErrorStream(OutputStream err) {
            this.err = err;
        }

        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        public void start(Environment env) throws IOException {
        	System.out.println("OS Name: " + System.getProperty("os.name"));
        	environment = env;
            thread = new Thread(this, SHELL_THREAD_NAME);
            thread.start();
        }

        public void destroy() {
            if (reader != null)
                reader.shutdown();
            thread.interrupt();
        }

        public void run() {
            try {
            	
            	FilterInputStream fis = new FilterInputStream(in) {        			
                                        @Override
                    public int read() throws IOException {
                     	
                        int i = in.read();
                        
                        if(i == 63)
                        {
                            //treat question mark as enter
                        	return 10;
                        }
                        return i;
                    }
                    
                };
                
                FilterOutputStream fos = new FilterOutputStream(out) {
                    @Override
                    public void write(final int i) throws IOException {
                        super.write(i);

                        // workaround for MacOSX!! reset line after CR..
                        /*
                        if (IS_MAC_OSX && i == ConsoleReader.CR.toCharArray()[0]) {
                            super.write(ConsoleReader.RESET_LINE);
                        }
                        */
                        if (i == ConsoleReader.CR.toCharArray()[0]) {
                            super.write(ConsoleReader.RESET_LINE);
                        }
                        
                    }
                };
                
            	reader = new ConsoleReader(fis,fos);         			
            	
                reader.setPrompt(SHELL_PROMPT);
                //reader.addCompleter(new StringsCompleter(SHELL_CMD_QUIT,
                //        SHELL_CMD_EXIT, SHELL_CMD_VERSION, SHELL_CMD_HELP));
                
                //reader.setHistory(history);
                genCompletors();
                
                
                for (Completer c : completors) {
            	reader.addCompleter(c);
                }
                
                writer = new PrintWriter(reader.getOutput());

                // output welcome banner on ssh session startup
                writer.println("****************************************************");
                writer.println("*        Welcome to Cresco Controller Shell        *");
                writer.println("****************************************************");
                
                writer.println(" ");
                writer.println("Warning Notification!!!");
                writer.println(" ");
                writer.println("This system is to be used by authorized users only for company work.");
                writer.println("Activities conducted on this system may be monitored and/or recorded with no");
                writer.println("expectation of privacy. All possible abuse and criminal activity may be");
                writer.println("handed over to the proper law enforcement officials for investigation and");
                writer.println("prosecution. Use implies consent to all of the conditions stated within this");
                writer.println(" ");
                writer.println("Warning Notification.");
				
                writer.flush();

                String line;
                
                while ((line = reader.readLine()) != null) {
                		
                	handleUserInput(line.trim());
                }

            } catch (InterruptedIOException e) {
                // Ignore
            } catch (Exception e) {
                System.out.println("Error executing InAppShell..." + e);
            	//log.error("Error executing InAppShell...", e);
            } finally {
                callback.onExit(0);
            }
        }

        private void handleUserInput(String line) throws Exception 
        {
        	line = line.replaceAll("\\s+", " ").trim().toLowerCase();
            String cmdString = line.replace(" ", "_");
            
            if (line.equalsIgnoreCase(SHELL_CMD_QUIT)
                    || line.equalsIgnoreCase(SHELL_CMD_EXIT))
                throw new InterruptedIOException();
            
            String response;
            if (line.equalsIgnoreCase(SHELL_CMD_VERSION))
                response = "unknown-static";
            
            else if (line.equalsIgnoreCase(SHELL_CMD_HELP))
                response = "Help is not implemented yet...";
            
            else
            {
            	response = cmdExec(cmdString);
            	
            }
            writer.println(response);
        	writer.flush();
        	
            
        }
    }

    public static String cmdExec(String cmdString) throws Exception {
    	
    	String[] s_string = cmdString.split("_");
    	List<String> cmdList = new ArrayList<String>();
    	StringBuilder sb = null;
        
    	String cmd = new String();
    	
    	if(ControllerEngine.cmdMap.size() > 0)
    	{
    	Iterator it = ControllerEngine.cmdMap.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
        
            String[] lines = pairs.getValue().toString().split(System.getProperty("line.separator"));
            
        
            for(String tmpStr : lines)
            {
            
            	String 	comStr = pairs.getKey().toString() + "_" + tmpStr;
            	//System.out.println("OK: " + comStr);
            
            	
            	if(comStr.toLowerCase().startsWith(cmdString))
            	{
            		String[] s_comStr = comStr.split("_");
            		int cmdPosition = s_string.length -1;
            		
            		if(s_comStr.length >= s_string.length + 1)
                	{
                		//System.out.println(s_comStr[cmdPosition + 1]);
                		//System.out.println(s_comStr[cmdPosition]);
                		//System.out.println("cmd: " + s_string.length + "com: " + s_comStr.length);
                		if(s_comStr[cmdPosition].equals(s_string[cmdPosition]))
                		{
                			if(!cmdList.contains(s_comStr[cmdPosition + 1]))
                			{
                				cmdList.add(s_comStr[cmdPosition + 1]);
                			}
                		}
                		else
                		{
                			if(!cmdList.contains(s_comStr[cmdPosition]))
                			{
                				cmdList.add(s_comStr[cmdPosition]);
                					
                			}
                		}
                		
                	}
                	else if(s_comStr.length == s_string.length)
                	{
                		if(!s_comStr[cmdPosition].equals(s_string[cmdPosition]))
                		{
                			if(!cmdList.contains(s_comStr[cmdPosition]))
                			{
                				cmdList.add(s_comStr[cmdPosition]);
                			}
                		}
                		
            		}
            	}
            	
            	if(comStr.toLowerCase().equals(cmdString.toLowerCase()))
            	{
            		cmd = cmdString;
            	}
            	
            	
            	
            	
            }
        		
        
        if((cmd.length() > 0) && (cmdList.size() == 0))
        {
        	CmdEvent ce = cs.call(new CmdEvent("execute",cmd));
         	return ce.getCmdResult();
        	
        }
        
        sb = new StringBuilder();
        for(String str : cmdList)
         {
        	 sb.append(str + "\n");
         }
        
        }
        if((cmd.length() == 0) && (cmdList.size() == 0))
        {
        	sb = new StringBuilder();
      		sb.append("*Invalid Command*\n");
        }
    }
    	else
    	{
    		sb = new StringBuilder();
    		sb.append("NO AGENTS/COMMANDS FOUND\n");
    	}
        return sb.toString().substring(0, sb.toString().length()-1);
    }
}