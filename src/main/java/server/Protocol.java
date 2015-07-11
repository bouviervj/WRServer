package server;

import java.util.HashMap;
import java.util.Vector;

import server.transaction.Action;
import server.transaction.Entity;
import server.transaction.Query;
import server.transaction.Reply;
import server.transaction.Service;


public class Protocol {
 
	private static final int WAITING = 0;
    private static final int SENTKNOCKKNOCK = 1;
 
    private int state = WAITING;
    
    public static HashMap<String,Vector<Service>> _serviceMap = new HashMap<String,Vector<Service>>();
 
 
    public Message processInput(ServerThread thread, Message theInput) {
        Message theOutput = null;
 
        if (state == WAITING) {
            theOutput = new Message(Message.TYPE_QUERY,new Query());
            state = SENTKNOCKKNOCK;
        } else if (state == SENTKNOCKKNOCK) {
        	
        	if (theInput._type==Message.TYPE_REPLY) {
        		
        		Reply reply = (Reply) theInput._object;
        		if (reply.services!=null) {
        			
        			thread.clientname = reply.from;
        			for (Service srv:reply.services) {
        				srv._thread = thread;
        			}
        			publishServices(reply);
        		}
        		
        	}
        
        } 
        
        return theOutput;
    }
    
    public void publishServices(Reply reply){
    	
    	_serviceMap.put(reply.from, reply.services);
    	
    }
    
    public static void callServices(String iType, String iActionCode){
    	
    	for (Vector<Service> services:_serviceMap.values()){
    	
    		for (Service srv:services) {
    			if (srv.type.equals(iType)) {
    				
    				Action act = new Action();
    				act.type = iType;
    				act.actioncode = iActionCode;
    				
    				Query aQuery = new Query();
    				aQuery.from = "server";
    				aQuery.to = "device";
    				
    				aQuery.actions = new Vector<Action>();
    				aQuery.actions.add(act);
    				
    				Message msg = new Message(Message.TYPE_QUERY, aQuery);
    				srv._thread.sendMessage( msg );
    				
    			}
    		}
    		
    	}
    	
    }
    
}
